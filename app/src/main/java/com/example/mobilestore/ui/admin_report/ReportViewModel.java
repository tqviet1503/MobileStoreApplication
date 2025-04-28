package com.example.mobilestore.ui.admin_report;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import java.util.ArrayList;

public class ReportViewModel extends ViewModel {
    private MutableLiveData<List<String>> reports = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public ReportViewModel() {
        reports.setValue(new ArrayList<>());
    }

    public LiveData<List<String>> getReports() {
        return reports;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void loadReports() {
        isLoading.setValue(true);
        // TODO: Implement actual data loading from repository
        
    }
}
