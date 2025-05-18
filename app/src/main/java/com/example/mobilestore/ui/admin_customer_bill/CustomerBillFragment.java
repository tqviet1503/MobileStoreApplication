package com.example.mobilestore.ui.admin_customer_bill;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import com.example.mobilestore.data.repository.ProductRepository;
import com.example.mobilestore.model.Brand;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class CustomerBillFragment extends Fragment implements BrandAdapter.OnBrandClickListener{
    private CustomerBillViewModel viewModel;
    private BrandAdapter adapter;
    private RecyclerView recyclerView;
    private ProductRepository repository;
    private FloatingActionButton fabAddBrand;
    private List<Brand> listView = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = ProductRepository.getInstance(requireContext());
        updateListView();
    }

    private void updateListView() {
        SQLiteDatabase db = repository.db;
        listView.clear();

        // truy van customer
        Cursor customerCursor = db.query("customers",
                new String[]{"COUNT(*) as count"},
                null, null, null, null, null);
        int customerCount = 0;
        if (customerCursor.moveToFirst()) {
            customerCount = customerCursor.getInt(0);
        }
        customerCursor.close();

        // truy van bills
        Cursor billCursor = db.query("bills",
                new String[]{"COUNT(*) as count"},
                null, null, null, null, null);
        int billCount = 0;
        if (billCursor.moveToFirst()) {
            billCount = billCursor.getInt(0);
        }
        billCursor.close();

        // add item vao listview
        listView.add(new Brand("Customers", R.drawable.ic_customer, customerCount));
        listView.add(new Brand("Bills", R.drawable.ic_bill, billCount));
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_customer_bill, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_customer_bill);
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
        if (brand.getName().equals("Customers")) {
            Fragment customerListFragment = new CustomerListFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, customerListFragment)
                    .addToBackStack(null)
                    .commit();
        } else if (brand.getName().equals("Bills")) {
            Fragment billListFragment = new BillListFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, billListFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onAddPhoneClick(Brand brand) {
    }
}
