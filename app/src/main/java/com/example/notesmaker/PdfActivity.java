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

        // get intent here
        String pdfpath = getIntent().getStringExtra("PdfPath");
        File pdfFile = new File(pdfpath);
        pdfView.fromFile(pdfFile)
                .pages(0, 2, 1, 3, 3, 3) // all pages are displayed by default
                .enableSwipe(true)
                .enableDoubletap(true)
                .swipeVertical(false)
                .defaultPage(1)
                .showMinimap(false)
                .enableAnnotationRendering(false)
                .password(null)
                .showPageWithAnimation(true)
                .load();

    }
}