package com.example.mobilestore.adapter;

import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mobilestore.R;
import com.example.mobilestore.bill.BillDetails;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BillAdapter extends RecyclerView.Adapter<BillAdapter.ViewHolder> {
    private static final String TAG = "BillAdapter";
    private List<BillDetails> bills;
    private final OnBillClickListener listener;

    public interface OnBillClickListener {
        void onBillClick(BillDetails bill);
    }

    public BillAdapter(List<BillDetails> bills, BillAdapter.OnBillClickListener listener) {
        this.bills = bills;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_item_bill, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BillDetails bill = bills.get(position);
        holder.bind(bill, listener);
    }

    @Override
    public int getItemCount() {
        return bills != null ? bills.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView billIdText;
        private final TextView billItemsText;  // Added to show multiple items indicator
        private final TextView billTotalText;  // Added to show total amount
        private final TextView billDateText;   // Added to show date
        private final TextView billDiscountText; // Added to show discount info
        private final ImageView billImageView; // Added to show icon for multiple/single items

        ViewHolder(View itemView) {
            super(itemView);
            billIdText = itemView.findViewById(R.id.tvId);

            // These might be optional depending on your admin_item_bill layout
            billItemsText = itemView.findViewById(R.id.tvItems);
            billTotalText = itemView.findViewById(R.id.tvTotal);
            billDateText = itemView.findViewById(R.id.tvDate);
            billDiscountText = itemView.findViewById(R.id.tvDiscount); // Thêm textview hiển thị discount
            billImageView = itemView.findViewById(R.id.ivBillIcon);
        }

        void bind(final BillDetails bill, final OnBillClickListener listener) {
            try {
                // Set bill ID - Sử dụng ID đã được định dạng
                billIdText.setText(bill.getFormattedBillId());

                // Log for debugging
                Log.d(TAG, "Binding bill: " + bill.getBillId() + ", Formatted: " + bill.getFormattedBillId());

                // Set date if view exists
                if (billDateText != null) {
                    try {
                        billDateText.setText(bill.getFormattedDate());
                        Log.d(TAG, "Set date: " + bill.getFormattedDate() + " for bill ID: " + bill.getBillId());
                    } catch (Exception e) {
                        Log.e(TAG, "Error setting date: " + e.getMessage(), e);
                        // Fallback to simple format
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        billDateText.setText(sdf.format(new Date()));
                    }
                }

                // Set items info if view exists
                if (billItemsText != null) {
                    if (bill.hasMultipleItems()) {
                        // Get real quantity from all items
                        int totalQuantity = bill.getTotalQuantity();
                        billItemsText.setText("Multiple items (" + totalQuantity + ")");
                        Log.d(TAG, "Multiple items: " + totalQuantity + " for bill ID: " + bill.getBillId());
                    } else {
                        // Try to get the first item name if it exists
                        String productName = bill.getItems().isEmpty() ?
                                "Unknown product" : bill.getItems().get(0).getPhoneName();
                        billItemsText.setText(productName);
                        Log.d(TAG, "Single item: " + productName + " for bill ID: " + bill.getBillId());
                    }
                }

                // Set discount info if available
                if (billDiscountText != null) {
                    if (bill.hasDiscount()) {
                        billDiscountText.setVisibility(View.VISIBLE);
                        billDiscountText.setText(bill.getFormattedDiscount());
                        billDiscountText.setTextColor(ContextCompat.getColor(itemView.getContext(),
                                android.R.color.holo_green_dark));

                        Log.d(TAG, "Discount: " + bill.getFormattedDiscount() +
                                " for bill ID: " + bill.getBillId());
                    } else {
                        billDiscountText.setVisibility(View.GONE);
                    }
                }

                // Set total price if view exists
                if (billTotalText != null) {
                    // Sử dụng giá đã áp dụng giảm giá từ bill
                    double total = bill.getTotalAmount();

                    // Hiển thị total price
                    if (bill.hasDiscount()) {
                        // Nếu có giảm giá, hiển thị tổng tiền với màu khác (nếu muốn)
                        billTotalText.setText(String.format(Locale.getDefault(), "%,.0fđ", total));
                        // Có thể thêm ghi chú về giá gốc nếu muốn
                        // billTotalText.setText(String.format(Locale.getDefault(), "%,.0fđ (-%,.0fđ)",
                        //         total, bill.getDiscountAmount()));
                    } else {
                        billTotalText.setText(String.format(Locale.getDefault(), "%,.0fđ", total));
                    }

                    Log.d(TAG, "Total amount: " + total + " for bill ID: " + bill.getBillId());
                    if (bill.hasDiscount()) {
                        Log.d(TAG, "Original amount: " + bill.calculateSubtotal() +
                                ", Discount: " + bill.getDiscountAmount() +
                                " for bill ID: " + bill.getBillId());
                    }
                }

                // Set appropriate icon if view exists
                if (billImageView != null) {
                    try {
                        if (bill.hasMultipleItems()) {
                            // Try to use a cart icon or other multi-item icon
                            try {
                                billImageView.setImageResource(R.drawable.ic_shopping_cart);
                            } catch (Resources.NotFoundException e) {
                                Log.w(TAG, "Shopping cart icon not found, trying fallback");
                                try {
                                    // Use Android's built-in more icon as a fallback
                                    billImageView.setImageResource(android.R.drawable.ic_menu_more);
                                } catch (Resources.NotFoundException e2) {
                                    // Final fallback
                                    billImageView.setImageResource(android.R.drawable.ic_menu_gallery);
                                }
                            }
                        } else {
                            // Try to use a phone icon for single items
                            try {
                                billImageView.setImageResource(R.drawable.phone_sample);
                            } catch (Resources.NotFoundException e) {
                                billImageView.setImageResource(android.R.drawable.ic_menu_gallery);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error setting icon: " + e.getMessage(), e);
                        // Ignore if we can't set the image
                    }
                }

                // Set click listener
                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onBillClick(bill);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error binding bill: " + e.getMessage(), e);
            }
        }
    }
}