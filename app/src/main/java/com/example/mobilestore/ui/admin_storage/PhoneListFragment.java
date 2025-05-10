package com.example.mobilestore.ui.admin_storage;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
