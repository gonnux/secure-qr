package com.gonnux.secureqr.ui.editor;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class EditorViewModel extends ViewModel {

    private MutableLiveData<String> data;
    private MutableLiveData<Boolean> secureQrMode;

    public EditorViewModel() {
        data = new MutableLiveData<>();
        data.setValue("SecureQR");
        secureQrMode = new MutableLiveData<>();
        secureQrMode.setValue(false);
    }

    public LiveData<String> getData() {
        return data;
    }

    public void setData(String value) {
        data.setValue(value);
    }

    public LiveData<Boolean> getSecureQrMode() {
        return secureQrMode;
    }

    public void setSecureQrMode(Boolean value) {
        secureQrMode.setValue(value);
    }
}