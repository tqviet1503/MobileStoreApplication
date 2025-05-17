package com.example.mobilestore.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mobilestore.R;
import com.example.mobilestore.bill.BillDetails;
import com.example.mobilestore.model.Phone;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class BillAdapter extends RecyclerView.Adapter<BillAdapter.ViewHolder> {
    private List<BillDetails> bills;
    private final OnBillClickListener listener;

    public static interface OnBillClickListener {
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

        ViewHolder(View itemView) {
            super(itemView);
            billIdText = itemView.findViewById(R.id.tvId);
        }

        void bind(final BillDetails bill, final OnBillClickListener listener) {
            billIdText.setText(String.valueOf(bill.getBillId()));
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBillClick(bill);
                }
            });
        }
    }
}