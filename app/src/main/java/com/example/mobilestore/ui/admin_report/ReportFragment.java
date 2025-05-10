package com.example.mobilestore.ui.admin_report;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.mobilestore.R;
import com.example.mobilestore.data.repository.ProductRepository;
import com.example.mobilestore.model.Brand;
import com.example.mobilestore.model.Phone;

public class ReportFragment extends Fragment implements ProductRepository.OnDataChangeListener{
    private ReportViewModel viewModel;
    private ProductRepository repository;
    private TextView categoryValueText;
    private TextView phoneCountText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_report, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = ProductRepository.getInstance(requireContext());
        repository.addListener(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize the TextView
        categoryValueText = view.findViewById(R.id.productCategoryValue);
        phoneCountText = view.findViewById(R.id.totalProductValue);
        // Update initial value
        updateValues();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        repository.removeListener(this);
    }

    @Override
    public void onBrandAdded(Brand brand) {
        updateValues();
    }

    @Override
    public void onPhoneAdded(Phone phone) {
        updateValues();
    }
    private void updateValues() {
        if (getActivity() == null) return;

        getActivity().runOnUiThread(() -> {
            if (categoryValueText != null) {
                int totalBrands = repository.getAllBrands().size();
                categoryValueText.setText(String.valueOf(totalBrands));
            }

            if (phoneCountText != null) {
                int totalPhones = getTotalPhones();
                phoneCountText.setText(String.valueOf(totalPhones));
            }
        });
    }

    private int getTotalPhones() {
        int total = 0;
        for (Brand brand : repository.getAllBrands()) {
            total += brand.getPhoneCount();
        }
        return total;
    }
}
