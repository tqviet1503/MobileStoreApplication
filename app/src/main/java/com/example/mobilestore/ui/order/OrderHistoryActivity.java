package com.example.mobilestore.ui.order;

import android.app.Dialog;
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
                // Display appropriate text based on single or multiple items
                if (order.hasMultipleItems()) {
                    holder.tvOrderProduct.setText("Multiple items (" + order.getItemCount() + ")");
                } else {
                    holder.tvOrderProduct.setText(order.getProductName());
                }
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

            // Set appropriate icon based on single or multiple items
            if (holder.imgOrderProduct != null) {
                try {
                    if (order.hasMultipleItems()) {
                        // Use a cart or multiple items icon for orders with multiple products
                        holder.imgOrderProduct.setImageResource(R.drawable.ic_shopping_cart);
                    } else {
                        // Use standard product image for single item orders
                        holder.imgOrderProduct.setImageResource(R.drawable.phone_sample);
                    }
                } catch (Resources.NotFoundException e) {
                    holder.imgOrderProduct.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            }

            // Sự kiện click vào đơn hàng
            holder.itemView.setOnClickListener(v -> {
                if (order.hasMultipleItems()) {
                    showMultiItemOrderDetails(order);
                } else {
                    showSingleItemOrderDetails(order);
                }
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

    // Hiển thị chi tiết đơn hàng đơn lẻ
    private void showSingleItemOrderDetails(Order order) {
        // Tạo dialog hiển thị thông tin chi tiết đơn hàng
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Order Details");

        // Tạo message chi tiết
        StringBuilder details = new StringBuilder();
        details.append("Order ID: ").append(order.getOrderId()).append("\n");
        details.append("Order Date: ").append(order.getFormattedDate()).append("\n\n");

        details.append("  • Product: ").append(order.getProductName()).append("\n");
        details.append("  • Quantity: ").append(order.getQuantity()).append("\n");
        details.append("  • Price: ").append(order.getFormattedPrice(order.getUnitPrice())).append("\n");
        details.append("  • Voucher: ").append(order.isHasDiscount() ? "-20%" : "0%").append("\n");
        details.append("  • Total Price: ").append(order.getFormattedPrice(order.getTotalPrice())).append("\n\n");

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

    // Hiển thị chi tiết đơn hàng nhiều sản phẩm với RecyclerView
    private void showMultiItemOrderDetails(Order order) {
        try {
            // Create custom dialog
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_order_details);

            // Set dialog title
            TextView tvDialogTitle = dialog.findViewById(R.id.tvDialogTitle);
            tvDialogTitle.setText("Order Details");

            // Set order header information
            TextView tvOrderId = dialog.findViewById(R.id.tvOrderId);
            tvOrderId.setText("Order ID: " + order.getOrderId());

            TextView tvOrderDate = dialog.findViewById(R.id.tvOrderDate);
            tvOrderDate.setText("Date: " + order.getFormattedDate());

            // Setup RecyclerView for order items
            RecyclerView rvOrderItems = dialog.findViewById(R.id.rvOrderItems);
            rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
            OrderItemAdapter itemAdapter = new OrderItemAdapter(order.getOrderItems());
            rvOrderItems.setAdapter(itemAdapter);

            // Set order summary information
            TextView tvDiscount = dialog.findViewById(R.id.tvDiscount);
            tvDiscount.setText("Discount: " + (order.isHasDiscount() ? "20%" : "0%"));

            TextView tvTotalPrice = dialog.findViewById(R.id.tvTotalPrice);
            tvTotalPrice.setText("Total: " + order.getFormattedPrice(order.getTotalPrice()));

            // Set shipping information
            TextView tvShippingInfo = dialog.findViewById(R.id.tvShippingInfo);
            StringBuilder shippingInfo = new StringBuilder("Shipping Information:\n");
            shippingInfo.append("  • Name: ").append(order.getCustomerName()).append("\n");
            shippingInfo.append("  • Address: ").append(order.getCustomerAddress()).append("\n");

            if (order.getNotes() != null && !order.getNotes().isEmpty()) {
                shippingInfo.append("  • Notes: ").append(order.getNotes());
            }

            tvShippingInfo.setText(shippingInfo.toString());

            // Set close button
            Button btnClose = dialog.findViewById(R.id.btnClose);
            btnClose.setOnClickListener(v -> dialog.dismiss());

            // Show dialog
            dialog.show();
        } catch (Resources.NotFoundException e) {
            // Fall back to simple AlertDialog if custom layout not found
            showSimpleMultiItemDialog(order);
        }
    }

    // Fallback method to show multi-item order details in a simple alert dialog
    private void showSimpleMultiItemDialog(Order order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Order Details");

        StringBuilder details = new StringBuilder();
        details.append("Order ID: ").append(order.getOrderId()).append("\n");
        details.append("Order Date: ").append(order.getFormattedDate()).append("\n\n");

        details.append("Products:\n");
        for (Order.OrderItem item : order.getOrderItems()) {
            details.append("  • ").append(item.getProductName())
                    .append(" (x").append(item.getQuantity()).append(") - ")
                    .append(order.getFormattedPrice(item.getTotalPrice()))
                    .append("\n");
        }

        details.append("\nVoucher: ").append(order.isHasDiscount() ? "-20%" : "0%").append("\n");
        details.append("Total Price: ").append(order.getFormattedPrice(order.getTotalPrice())).append("\n\n");

        details.append("Shipping Information:\n");
        details.append("  • Name: ").append(order.getCustomerName()).append("\n");
        details.append("  • Address: ").append(order.getCustomerAddress()).append("\n");

        if (order.getNotes() != null && !order.getNotes().isEmpty()) {
            details.append("  • Notes: ").append(order.getNotes()).append("\n");
        }

        builder.setMessage(details.toString());
        builder.setPositiveButton("Close", null);
        builder.show();
    }

    // Adapter for displaying order items in the detail dialog
    private class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.ItemViewHolder> {
        private List<Order.OrderItem> items;

        public OrderItemAdapter(List<Order.OrderItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_order_detail, parent, false);
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
            Order.OrderItem item = items.get(position);

            holder.tvOrderItemName.setText(item.getProductName());
            holder.tvOrderItemQuantity.setText("x" + item.getQuantity());
            holder.tvOrderItemPrice.setText(
                    String.format(java.util.Locale.getDefault(), "%,.0fđ",
                            item.getUnitPrice() * item.getQuantity()));

            // Set product image (you can add logic to choose appropriate image based on product)
            try {
                // Check product name to determine which image to use
                String productName = item.getProductName().toLowerCase();
                if (productName.contains("iphone") || productName.contains("apple")) {
                    holder.imgOrderItem.setImageResource(R.drawable.phone_sample);
                } else if (productName.contains("samsung") || productName.contains("galaxy")) {
                    holder.imgOrderItem.setImageResource(R.drawable.samsung);
                } else if (productName.contains("xiaomi") || productName.contains("redmi")) {
                    holder.imgOrderItem.setImageResource(R.drawable.redmi);
                } else {
                    holder.imgOrderItem.setImageResource(R.drawable.phone_sample);
                }
            } catch (Resources.NotFoundException e) {
                holder.imgOrderItem.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }

        @Override
        public int getItemCount() {
            return items != null ? items.size() : 0;
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {
            ImageView imgOrderItem;
            TextView tvOrderItemName, tvOrderItemQuantity, tvOrderItemPrice;

            public ItemViewHolder(@NonNull View itemView) {
                super(itemView);
                imgOrderItem = itemView.findViewById(R.id.imgOrderItem);
                tvOrderItemName = itemView.findViewById(R.id.tvOrderItemName);
                tvOrderItemQuantity = itemView.findViewById(R.id.tvOrderItemQuantity);
                tvOrderItemPrice = itemView.findViewById(R.id.tvOrderItemPrice);
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}