package com.example.notesmaker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public class NotesActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 1;
    private static final int REQUEST_CODE_SELECT_DOC = 10;
    static final int REQUEST_IMAGE_CAPTURE = 2;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        textView = findViewById(R.id.preview_text);

        FloatingActionButton camFab = findViewById(R.id.fab_cam);
        camFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // amey let ur code go here...

                {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }

            }
        });

        FloatingActionButton imgFab = findViewById(R.id.fab_image);
        imgFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(
                            NotesActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_STORAGE_PERMISSION
                    );
                }
                else
                {
                    selectImage();
                }
            }
        });

        FloatingActionButton docFab = findViewById(R.id.fab_doc);
        docFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                myFileIntent.setType("*/*");
                startActivityForResult(myFileIntent, REQUEST_CODE_SELECT_DOC);
            }
        });
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if(intent.resolveActivity(getPackageManager())!=null)
        {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length>0)
        {
            selectImage();
        }
        else
        {
            Toast.makeText(this, "Permission Denied!!!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            getTextFromBitmap(imageBitmap);
        }


        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {

                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    InputStream inputStream = null;
                    try {
                        inputStream = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
//                        varun u can get the bitmap from here for ocr...
                        getTextFromBitmap(bitmap);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Exception" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }

            }
        }

        if(requestCode == REQUEST_CODE_SELECT_DOC && resultCode == RESULT_OK){
            // doc code will go here ...
        }

    }

    private void getTextFromBitmap(Bitmap imageBitmap) {
        //Enter your code from here Varun
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionTextDetector textDetector = FirebaseVision.getInstance().getVisionTextDetector();
        textDetector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                displayTextFromImage(firebaseVisionText);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(NotesActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayTextFromImage(FirebaseVisionText firebaseVisionText) {
        List<FirebaseVisionText.Block> blockList = firebaseVisionText.getBlocks();
        String text = "";
        if(blockList.size() == 0){
            Toast.makeText(this, "No Text found in image", Toast.LENGTH_SHORT).show();
        }else{
            for (FirebaseVisionText.Block block : firebaseVisionText.getBlocks()){
                text += block.getText() + "\n";
            }

//            previewText(text);
            Log.d("img00", text);
            //Use the text from here aditya
            
        }
    }

//    private void previewText(String string){
//        textView.setText(string);
//    }
}