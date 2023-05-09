package com.example.veggiefinder;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.veggiefinder.utils.ImageProcessor;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;

public class TakePictureActivity extends AppCompatActivity {
    private ImageCapture imageCapture;
    private PreviewView previewView;
    private static final int PERMISSIONS_REQUEST_CODE = 10;
    private static final String[] PERMISSIONS = {Manifest.permission.CAMERA};

    /**
     * Setup the Activity
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_picture);

        Button mCaptureButton = findViewById(R.id.capture_button);
        previewView = findViewById(R.id.previewView);

        //Requests permission to use the camera
        requestPermissions();
        mCaptureButton.setOnClickListener(v -> takePicture());

    }

    private void setUpCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                imageCapture = new ImageCapture.Builder().build();
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
            } catch (ExecutionException | InterruptedException e) {
                Log.e("TakePictureFragment", "Error setting up camera", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void showResultDialog(String predictedVegetable, byte[] imageBytes) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_result);

        ImageView resultImageView = dialog.findViewById(R.id.result_image_view);
        TextView resultTextView = dialog.findViewById(R.id.result_text_view);
        Button closeButton = dialog.findViewById(R.id.close_button);

        resultTextView.setText(getString(R.string.predicted_vegetable, predictedVegetable));
        Glide.with(this).load(imageBytes).into(resultImageView);

        closeButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void takePicture() {
        if (imageCapture == null) {
            return;
        }

        File photoFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "photo.jpg");

        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                //TODO save Image on FireStore
                Toast.makeText(TakePictureActivity.this, "Image saved successfully", Toast.LENGTH_SHORT).show();
                byte[] imageBytes = readFileAsByteArray(photoFile);
                if (imageBytes != null) {
                    ImageProcessor.processImage(TakePictureActivity.this, imageBytes, (predictedVegetable, imageBytes1) -> showResultDialog(predictedVegetable, imageBytes1));

                } else {
                    Toast.makeText(TakePictureActivity.this, "Error processing image", Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e("TakePictureActivity", "Error capturing image", exception);
            }
        });
    }

    private byte[] readFileAsByteArray(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, read);
            }
            fis.close();
            return bos.toByteArray();
        } catch (IOException e) {
            Log.e("TakePictureActivity", "Error reading file", e);
        }
        return null;
    }


    private void requestPermissions() {
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSIONS_REQUEST_CODE);
        } else {
            setUpCamera();
        }
    }

    private static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setUpCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to use this app", Toast.LENGTH_SHORT).show();
            }
        }
    }


}