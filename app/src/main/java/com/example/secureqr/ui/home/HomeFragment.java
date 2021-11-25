package com.example.secureqr.ui.home;

import static androidx.core.content.ContextCompat.getSystemService;

import android.Manifest;
import android.content.Context;
import android.graphics.Camera;
import android.graphics.Rect;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraProvider;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.secureqr.MainActivity;
import com.example.secureqr.R;
import com.example.secureqr.databinding.FragmentHomeBinding;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
    private ProcessCameraProvider cameraProvider;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupCamera(binding.previewView);
    }

    private void setupCamera(PreviewView previewView) {
        TedPermission.create().setPermissionListener(new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                Toast.makeText(requireActivity(), "Permission Granted", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(requireActivity(), "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }
        }).setDeniedMessage("Permissions are denied")
                .setPermissions(Manifest.permission.CAMERA)
                .check();

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                Integer lensFacing = null;
                if (cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA))
                    lensFacing = CameraSelector.LENS_FACING_BACK;
                else if (cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA))
                    lensFacing = CameraSelector.LENS_FACING_FRONT;

                WindowManager windowManager = (WindowManager) requireContext().getSystemService(Context.WINDOW_SERVICE);
                Rect metrics = windowManager.getCurrentWindowMetrics().getBounds();
                int aspectRatio = getAspectRatio(metrics.width(), metrics.height());
                CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(lensFacing).build();
                Preview preview = new Preview.Builder()
                        .setTargetAspectRatio(aspectRatio)
                        //.setTargetRotation(previewView.getDisplay().getRotation())
                        .build();

                BarcodeScanner barcodeScanner = BarcodeScanning.getClient();
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setTargetAspectRatio(aspectRatio)
                        //.setTargetRotation(previewView.getDisplay().getRotation())
                        .build();
                imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(),
                        (imageProxy) -> processImageProxy(imageProxy, barcodeScanner)
                );

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
            } catch (ExecutionException | InterruptedException | CameraInfoUnavailableException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private static void processImageProxy(ImageProxy imageProxy, BarcodeScanner barcodeScanner) {
        InputImage inputImage = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());
        barcodeScanner.process(inputImage)
                .addOnSuccessListener((barcodes) -> barcodes.forEach((barcode) -> Log.i("SecureQR", barcode.getRawValue())))
                .addOnFailureListener((e) -> e.printStackTrace())
                .addOnCompleteListener((task) -> imageProxy.close());
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}