package com.example.covid19detector;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;


public class load extends AppCompatActivity {

    ImageView imageView;
    Button buttonLoad, buttonClassify;
    private String name, age, gen, diabetic, heart, anyOther;
    private static final int PICK_IMAGE = 100;
    Uri imageUri;
    private String chosen;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);

        name = (String) getIntent().getStringExtra("name");
        age = (String) getIntent().getStringExtra("age");
        gen = (String) getIntent().getStringExtra("gen");
        diabetic = (String) getIntent().getStringExtra("diabetic");
        heart = (String) getIntent().getStringExtra("heart");
        anyOther = (String) getIntent().getStringExtra("anyOther");

        imageView = (ImageView)findViewById(R.id.imageView3);
        buttonLoad = (Button)findViewById(R.id.button);
        buttonClassify = (Button)findViewById(R.id.button2);

        buttonLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        buttonClassify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chosen = "dcv.tflite";
                Intent intent = new Intent(load.this, Classify.class);
                intent.putExtra("resID_uri", imageUri);
                intent.putExtra("chosen", chosen);
                intent.putExtra("name", name);
                intent.putExtra("age", age);
                intent.putExtra("gen", gen);
                intent.putExtra("anyOther", anyOther);
                intent.putExtra("diabetic", diabetic);
                intent.putExtra("heart", heart);

                startActivity(intent);

            }
        });
    }


    private void openGallery(){
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE){
            imageUri = data.getData();
            imageView.setImageURI(imageUri);

        }
    }


}