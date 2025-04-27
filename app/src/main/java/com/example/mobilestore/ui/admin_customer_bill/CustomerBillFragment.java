package com.example.mobilestore.ui.admin_customer_bill;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilestore.R;
import com.example.mobilestore.adapter.BrandAdapter;
import com.example.mobilestore.model.Brand;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class CustomerBillFragment extends Fragment implements BrandAdapter.OnBrandClickListener{
    private CustomerBillViewModel viewModel;
    private BrandAdapter adapter;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAddBrand;
    private List<Brand> listView = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //currently setup here, need to change later
        //change base on database, not static
        listView = new ArrayList<>();
        listView.add(new Brand("Customers", R.drawable.ic_customer, 10));
        listView.add(new Brand("Bills", R.drawable.ic_bill, 15));
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_customer_bill, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_brands);
        setupRecyclerView();
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // TODO: implement base on StorageFragment when finish database
    }

    private void setupRecyclerView() {
        adapter = new BrandAdapter(listView, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onBrandClick(Brand brand) {
        // Xử lý khi click vào một brand
    }
}
