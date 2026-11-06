package com.gonnux.secureqr.ui.editor;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditorFragment extends Fragment {

    private EditorViewModel editorViewModel;
    private FragmentEditorBinding binding;
    private final ExecutorService cryptoExecutor = Executors.newSingleThreadExecutor();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        editorViewModel = new ViewModelProvider(requireActivity()).get(EditorViewModel.class);

        binding = FragmentEditorBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        editorViewModel.getSecureQrMode().observe(getViewLifecycleOwner(), (mode) -> {
            binding.secureQrModeSwitch.setChecked(mode);
            binding.passwordEdit.setText(null);
            binding.passwordEdit.setEnabled(mode);
            boolean hasPassword = binding.passwordEdit.getText().length() >= 1;
            binding.encodeQrButton.setEnabled(mode && hasPassword);
            binding.decodeQrButton.setEnabled(mode && hasPassword);
            if (!mode) {
                editorViewModel.setOutputData(binding.inputDataEdit.getText().toString());
            } else {
                editorViewModel.setOutputData(null);
            }
        });

        editorViewModel.getInputData().observe(getViewLifecycleOwner(), (inputData) -> {
            binding.inputDataEdit.setText(inputData);
        });

        editorViewModel.getOutputData().observe(getViewLifecycleOwner(), (outputData) -> {
            binding.outputDataText.setText(outputData);
            if (outputData != null && !outputData.isEmpty()) {
                try {
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    Bitmap bitmap = barcodeEncoder.encodeBitmap(outputData, BarcodeFormat.QR_CODE, 200, 200);
                    binding.qrCodeImage.setImageBitmap(bitmap);
                } catch (WriterException e) {
                    Log.e("SecureQR", e.getMessage());
                }
            } else {
                binding.qrCodeImage.setImageBitmap(null);
            }
        });

        binding.qrCodeImage.setOnClickListener((view) -> {
            Drawable drawable = ((ImageView) view).getDrawable();
            if (!(drawable instanceof BitmapDrawable)) {
                Toast.makeText(requireActivity(), "No QR code to share", Toast.LENGTH_SHORT).show();
                return;
            }
            Bitmap image = ((BitmapDrawable) drawable).getBitmap();
            String imageUri = MediaStore.Images.Media.insertImage(requireActivity().getContentResolver(), image, "SecureQr", "QR Code");
            if (imageUri == null) {
                Toast.makeText(requireActivity(), "Sharing QR code has failed", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/*");
            share.putExtra(Intent.EXTRA_STREAM, Uri.parse(imageUri));
            startActivity(Intent.createChooser(share,"Share via"));
        });

        binding.outputDataText.setOnClickListener((view) -> {
            String text = binding.outputDataText.getText().toString();
            if (!text.isEmpty()) {
                ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("SecureQR", text);
                if (Boolean.TRUE.equals(editorViewModel.getSecureQrMode().getValue())) {
                    // Android 13+ 클립보드 미리보기에 복호화한 평문이 뜨지 않게 한다. 이전 버전은 이 extra 를 무시한다.
                    PersistableBundle extras = new PersistableBundle();
                    extras.putBoolean("android.content.extra.IS_SENSITIVE", true);
                    clip.getDescription().setExtras(extras);
                }
                clipboard.setPrimaryClip(clip);
                Toast.makeText(requireActivity(), "Copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });

        binding.secureQrModeSwitch.setOnCheckedChangeListener((compoundButton, checked) -> {
            editorViewModel.setSecureQrMode(checked);
        });

        binding.inputDataEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editorViewModel.getSecureQrMode().getValue()) {
                    editorViewModel.setOutputData(editable.toString());
                }
            }
        });

        binding.passwordEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                boolean secureMode = editorViewModel.getSecureQrMode().getValue();
                boolean hasPassword = editable.length() >= 1;
                binding.encodeQrButton.setEnabled(secureMode && hasPassword);
                binding.decodeQrButton.setEnabled(secureMode && hasPassword);
            }
        });

        binding.encodeQrButton.setOnClickListener((view) -> {
            view.setEnabled(false);
            binding.decodeQrButton.setEnabled(false);
            String inputData = binding.inputDataEdit.getText().toString();

            if (editorViewModel.getSecureQrMode().getValue()) {
                String password = binding.passwordEdit.getText().toString();
                cryptoExecutor.execute(() -> {
                    try {
                        String result = Crypto.encrypt(inputData, password);
                        view.post(() -> {
                            editorViewModel.setOutputData(result);
                            view.setEnabled(true);
                            binding.decodeQrButton.setEnabled(true);
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        view.post(() -> {
                            editorViewModel.setOutputData(null);
                            Toast.makeText(requireActivity(), "Encoding QR code has failed", Toast.LENGTH_SHORT).show();
                            view.setEnabled(true);
                            binding.decodeQrButton.setEnabled(true);
                        });
                    }
                });
            } else {
                editorViewModel.setOutputData(inputData);
                view.setEnabled(true);
                binding.decodeQrButton.setEnabled(true);
            }
        });

        binding.decodeQrButton.setOnClickListener((view) -> {
            view.setEnabled(false);
            binding.encodeQrButton.setEnabled(false);
            String inputData = binding.inputDataEdit.getText().toString();
            if (inputData.isEmpty()) {
                Toast.makeText(requireActivity(), "Decoding QR code has failed", Toast.LENGTH_SHORT).show();
                view.setEnabled(true);
                binding.encodeQrButton.setEnabled(true);
                return;
            }
            String password = binding.passwordEdit.getText().toString();
            cryptoExecutor.execute(() -> {
                try {
                    String result = Crypto.decrypt(inputData, password);
                    view.post(() -> {
                        editorViewModel.setOutputData(result);
                        view.setEnabled(true);
                        binding.encodeQrButton.setEnabled(true);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    view.post(() -> {
                        editorViewModel.setOutputData(null);
                        Toast.makeText(requireActivity(), "Decoding QR code has failed", Toast.LENGTH_SHORT).show();
                        view.setEnabled(true);
                        binding.encodeQrButton.setEnabled(true);
                    });
                }
            });
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cryptoExecutor.shutdownNow();
        binding = null;
    }
}
