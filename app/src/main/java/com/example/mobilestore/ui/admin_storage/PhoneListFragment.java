package com.example.mobilestore.ui.admin_storage;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilestore.R;
import com.example.mobilestore.adapter.PhoneAdapter;
import com.example.mobilestore.data.repository.ProductRepository;
import com.example.mobilestore.model.Phone;

import java.util.List;

public class PhoneListFragment extends Fragment implements PhoneAdapter.OnPhoneClickListener {
    private RecyclerView recyclerView;
    private ProductRepository repository;
    private String brandName;
    private PhoneAdapter adapter;
    private TextView bigBrandName;

    public static PhoneListFragment newInstance(String brandName) {
        PhoneListFragment fragment = new PhoneListFragment();
        Bundle args = new Bundle();
        args.putString("brandName", brandName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = ProductRepository.getInstance(requireContext());
        brandName = getArguments().getString("brandName");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_phone_list, container, false);

        // Setup RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view_phones);
        bigBrandName = view.findViewById(R.id.bigBrandName);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Load phones and setup adapter
        List<Phone> phones = repository.getPhonesForBrand(brandName);
        adapter = new PhoneAdapter(phones, this);
        recyclerView.setAdapter(adapter);
        bigBrandName.setText(brandName);

        return view;
    }

    @Override
    public void onPhoneClick(Phone phone) {
        showPhoneDetailsDialog(phone);
    }

    @Override
    public void onPhoneAdjustClick(Phone phone) {
        showAdjustPhoneDialog(phone);
    }

    private void showAdjustPhoneDialog(Phone phone) {
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_adjust_phone, null);

        EditText edtPrice = dialogView.findViewById(R.id.adjust_price);
        EditText edtStock = dialogView.findViewById(R.id.adjust_stock);
        EditText edtBattery = dialogView.findViewById(R.id.adjust_battery);
        EditText edtRam = dialogView.findViewById(R.id.adjust_ram);
        EditText edtStorage = dialogView.findViewById(R.id.adjust_storage);
        EditText edtModel = dialogView.findViewById(R.id.adjust_model);
        EditText edtCpu = dialogView.findViewById(R.id.adjust_cpu);

        edtPrice.setText(String.valueOf(phone.getPrice()));
        edtStock.setText(String.valueOf(phone.getStockQuantity()));
        edtBattery.setText(String.valueOf(phone.getPerformance().getBatteryCapacity()));
        edtRam.setText(String.valueOf(phone.getPerformance().getRamGB()));
        edtStorage.setText(String.valueOf(phone.getPerformance().getStorageGB()));
        edtModel.setText(phone.getModel());
        edtCpu.setText(phone.getPerformance().getProcessor());

        new AlertDialog.Builder(requireContext())
                .setTitle("Adjust Phone")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String priceStr = edtPrice.getText().toString().trim();
                    String stockStr = edtStock.getText().toString().trim();
                    String batteryStr = edtBattery.getText().toString().trim();
                    String ramStr = edtRam.getText().toString().trim();
                    String storageStr = edtStorage.getText().toString().trim();
                    String modelStr = edtModel.getText().toString().trim();
                    String cpuStr = edtCpu.getText().toString().trim();

                    double newPrice = phone.getPrice();
                    int newStock = phone.getStockQuantity();

                    if (!priceStr.isEmpty()) {
                        newPrice = Double.parseDouble(priceStr);
                    }
                    if (!stockStr.isEmpty()) {
                        newStock = Integer.parseInt(stockStr);
                    }
                    if (!batteryStr.isEmpty()) {
                        phone.getPerformance().setBatteryCapacity(String.valueOf(Integer.parseInt(batteryStr)));
                    }
                    if (!ramStr.isEmpty()) {
                        phone.getPerformance().setRamGB(Integer.parseInt(ramStr));
                    }
                    if (!storageStr.isEmpty()) {
                        phone.getPerformance().setStorageGB(Integer.parseInt(storageStr));
                    }
                    if (!modelStr.isEmpty()) {
                        phone.setModel(modelStr);
                    }
                    if (!cpuStr.isEmpty()) {
                        phone.getPerformance().setProcessor(cpuStr);
                    }
                    // Update database
                    repository.updateAdjustedPhoneInfo(phone.getId(), newPrice, newStock, modelStr, cpuStr, Integer.parseInt(ramStr), Integer.parseInt(storageStr), batteryStr);

                    // Reload list
                    List<Phone> updatedPhones = repository.getPhonesForBrand(brandName);
                    adapter.setPhones(updatedPhones);
                    adapter.notifyDataSetChanged();
                })
                .setNegativeButton("Cancel", null)
                .setNeutralButton("Remove", (dialog, which) -> {
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Confirm Remove")
                            .setMessage("Are you sure you want to remove this phone?")
                            .setPositiveButton("Yes", (confirmDialog, confirmWhich) -> {
                                String phoneId = phone.getId();
                                repository.removePhoneById(phoneId);
                                adapter.removePhoneById(phoneId);
                            })
                            .setNegativeButton("No", null)
                            .show();
                })
                .show();
    }

    private void showPhoneDetailsDialog(Phone phone) {
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.admin_phone_detail_dialog, null);

        // Set phone details
        ((TextView) dialogView.findViewById(R.id.text_phone_name))
                .setText(phone.getPhoneName());
//        ((TextView) dialogView.findViewById(R.id.text_brand))
//                .setText(phone.getBrand());
        ((TextView) dialogView.findViewById(R.id.text_model))
                .setText("Model: " + phone.getModel());
        ((TextView) dialogView.findViewById(R.id.text_price))
                .setText("Price: " + String.format("%,.0fđ", phone.getPrice()));
        ((TextView) dialogView.findViewById(R.id.text_processor))
                .setText("CPU: " + phone.getPerformance().getProcessor());
        ((TextView) dialogView.findViewById(R.id.text_ram))
                .setText("RAM: " + phone.getPerformance().getRamGB() + "GB");
        ((TextView) dialogView.findViewById(R.id.text_storage))
                .setText("Storage: " + phone.getPerformance().getStorageGB() + "GB");
        ((TextView) dialogView.findViewById(R.id.text_battery))
                .setText("Battery: " + phone.getPerformance().getBatteryCapacity() + "mAh");
        ((TextView) dialogView.findViewById(R.id.text_stock))
                .setText("In Stock: " + phone.getStockQuantity());

        new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }
}
