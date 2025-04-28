package com.example.mobilestore.ui.admin_storage;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilestore.data.repository.ProductRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.mobilestore.R;
import com.example.mobilestore.adapter.BrandAdapter;
import com.example.mobilestore.model.Brand;

public class StorageFragment extends Fragment implements BrandAdapter.OnBrandClickListener {

    private RecyclerView recyclerView;
    private ProductRepository repository;
    private BrandAdapter adapter;
    private TextView totalProductValueText;
    private FloatingActionButton fabAddBrand;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = ProductRepository.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_storage, container, false);

        // Find views
        recyclerView = view.findViewById(R.id.recycler_view_brands);
        totalProductValueText = view.findViewById(R.id.totalProductValue);
        fabAddBrand = view.findViewById(R.id.fab_add_brand);

        setupRecyclerView();
        setupFab();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateTotalProductValue(); // update ngay khi giao diện xong
    }

    private void setupRecyclerView() {
        adapter = new BrandAdapter(repository.getAllBrands(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupFab() {
        fabAddBrand.setOnClickListener(v -> showAddBrandDialog());
    }

    private void showAddBrandDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.add_brand, null);
        EditText brandNameEdit = dialogView.findViewById(R.id.edit_brand_name);

        new AlertDialog.Builder(requireContext())
                .setTitle("Add New Brand")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String brandName = brandNameEdit.getText().toString().trim();
                    if (!brandName.isEmpty()) {
                        Brand newBrand = new Brand(brandName, R.drawable.ic_phone, 0);
                        repository.addBrand(newBrand);
                        adapter.updateBrands(repository.getAllBrands());
                        updateTotalProductValue(); // cập nhật số lượng brand
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateTotalProductValue() {
        if (totalProductValueText != null) {
            int totalBrands = repository.getAllBrands().size();
            String displayText = totalBrands + " Products";
            totalProductValueText.setText(displayText);
        }
    }

    @Override
    public void onBrandClick(Brand brand) {
        // Xử lý khi click vào một brand
    }
}
