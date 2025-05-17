package com.example.mobilestore.ui.order;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.cardview.widget.CardView;

import com.example.mobilestore.R;
import com.example.mobilestore.data.customer.CustomerDataManager;
import com.example.mobilestore.model.Order;

import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity {

    private RecyclerView orderRecyclerView;
    private TextView titleTextView, emptyOrdersTextView;
    private ImageView avatarImageView;
    private Button backButton;
    private CustomerDataManager customerDataManager;
    private List<Order> ordersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        try {
            // Khởi tạo CustomerDataManager
            customerDataManager = CustomerDataManager.getInstance(this);

            // Lấy danh sách đơn hàng
            ordersList = customerDataManager.getOrdersList();

            // Khởi tạo view
            initializeViews();

            // Hiển thị danh sách đơn hàng
            setupOrdersList();

            // Thiết lập nút quay lại
            setupBackButton();
        } catch (Resources.NotFoundException e) {
            Toast.makeText(this, "Order history layout not found: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing order history: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeViews() {
        orderRecyclerView = findViewById(R.id.orderRecyclerView);
        titleTextView = findViewById(R.id.titleTextView);
        avatarImageView = findViewById(R.id.avatarImageView);
        emptyOrdersTextView = findViewById(R.id.emptyOrdersTextView);
        backButton = findViewById(R.id.backButton);

        if (titleTextView != null) {
            titleTextView.setText("Order History");
        }
    }

    private void setupOrdersList() {
        if (orderRecyclerView != null) {
            orderRecyclerView.setLayoutManager(new LinearLayoutManager(this));

            if (ordersList != null && !ordersList.isEmpty()) {
                if (emptyOrdersTextView != null) {
                    emptyOrdersTextView.setVisibility(View.GONE);
                }
                orderRecyclerView.setVisibility(View.VISIBLE);

                OrderAdapter adapter = new OrderAdapter(ordersList);
                orderRecyclerView.setAdapter(adapter);
            } else {
                if (emptyOrdersTextView != null) {
                    emptyOrdersTextView.setVisibility(View.VISIBLE);
                }
                orderRecyclerView.setVisibility(View.GONE);
            }
        }
    }

    private void setupBackButton() {
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
    }

    // Adapter cho RecyclerView hiển thị đơn hàng
    private class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
        private List<Order> orders;

        public OrderAdapter(List<Order> orders) {
            this.orders = orders;
        }

        @NonNull
        @Override
        public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            try {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
                return new OrderViewHolder(view);
            } catch (Resources.NotFoundException e) {
                CardView cardView = new CardView(parent.getContext());
                cardView.setLayoutParams(new CardView.LayoutParams(
                        CardView.LayoutParams.MATCH_PARENT,
                        CardView.LayoutParams.WRAP_CONTENT));
                cardView.setCardElevation(8);
                cardView.setRadius(12);
                cardView.setContentPadding(16, 16, 16, 16);

                TextView textView = new TextView(parent.getContext());
                textView.setLayoutParams(new CardView.LayoutParams(
                        CardView.LayoutParams.MATCH_PARENT,
                        CardView.LayoutParams.WRAP_CONTENT));
                textView.setId(View.generateViewId());
                cardView.addView(textView);

                return new OrderViewHolder(cardView);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
            Order order = orders.get(position);

            // Thiết lập dữ liệu cho ViewHolder
            if (holder.tvOrderProduct != null) {
                holder.tvOrderProduct.setText(order.getProductName());
            }

            if (holder.tvOrderDate != null) {
                holder.tvOrderDate.setText(order.getFormattedDate());
            }

            if (holder.tvOrderPrice != null) {
                holder.tvOrderPrice.setText(order.getFormattedPrice(order.getTotalPrice()));
            }

            if (holder.tvOrderQuantity != null) {
                holder.tvOrderQuantity.setText("x" + order.getQuantity());
            }

            // Sự kiện click vào đơn hàng
            holder.itemView.setOnClickListener(v -> {
                showOrderDetails(order);
            });
        }

        @Override
        public int getItemCount() {
            return orders != null ? orders.size() : 0;
        }

        // ViewHolder cho item đơn hàng
        class OrderViewHolder extends RecyclerView.ViewHolder {
            TextView tvOrderProduct, tvOrderDate, tvOrderPrice, tvOrderQuantity;
            ImageView imgOrderProduct;

            public OrderViewHolder(@NonNull View itemView) {
                super(itemView);

                // Tìm các view trong layout item_order
                tvOrderProduct = itemView.findViewById(R.id.tvOrderProduct);
                tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
                tvOrderPrice = itemView.findViewById(R.id.tvOrderPrice);
                tvOrderQuantity = itemView.findViewById(R.id.tvOrderQuantity);
                imgOrderProduct = itemView.findViewById(R.id.imgOrderProduct);

                // Thiết lập hình ảnh sản phẩm
                if (imgOrderProduct != null) {
                    try {
                        imgOrderProduct.setImageResource(R.drawable.phone_sample);
                    } catch (Resources.NotFoundException e) {
                        imgOrderProduct.setImageResource(android.R.drawable.ic_menu_gallery);
                    }
                }
            }
        }
    }

    // Hiển thị chi tiết đơn hàng khi người dùng click vào một đơn hàng
    private void showOrderDetails(Order order) {
        // Tạo dialog hiển thị thông tin chi tiết đơn hàng
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Order Details");

        // Tạo message chi tiết
        StringBuilder details = new StringBuilder();
        details.append("Order Date: ").append(order.getFormattedDate()).append("\n\n");
        details.append("Product: ").append(order.getProductName()).append("\n");
        details.append("Quantity: ").append(order.getQuantity()).append("\n");
        details.append("Unit Price: ").append(order.getFormattedPrice(order.getUnitPrice())).append("\n");
        details.append("Voucher: ").append(order.isHasDiscount() ? "-20%" : "0%").append("\n");
        details.append("Total Price: ").append(order.getFormattedPrice(order.getTotalPrice())).append("\n\n");

        details.append("Shipping Information:\n");
        details.append("  • Name: ").append(order.getCustomerName()).append("\n");
        details.append("  • Address: ").append(order.getCustomerAddress()).append("\n");

        // Thêm ghi chú nếu có
        if (order.getNotes() != null && !order.getNotes().isEmpty()) {
            details.append("  • Notes: ").append(order.getNotes()).append("\n");
        }

        builder.setMessage(details.toString());
        builder.setPositiveButton("Close", null);
        builder.show();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}