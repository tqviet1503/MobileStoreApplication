package com.example.mobilestore.adapter;

import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilestore.R;
import com.example.mobilestore.bill.BillDetails;

import java.util.List;
import java.util.Locale;

public class BillItemAdapter extends RecyclerView.Adapter<BillItemAdapter.ViewHolder> {
    private static final String TAG = "BillItemAdapter";
    private List<BillDetails.BillItem> items;

    public BillItemAdapter(List<BillDetails.BillItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bill_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BillDetails.BillItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView phoneNameText;     // Tên sản phẩm
        private final TextView unitPriceText;     // Đơn giá
        private final TextView quantityText;      // Số lượng
        private final TextView priceText;         // Thành tiền
        private final ImageView phoneImage;       // Hình ảnh sản phẩm

        ViewHolder(View itemView) {
            super(itemView);
            phoneNameText = itemView.findViewById(R.id.text_phone_name);
            unitPriceText = itemView.findViewById(R.id.text_unit_price); // Thêm textview cho đơn giá
            quantityText = itemView.findViewById(R.id.text_quantity);
            priceText = itemView.findViewById(R.id.text_price);
            phoneImage = itemView.findViewById(R.id.image_phone);
        }

        void bind(BillDetails.BillItem item) {
            try {
                // Product - Tên sản phẩm
                phoneNameText.setText(item.getPhoneName());

                // Price - Đơn giá của 1 sản phẩm
                unitPriceText.setText(String.format(Locale.getDefault(), "%,.0fđ", item.getUnitPrice()));

                // Quantity - Số lượng
                quantityText.setText("x" + item.getQuantity());

                // Thành tiền (đơn giá * số lượng)
                double totalPrice = item.getUnitPrice() * item.getQuantity();
                priceText.setText(String.format(Locale.getDefault(), "%,.0fđ", totalPrice));

                // Log for debugging
                Log.d(TAG, "Binding item: " + item.getPhoneName() +
                        ", Unit Price: " + item.getUnitPrice() +
                        ", Quantity: " + item.getQuantity() +
                        ", Total: " + totalPrice);

                // Set phone image based on name if possible
                try {
                    String phoneName = item.getPhoneName().toLowerCase();
                    if (phoneName.contains("iphone") || phoneName.contains("apple")) {
                        phoneImage.setImageResource(R.drawable.phone_sample);
                    } else if (phoneName.contains("samsung") || phoneName.contains("galaxy")) {
                        try {
                            phoneImage.setImageResource(R.drawable.samsung);
                        } catch (Resources.NotFoundException e) {
                            phoneImage.setImageResource(R.drawable.phone_sample);
                        }
                    } else if (phoneName.contains("xiaomi") || phoneName.contains("redmi")) {
                        try {
                            phoneImage.setImageResource(R.drawable.redmi);
                        } catch (Resources.NotFoundException e) {
                            phoneImage.setImageResource(R.drawable.phone_sample);
                        }
                    } else if (phoneName.contains("oppo")) {
                        try {
                            phoneImage.setImageResource(R.drawable.oppo);
                        } catch (Resources.NotFoundException e) {
                            phoneImage.setImageResource(R.drawable.phone_sample);
                        }
                    } else {
                        phoneImage.setImageResource(R.drawable.phone_sample);
                    }
                } catch (Resources.NotFoundException e) {
                    Log.e(TAG, "Error setting phone image: " + e.getMessage(), e);
                    phoneImage.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error binding item: " + e.getMessage(), e);
            }
        }
    }
}