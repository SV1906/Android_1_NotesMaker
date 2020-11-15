package com.example.notesmaker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.Frame;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

public class CloudNotes extends AppCompatActivity {

    RecyclerView cloudNotes;
    LinearLayoutManager linearLayoutManager;
    CloudFileAdapter cloudFileAdapter;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseStorage storage;
    StorageReference pdfStorage;
    SwipeRefreshLayout swipeRefreshLayout;




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_notes);

        swipeRefreshLayout = findViewById(R.id.cloudListRefresh);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        if (mUser!=null){
            storage = FirebaseStorage.getInstance();
            pdfStorage = storage.getReference();
            pdfStorage = pdfStorage.child(mUser.getUid());
            pdfStorage = pdfStorage.child("PDFs");
            getFiles();
            swipeRefreshLayout.setEnabled(true);


            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    getFiles();
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        } else {
            swipeRefreshLayout.setEnabled(false);
        }

        cloudNotes = findViewById(R.id.cloudNotes);
        linearLayoutManager = new LinearLayoutManager(this);


    }

    public void getFiles(){
        pdfStorage.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                cloudFileAdapter = new CloudFileAdapter(CloudNotes.this, listResult.getItems());
                cloudNotes.setLayoutManager(linearLayoutManager);
                cloudNotes.setAdapter(cloudFileAdapter);
            }
        });
    }

    public void shareNote(StorageReference storageReference) {
        File cache = getCacheDir();
        File PDF = null;
        try {
            PDF = File.createTempFile("TempFile", null, cache);
        } catch (IOException e) {
            e.printStackTrace();
        }
        final File finalPDF = PDF;

        storageReference.getFile(PDF).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("application/pdf");
                shareIntent.putExtra(Intent.EXTRA_STREAM, finalPDF);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareIntent, "Share it"));
            }
        });
    }

}