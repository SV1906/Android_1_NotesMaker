package com.example.notesmaker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;
import com.ml.quaterion.text2summary.Text2Summary;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.BufferUnderflowException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import kotlin.jvm.internal.Ref;

public class PreviewActivity extends AppCompatActivity {
    private ArrayList<PreviewData> mPreviewDataList;
    private String originalString;
    private Boolean isSummarised;
    private Uri imgUri;

    private RecyclerView mRecyclerView;
    private PreviewAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private Button buttonAdd, buttonSaveToPDF;
    private EditText editTextPDFName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        createPreviewDataList();
        buildRecyclerView();
        Bundle extras = getIntent().getExtras();
        String text;
        if (extras != null){
            text = extras.getString("Text");
            insertItem(0, text);
        }


        buttonAdd = findViewById(R.id.btnAddPara);
        buttonSaveToPDF = findViewById(R.id.btnSavePDF);
        editTextPDFName = findViewById(R.id.prevEditTextTitle);

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.startPickImageActivity(PreviewActivity.this);
                /*if (imgUri != null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(imgUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        getTextFromBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }*/
            }
        });
        buttonSaveToPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullPDFString = "";
                for (PreviewData data : mPreviewDataList){
                    fullPDFString += data.getPreviewText() + "\n";
                }
                String PDFName = editTextPDFName.getText().toString();
                if (PDFName == null){
                    PDFName = getName(PDFName);
                }
                saveToPDF(fullPDFString, PDFName);
            }
        });
    }

    private void saveToPDF(String fullPDFString, String PDFName) {
        //Code to save PDF
    }

    String getName(String name){;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss", Locale.ENGLISH);
        Date now = new Date();

        String pdfName = (name + "_" + formatter.format(now) + ".pdf");
        return pdfName;
    }

    private void getTextFromBitmap(Bitmap bitmap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionTextDetector textDetector = FirebaseVision.getInstance().getVisionTextDetector();
        textDetector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                displayTextFromImage(firebaseVisionText);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PreviewActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayTextFromImage(FirebaseVisionText firebaseVisionText) {
        List<FirebaseVisionText.Block> blockList = firebaseVisionText.getBlocks();
        String text = "";
        if(blockList.size() == 0){
            Toast.makeText(this, "No Text found in image", Toast.LENGTH_SHORT).show();
        }else {
            for (FirebaseVisionText.Block block : firebaseVisionText.getBlocks()) {
                text += block.getText() + "\n";
            }
            int position = mAdapter.getItemCount();
            insertItem(position, text);
        }
    }


    public void insertItem(int position, String text){
        mPreviewDataList.add(position, new PreviewData(text));
        mAdapter.notifyItemInserted(position);
        mAdapter.notifyDataSetChanged();
    }

    public void removeItem(int position){
        mPreviewDataList.remove(position);
        mAdapter.notifyItemRemoved(position);
        mAdapter.notifyDataSetChanged();
    }

    public void changeItem(int position, String text){
        mPreviewDataList.get(position).changeText(text);
        mAdapter.notifyItemChanged(position);
        mAdapter.notifyDataSetChanged();
    }

    public void createPreviewDataList(){
        mPreviewDataList = new ArrayList<>();
        /*mPreviewDataList.add(new PreviewData("Great job!"));
        mPreviewDataList.add(new PreviewData("Amazing job!"));
        mPreviewDataList.add(new PreviewData("Amazing Work!"));*/
    }

    public void buildRecyclerView(){
        mRecyclerView = findViewById(R.id.previewRecyclerView);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new PreviewAdapter(mPreviewDataList);
        isSummarised = false;

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new PreviewAdapter.OnItemClickListener() {
            @Override
            public void onDeleteClick(int position) {
                removeItem(position);
            }

            @Override
            public void onSummariseClick(int position) {
                isSummarised = true;
                originalString = mPreviewDataList.get(position).getPreviewText();
                String summarisedText = summariseText(originalString);
                changeItem(position, summarisedText);
            }

            @Override
            public void onResetClick(int position) {
                if (isSummarised) {
                    changeItem(position, originalString);
                }
            }
        });
    }

    private String summariseText(String text) {

        final Ref.ObjectRef summary = new Ref.ObjectRef();

        summary.element = Text2Summary.Companion.summarize(text, 0.4F);

        return (String)summary.element;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            Uri imageUri = CropImage.getPickImageResultUri(this, data);
            imgUri = imageUri;
            startCrop(imageUri);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                imgUri = result.getUri();
                if (imgUri != null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(imgUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        getTextFromBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                Toast.makeText(this, "Image captured successfully !", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startCrop(Uri imageUri) {
        CropImage.activity(imageUri).setGuidelines(CropImageView.Guidelines.ON).setMultiTouchEnabled(true).start(this);
    }
}