package com.example.veggiefinder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);

        Button takePictureButton = findViewById(R.id.take_picture_button);
        Button choosePictureButton = findViewById(R.id.choose_picture_button);

        takePictureButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, TakePictureActivity.class)));
        choosePictureButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ChoosePictureActivity.class)));
    }
}
