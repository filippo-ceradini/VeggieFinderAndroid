package com.example.veggiefinder;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.veggiefinder.utils.FireStoreManager;
import com.example.veggiefinder.utils.ImageAdapter;
import com.example.veggiefinder.utils.ImageProcessor;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ChoosePictureActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    private String selectedImageUrl;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_picture);
        recyclerView = findViewById(R.id.recyclerView);
        Button selectImageButton = findViewById(R.id.select_image_button);

        // Set up RecyclerView with a LinearLayoutManager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        selectImageButton.setOnClickListener(v -> {
            if (selectedImageUrl != null) {
                loadImageBytesAndProcess(selectedImageUrl);
            } else {
                Toast.makeText(ChoosePictureActivity.this, "Please select an image from the list", Toast.LENGTH_SHORT).show();
            }
        });

        // Load the images from FireStore and update the RecyclerView
        loadImagesFromFireStore();
    }

    private void loadImagesFromFireStore() {
        FireStoreManager.getInstance().loadImagesFromStorage(new OnSuccessListener<List<String>>() {
            @Override
            public void onSuccess(List<String> imageUrls) {
                ImageAdapter adapter = new ImageAdapter(imageUrls, new ImageAdapter.OnImageClickListener() {
                    @Override
                    public void onImageClicked(String imageUrl) {
                        selectedImageUrl = imageUrl;
                    }
                });
                recyclerView.setAdapter(adapter);
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ChoosePictureActivity.this, "Error loading images: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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


    private void loadImageBytesAndProcess(String imageUrl) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(imageUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    byte[] imageBytes = response.body().bytes();
                    ImageProcessor.processImage(ChoosePictureActivity.this, imageBytes, (predictedVegetable, imageBytes1) -> showResultDialog(predictedVegetable, imageBytes1));
                }
            }
        });
    }
}
