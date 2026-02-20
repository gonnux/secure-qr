package com.gonnux.secureqr.ui.scanner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.gonnux.secureqr.R;
import com.gonnux.secureqr.biz.CipherText;
import com.gonnux.secureqr.databinding.FragmentScannerBinding;
import com.gonnux.secureqr.ui.editor.EditorViewModel;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class ScannerFragment extends Fragment {

    private FragmentScannerBinding binding;
    private EditorViewModel editorViewModel;
    private NavController navController;
    private RectF qrCodeScanArea;
    private RectF previewArea;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentScannerBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        editorViewModel = new ViewModelProvider(requireActivity()).get(EditorViewModel.class);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        binding.previewView.post(this::setupCamera);
    }

    private void requirePermission() {
        TedPermission.create().setPermissionListener(new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                Log.i("SecureQR", "Permission Granted");
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(requireActivity(), "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }
        }).setDeniedMessage("Permissions are denied")
        .setPermissions(Manifest.permission.CAMERA)
        .check();
    }

    private void printRect(String tag, RectF rect) {
        Log.i(tag, "left: " + rect.left + " top: " + rect.top + " right: " + rect.right + " bottom: " + rect.bottom + " width: " + rect.width() + " height: " + rect.height());
    }

    private void setupCamera() {
        requirePermission();
        qrCodeScanArea = getViewArea(binding.qrCodeScanArea);
        previewArea = getViewArea(binding.previewView);

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                int lensFacing;
                if (cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA))
                    lensFacing = CameraSelector.LENS_FACING_BACK;
                else if (cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA))
                    lensFacing = CameraSelector.LENS_FACING_FRONT;
                else
                    throw new RuntimeException("No Camera");

                CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(lensFacing).build();

                int rotation = binding.previewView.getDisplay().getRotation();
                int aspectRatio = getAspectRatio();

                Preview preview = new Preview.Builder()
                        .setTargetAspectRatio(aspectRatio)
                        .setTargetRotation(rotation)
                        .build();

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setTargetAspectRatio(aspectRatio)
                        .setTargetRotation(rotation)
                        .build();

                BarcodeScanner barcodeScanner = BarcodeScanning.getClient();

                imageAnalysis.setAnalyzer(
                    Executors.newSingleThreadExecutor(),
                    (imageProxy) -> processImageProxy(imageProxy, barcodeScanner)
                );

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

                preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

            } catch (ExecutionException | InterruptedException | CameraInfoUnavailableException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private RectF getBarcodeArea(ImageProxy imageProxy, Barcode barcode) {
        float scaleFactorY = Math.max(previewArea.height(), previewArea.width()) / Math.max(imageProxy.getWidth(), imageProxy.getHeight());
        float scaleFactorX = Math.min(previewArea.width(), previewArea.height()) / Math.min(imageProxy.getHeight(), imageProxy.getWidth());
        RectF rect = new RectF(barcode.getBoundingBox());
        float newLeft = rect.left * scaleFactorX;
        float newTop = rect.top * scaleFactorY + previewArea.top;
        float newRight = rect.right * scaleFactorX;
        float newBottom = rect.bottom * scaleFactorY + previewArea.top;

        return new RectF(newLeft, newTop, newRight, newBottom);
    }

    private void processBarcode(Barcode barcode){
        String data = barcode.getRawValue();
        // getRawValue()는 QR코드 데이터가 유효한 UTF-8이 아니면 null을 반환한다
        if (data == null) {
            Toast.makeText(requireActivity(), "지원하지 않는 QR코드 형식입니다", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            CipherText.decode(data);
            editorViewModel.setSecureQrMode(true);
            editorViewModel.setData(null);
        } catch (IllegalArgumentException e) {
            editorViewModel.setData(data);
        }
        editorViewModel.setEncodedData(data);
        navController.navigate(R.id.navigation_editor);
    }

    private void processImageProxy(ImageProxy imageProxy, BarcodeScanner barcodeScanner) {
        @SuppressLint("UnsafeOptInUsageError")
        InputImage inputImage = InputImage.fromMediaImage(
            Objects.requireNonNull(imageProxy.getImage()), imageProxy.getImageInfo().getRotationDegrees()
        );

        barcodeScanner.process(inputImage)
        .addOnSuccessListener((barcodes) ->
            barcodes
            .stream()
            .filter(barcode -> qrCodeScanArea.contains(getBarcodeArea(imageProxy, barcode)))
            .findFirst()
            .ifPresent(this::processBarcode))
            .addOnFailureListener(Throwable::printStackTrace)
            .addOnCompleteListener((task) -> imageProxy.close()
        );
    }

    private static int getAspectRatio(int width, int height) {
        final double RATIO_4_3_VALUE = 4.0 / 3.0;
        final double RATIO_16_9_VALUE = 16.0 / 9.0;
        double previewRatio = ((double)Math.max(width, height)) / Math.min(width, height);
        if (Math.abs(previewRatio - RATIO_4_3_VALUE) <= Math.abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3;
        }
        return AspectRatio.RATIO_16_9;
    }

    private Rect getScreenArea() {
        WindowManager windowManager = (WindowManager) requireContext().getSystemService(Context.WINDOW_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            return windowManager.getCurrentWindowMetrics().getBounds();
        } else {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(displayMetrics);
            return new Rect(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        }
    }

    private int getAspectRatio() {
        Rect screenArea = getScreenArea();
        return getAspectRatio(screenArea.width(), screenArea.height());
    }

    private static RectF getViewArea(View view) {
        int[] coords = new int[2];
        view.getLocationOnScreen(coords);
        return new RectF(
                coords[0],
                coords[1],
                coords[0] + view.getWidth(),
                coords[1] + view.getHeight()
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}