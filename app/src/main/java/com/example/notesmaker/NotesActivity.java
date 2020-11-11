package com.example.notesmaker;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;
import com.ml.quaterion.text2summary.Text2Summary;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import kotlin.jvm.internal.Ref.ObjectRef;

public class NotesActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 1;
    private static final int REQUEST_CODE_SELECT_DOC = 10;
    static final int REQUEST_IMAGE_CAPTURE = 2;

    RecyclerView PDFList;
    PdfListAdapter pdfListAdapter;
    LinearLayoutManager linearLayoutManager;
    File[] allPdfList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        initRecyclerView();


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

//        FloatingActionButton docFab = findViewById(R.id.fab_doc);
//        docFab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent myFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
//                myFileIntent.setType("*/*");
//                startActivityForResult(myFileIntent, REQUEST_CODE_SELECT_DOC);
//            }
//        });
    }

    private void initRecyclerView() {
        try {
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyPdf";
            File directory = new File(path);
            File[] allFiles = directory.listFiles();
            PDFList = findViewById(R.id.pdfList);

            allPdfList = getPDFs(allFiles);
            pdfListAdapter = new PdfListAdapter(allPdfList);
            linearLayoutManager = new LinearLayoutManager(this);

            PDFList.setLayoutManager(linearLayoutManager);
            PDFList.setAdapter(pdfListAdapter);
        } catch (Exception e){
            e.printStackTrace();
        }

        pdfListAdapter.setOnItemClickListener(new PdfListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(NotesActivity.this, PdfActivity.class);
                // passing current pdf from here
                String pdfpath = allPdfList[position].getPath();


                Log.d("chk", pdfpath);

                intent.putExtra("PdfPath", pdfpath);
                startActivity(intent);
            }
        });
    }

    private File[] getPDFs(File[] allFiles) {
        int i = 0;
        File[] PDFs = new File[allFiles.length];
        for(File f: allFiles){
            if (f.isFile() && f.getPath().endsWith(".pdf")){
                PDFs[i] = f;
                i++;
            }
        }
        return PDFs;
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

           // previewText(text);
          //  Log.d("chk", text);
            //Use the text from here aditya

            // Dialog box
            // Initializing a dialog box
            final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(NotesActivity.this);
            View mView = getLayoutInflater().inflate(R.layout.dialog, null);

            // declaring edit text\
            final EditText editText = mView.findViewById(R.id.edit_text);
            final Button button = mView.findViewById(R.id.button);
            final Button button2 = mView.findViewById(R.id.button2);
            // setting view
            builder.setView(mView);
//
//            // prevents off screen touches
            builder.setCancelable(false);

            final String copiedText = text;
            editText.setText(copiedText);

            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    String finalText = summarizeText(copiedText);
                    editText.setText(finalText);
                }
            });

            button2.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    editText.setText(copiedText);
                }
            });

            builder.setPositiveButton("Save as PDF", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String finalText = editText.getText().toString();
                    saveToPDF(finalText);

                }
            });

            // setting neg btn
            builder.setNegativeButton("Discard", null);

            //show
            builder.show();

        }
    }

    private void saveToPDF(String text)
    {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){

            if(text.isEmpty())
            {
                Toast.makeText(this, "Nothing to save, Text is empty", Toast.LENGTH_SHORT).show();
            }
            else {
                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyPdf";

                PDF pdf = new PDF();
                pdf.addParagraph(text);
                pdf.makeDocument(path);

                Toast.makeText(this, "Note Saved as a PDF in " + path, Toast.LENGTH_SHORT).show();

                initRecyclerView();
            }

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("Storage Permission Needed");
                alert.setMessage("We need storage permission to store the PDF on your device. Please grant storage permission.");
                alert.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 01);
                    }
                });
                alert.show();
            } else {
                requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 01);
            }
        }
    }

    private String summarizeText(String text)
    {
        final ObjectRef summary = new ObjectRef();
        summary.element = Text2Summary.Companion.summarize(text, 0.4F);
        //  TV.setText((CharSequence)((String)summary.element));
//        previewText((String)summary.element);
        return (String)summary.element;
    }

//   private void previewText(String string){
//        textView.setText(string);
//   }
}