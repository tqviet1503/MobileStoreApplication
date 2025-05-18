package com.example.mobilestore.ui.admin_storage;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilestore.data.repository.ProductRepository;
import com.example.mobilestore.model.Phone;
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
        repository = ProductRepository.getInstance(requireContext());
        repository.addListener(new ProductRepository.OnDataChangeListener() {
            @Override
            public void onBrandAdded(Brand brand) { }

            @Override
            public void onPhoneAdded(Phone phone) { }

            @Override
            public void onDataChanged() {
                updateTotalProductValue();
            }
        });
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
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, PhoneListFragment.newInstance(brand.getName()))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onAddPhoneClick(Brand brand) {
        showAddPhoneDialog(brand);
    }

    private void showAddPhoneDialog(Brand brand) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.phone_input_dialog, null);

        EditText phoneNameEdit = dialogView.findViewById(R.id.edit_phone_name);
        EditText modelEdit = dialogView.findViewById(R.id.edit_model);
        EditText priceEdit = dialogView.findViewById(R.id.edit_price);
        EditText processorEdit = dialogView.findViewById(R.id.edit_processor);
        EditText ramEdit = dialogView.findViewById(R.id.edit_ram);
        EditText storageEdit = dialogView.findViewById(R.id.edit_storage);
        EditText batteryEdit = dialogView.findViewById(R.id.edit_battery);
        EditText stockEdit = dialogView.findViewById(R.id.edit_stock);

        new AlertDialog.Builder(requireContext())
                .setTitle("Add New Phone")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    try {
                        if (!validateInputs(phoneNameEdit, modelEdit, priceEdit,
                                processorEdit, ramEdit, storageEdit, batteryEdit, stockEdit)) {
                            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String phoneName = phoneNameEdit.getText().toString().trim();
                        String model = modelEdit.getText().toString().trim();
                        double price = Double.parseDouble(priceEdit.getText().toString());
                        String processor = processorEdit.getText().toString().trim();
                        int ram = Integer.parseInt(ramEdit.getText().toString());
                        int storage = Integer.parseInt(storageEdit.getText().toString());
                        String battery = batteryEdit.getText().toString().trim(); // Get battery value
                        int stock = Integer.parseInt(stockEdit.getText().toString());

                        Phone.PhonePerformance performance = new Phone.PhonePerformance(
                                processor,
                                ram,
                                storage,
                                battery
                        );

                        Phone newPhone = new Phone(
                                String.valueOf(System.currentTimeMillis()),
                                model,
                                brand.getName(),
                                price,
                                performance,
                                phoneName,
                                stock
                        );

                        repository.addPhone(brand.getName(), newPhone);
                        adapter.updateBrands(repository.getAllBrands());
                        updateTotalProductValue();

                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Please enter valid numbers", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private boolean validateInputs(EditText... inputs) {
        for (EditText input : inputs) {
            if (input.getText().toString().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
