package com.example.signalsense;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.signalsense.data.CpuGridItem;

import java.util.List;

public class CpuInfoViewModel extends ViewModel {
    private MutableLiveData<Integer> overallCpuUsageLiveData = new MutableLiveData<>();
    private MutableLiveData<String> cpuTempLiveData = new MutableLiveData<>();
    private MutableLiveData<String> totalCoresLiveData = new MutableLiveData<>();
    private MutableLiveData<List<CpuGridItem>> cpuGridItemsLiveData = new MutableLiveData<>();

    public LiveData<Integer> getOverallCpuUsage() {
        return overallCpuUsageLiveData;
    }

    public LiveData<String> getCpuTemp() {
        return cpuTempLiveData;
    }

    public LiveData<String> getTotalCores() {
        return totalCoresLiveData;
    }

    public LiveData<List<CpuGridItem>> getCpuGridItems() {
        return cpuGridItemsLiveData;
    }

    public void updateCpuInfo(int overallCpuUsage, String cpuTemp, String totalCores, List<CpuGridItem> gridItems) {
        overallCpuUsageLiveData.postValue(overallCpuUsage);
        cpuTempLiveData.postValue(cpuTemp);
        totalCoresLiveData.postValue(totalCores);
        cpuGridItemsLiveData.postValue(gridItems);
    }
}
