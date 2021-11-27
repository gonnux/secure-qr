package com.example.secureqr.ui.editor;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class EditorViewModel extends ViewModel {

    private MutableLiveData<String> data;

    public EditorViewModel() {
        data = new MutableLiveData<>();
        data.setValue("SecureQR");
    }

    public void setData(String value) {
        data.setValue(value);
    }

    public LiveData<String> getData() {
        return data;
    }
}