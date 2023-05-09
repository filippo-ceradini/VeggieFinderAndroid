package com.example.veggiefinder.utils;

import android.app.Activity;
import android.widget.Toast;
import androidx.annotation.NonNull;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

public class ImageProcessor {
    public interface OnImageProcessedListener {
        void onImageProcessed(String predictedVegetable, byte[] imageBytes);
    }

    public static void processImage(@NonNull Activity activity, byte[] imageBytes, OnImageProcessedListener listener) {
        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "selected_image.jpg", RequestBody.create(imageBytes, MediaType.parse("image/*")))
                .build();

        Request predictRequest = new Request.Builder()
                .url("http://10.0.1.3:8080/api/predict")
                .post(requestBody)
                .build();

        client.newCall(predictRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();

                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        String predictedVegetable = jsonObject.getString("result");

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listener.onImageProcessed(predictedVegetable, imageBytes);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
