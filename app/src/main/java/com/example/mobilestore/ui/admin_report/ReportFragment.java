package com.example.mobilestore.ui.admin_report;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.mobilestore.R;

public class ReportFragment extends Fragment {
    private ReportViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_report, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ReportViewModel.class);
        
        viewModel.loadReports();
        viewModel.getReports().observe(getViewLifecycleOwner(), reports -> {
            // TODO: Update UI with reports
        });
    }
}
