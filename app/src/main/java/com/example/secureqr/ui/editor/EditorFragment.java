package com.example.secureqr.ui.editor;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.secureqr.databinding.FragmentEditorBinding;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class EditorFragment extends Fragment {

    private EditorViewModel editorViewModel;
    private FragmentEditorBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        editorViewModel =
                new ViewModelProvider(requireActivity()).get(EditorViewModel.class);

        binding = FragmentEditorBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        editorViewModel.getData().observe(getViewLifecycleOwner(), (data) -> {
            binding.editTextTextMultiLine.setText(data);
            Bitmap bitmap;
            try {
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                bitmap = barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, 200, 200);
                binding.qrCodeImage.setImageBitmap(bitmap);
            } catch (WriterException e) {
                Log.e("SecureQR", e.getMessage());
            }
        });
        binding.button.setOnClickListener((view) -> {
            editorViewModel.setData(binding.editTextTextMultiLine.getText().toString());
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}