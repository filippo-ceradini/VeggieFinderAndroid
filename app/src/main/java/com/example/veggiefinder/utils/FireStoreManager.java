package com.example.veggiefinder.utils;

import android.net.Uri;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FireStoreManager {
    private static final String IMAGE_COLLECTION = "images";
    private final CollectionReference imageCollection;
    private final StorageReference storageRef;
    private static FireStoreManager instance = null;

    public static FireStoreManager getInstance() {
        if (instance == null) {
            instance = new FireStoreManager();
        }
        return instance;
    }

    private FireStoreManager() {
        FirebaseFirestore fireStore = FirebaseFirestore.getInstance();
        imageCollection = fireStore.collection(IMAGE_COLLECTION);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    /**
     * Add an image to FireStore
     */
    public void addImage(String imageUrl, String imageName) {
        Map<String, Object> imageMap = new HashMap<>();
        imageMap.put("imageUrl", imageUrl);
        imageMap.put("imageName", imageName);

        imageCollection.document(imageName).set(imageMap)
                .addOnSuccessListener(unused -> {
                    // Image added successfully
                })
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        // Image adding failed
                    }
                });
    }

    /**
     * Upload an image to Firebase Storage and add its metadata to FireStore
     */
    public void uploadImage(Uri imageUri, String imageName, OnSuccessListener<String> successListener, OnFailureListener failureListener) {
        StorageReference imageRef = storageRef.child(IMAGE_COLLECTION + "/" + imageName);

        // Image uploading failed
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Image uploaded successfully, get the download URL
                    imageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String imageUrl = uri.toString();

                                // Add the image metadata to FireStore
                                addImage(imageUrl, imageName);

                                // Call the success listener with the image URL
                                successListener.onSuccess(imageUrl);
                            });
                })
                .addOnFailureListener(failureListener);
    }


    /**
     * Load all images from Firebase Storage and pass the image URLs to the provided success listener
     */
    public void loadImagesFromStorage(OnSuccessListener<List<String>> successListener, OnFailureListener failureListener) {
        StorageReference imagesRef = storageRef.child(IMAGE_COLLECTION);

        imagesRef.listAll().addOnSuccessListener(listResult -> {
            List<String> imageUrls = new ArrayList<>();

            for (StorageReference item : listResult.getItems()) {
                item.getDownloadUrl().addOnSuccessListener(uri -> {
                    imageUrls.add(uri.toString());

                    // If all images have been loaded, pass the image URLs to the success listener
                    if (imageUrls.size() == listResult.getItems().size()) {
                        successListener.onSuccess(imageUrls);
                    }
                }).addOnFailureListener(failureListener);
            }
        }).addOnFailureListener(failureListener);
    }

}