package com.example.notesmaker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;

public class PdfActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);

        PDFView pdfView = findViewById(R.id.pdfView);

        File

        pdfView.fromFile(File);
}