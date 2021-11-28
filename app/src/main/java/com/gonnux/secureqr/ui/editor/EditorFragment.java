package com.gonnux.secureqr.ui.editor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.gonnux.secureqr.biz.Crypto;
import com.gonnux.secureqr.databinding.FragmentEditorBinding;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class EditorFragment extends Fragment {

    private EditorViewModel editorViewModel;
    private FragmentEditorBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        editorViewModel = new ViewModelProvider(requireActivity()).get(EditorViewModel.class);

        binding = FragmentEditorBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        editorViewModel.getSecureQrMode().observe(getViewLifecycleOwner(), (mode) -> {
            binding.secureQrModeSwitch.setChecked(mode);
            binding.passwordEdit.setText(null);
            binding.passwordEdit.setEnabled(mode);
            binding.decodeQrButton.setEnabled(mode);
        });

        editorViewModel.getData().observe(getViewLifecycleOwner(), (data) -> {
            binding.qrCodeDataEdit.setText(data);
        });

        editorViewModel.getEncodedData().observe(getViewLifecycleOwner(), (encodedData) -> {
            binding.encodedQrCodeDataText.setText(encodedData);
            if (!editorViewModel.getSecureQrMode().getValue()) {
                editorViewModel.setData(encodedData);
            }
            try {
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.encodeBitmap(encodedData, BarcodeFormat.QR_CODE, 200, 200);
                binding.qrCodeImage.setImageBitmap(bitmap);
            } catch (WriterException e) {
                Log.e("SecureQR", e.getMessage());
            }
        });

        binding.qrCodeImage.setOnClickListener((view) -> {
            Bitmap image = ((BitmapDrawable) ((ImageView) view).getDrawable()).getBitmap();
            String imageUri = MediaStore.Images.Media.insertImage(requireActivity().getContentResolver(), image, "SecureQr", "QR Code");
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/*");
            share.putExtra(Intent.EXTRA_STREAM, Uri.parse(imageUri));
            startActivity(Intent.createChooser(share,"Share via"));
        });

        binding.secureQrModeSwitch.setOnCheckedChangeListener((compoundButton, checked) -> {
            editorViewModel.setSecureQrMode(checked);
            /*binding.qrCodeImage.setImageDrawable(null);
            editorViewModel.setEncodedData(null);*/
        });

        binding.encodeQrButton.setOnClickListener((view) -> {
            String data = binding.qrCodeDataEdit.getText().toString();
            editorViewModel.setData(data);

            if (editorViewModel.getSecureQrMode().getValue()) {
                try {
                    String password = binding.passwordEdit.getText().toString();
                    editorViewModel.setEncodedData(Crypto.encrypt(data, password));
                } catch (Exception e) {
                    editorViewModel.setEncodedData(null);
                    e.printStackTrace();
                    Toast.makeText(requireActivity(), "Encoding QR code has failed", Toast.LENGTH_SHORT).show();
                }
            } else {
                editorViewModel.setEncodedData(data);
            }
        });

        binding.decodeQrButton.setOnClickListener((view) -> {
            String data = editorViewModel.getEncodedData().getValue();
            String password = binding.passwordEdit.getText().toString();
            try {
                editorViewModel.setData(Crypto.decrypt(data, password));
            } catch (Exception e) {
                editorViewModel.setData(null);
                Toast.makeText(requireActivity(), "Decoding QR code has failed", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}