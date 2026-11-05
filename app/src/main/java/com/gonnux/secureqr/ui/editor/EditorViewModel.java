package com.gonnux.secureqr.ui.editor;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class EditorViewModel extends ViewModel {

    private MutableLiveData<Boolean> secureQrMode;
    private MutableLiveData<String> inputData;
    private MutableLiveData<String> outputData;

    public EditorViewModel() {
        secureQrMode = new MutableLiveData<>();
        secureQrMode.setValue(false);
        inputData = new MutableLiveData<>();
        outputData = new MutableLiveData<>();
    }

    public LiveData<Boolean> getSecureQrMode() {
        return secureQrMode;
    }

    public void setSecureQrMode(Boolean value) {
        secureQrMode.setValue(value);
    }

    public LiveData<String> getInputData() {
        return inputData;
    }

    public void setInputData(String value) {
        inputData.setValue(value);
    }

    public LiveData<String> getOutputData() {
        return outputData;
    }

    public void setOutputData(String value) {
        outputData.setValue(value);
    }
}
