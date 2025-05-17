package com.example.mobilestore.ui.admin_customer_bill;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilestore.R;
import com.example.mobilestore.adapter.CustomerAdapter;
import com.example.mobilestore.data.customer.CustomerDataManager;
import com.example.mobilestore.data.customer.CustomerDetails;
import com.example.mobilestore.data.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;

public class CustomerListFragment extends Fragment {
    private RecyclerView recyclerView;
    private CustomerAdapter adapter;
    private ProductRepository repository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_customer_list, container, false);

        repository = ProductRepository.getInstance(requireContext());

        recyclerView = view.findViewById(R.id.recycler_view_customer);

        setupRecyclerView();
        return view;
    }

    private void setupRecyclerView() {
        adapter = new CustomerAdapter(getCustomersFromDatabase());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private List<CustomerDetails> getCustomersFromDatabase() {
        List<CustomerDetails> customers = new ArrayList<>();
        SQLiteDatabase db = repository.db;

        Cursor cursor = db.query(
                "customers",
                null,  // Get all columns
                null, null, null, null,
                "created_at DESC" // Sort by newest first
        );

        if (cursor.moveToFirst()) {
            do {
                CustomerDetails customer = new CustomerDetails(
                        cursor.getString(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                        cursor.getString(cursor.getColumnIndexOrThrow("email")),
                        cursor.getString(cursor.getColumnIndexOrThrow("address")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("total_orders")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("total_spent")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("loyalty_points"))
                );
                customers.add(customer);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return customers;
    }

}
