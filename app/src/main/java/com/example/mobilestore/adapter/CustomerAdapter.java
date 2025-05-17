package com.example.mobilestore.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilestore.R;
import com.example.mobilestore.data.customer.CustomerDetails;

import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.ViewHolder> {
    private final List<CustomerDetails> customers;

    public CustomerAdapter(List<CustomerDetails> customers) {
        this.customers = customers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_customer_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CustomerDetails customer = customers.get(position);
        holder.bind(customer);
    }

    @Override
    public int getItemCount() {
        return customers.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView nameText;
        final TextView idText;
        final TextView phoneText;
        final TextView emailText;
        final TextView addressText;
        final TextView statsText;

        ViewHolder(View view) {
            super(view);
            nameText = view.findViewById(R.id.customer_name);
            idText = view.findViewById(R.id.customer_id);
            phoneText = view.findViewById(R.id.customer_phone);
            emailText = view.findViewById(R.id.customer_email);
            addressText = view.findViewById(R.id.customer_address);
            statsText = view.findViewById(R.id.customer_stats);
        }

        void bind(CustomerDetails customer) {
            nameText.setText(customer.name);
            idText.setText(customer.id);
            phoneText.setText(customer.phone);
            emailText.setText(customer.email);
            addressText.setText(customer.address);

            String stats = String.format("Orders: %d | Spent: %,.0fđ | Points: %d",
                    customer.totalOrders,
                    customer.totalSpent,
                    customer.loyaltyPoints);
            statsText.setText(stats);
        }
    }
}
