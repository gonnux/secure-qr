package com.gonnux.secureqr.ui.editor;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class EditorViewModel extends ViewModel {

    private MutableLiveData<Boolean> secureQrMode;
    private MutableLiveData<String> data;
    private MutableLiveData<String> encodedData;

    public EditorViewModel() {
        secureQrMode = new MutableLiveData<>();
        secureQrMode.setValue(false);
        data = new MutableLiveData<>();
        encodedData = new MutableLiveData<>();
    }

    public LiveData<Boolean> getSecureQrMode() {
        return secureQrMode;
    }

    public void setSecureQrMode(Boolean value) {
        secureQrMode.setValue(value);
    }

    public LiveData<String> getData() {
        return data;
    }

    public void setData(String value) {
        data.setValue(value);
    }

    public LiveData<String> getEncodedData() {
        return encodedData;
    }

    public void setEncodedData(String value) {
        encodedData.setValue(value);
    }
}