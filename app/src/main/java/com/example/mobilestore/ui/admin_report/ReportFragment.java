package com.example.mobilestore.ui.admin_report;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
    private TextView totalSoldText;
    private TextView monthlyIncomeText, totalProfitTileText;
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
        updateValues();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize the TextView
        categoryValueText = view.findViewById(R.id.productCategoryValue);
        phoneCountText = view.findViewById(R.id.totalProductValue);
        totalSoldText = view.findViewById(R.id.totalSoldValue);
        monthlyIncomeText = view.findViewById(R.id.monthlyIncomeValue);
        totalProfitTileText = view.findViewById(R.id.totalProfitTitle);

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
    public void onDataChanged() {
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

            // update profit data o day
            SQLiteDatabase db = repository.db;
            Cursor cursor = db.query("profit", null, null, null,
                    null, null, null);

            if (cursor.moveToFirst()) {
                if (totalSoldText != null) {
                    int totalSold = cursor.getInt(
                            cursor.getColumnIndexOrThrow("total_sold"));
                    totalSoldText.setText(String.valueOf(totalSold));
                }

                if (monthlyIncomeText != null) {
                    double income = cursor.getDouble(
                            cursor.getColumnIndexOrThrow("income"));
                    String formattedIncome = String.format("%,.0f", income);
                    monthlyIncomeText.setText(income/1000000 + "M đ");
                    totalProfitTileText.setText("VND " + formattedIncome);
                }
            }
            cursor.close();
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
