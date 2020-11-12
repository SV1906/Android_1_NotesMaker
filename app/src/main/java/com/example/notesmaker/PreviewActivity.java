package com.example.notesmaker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;

import com.ml.quaterion.text2summary.Text2Summary;

import java.util.ArrayList;

import kotlin.jvm.internal.Ref;

public class PreviewActivity extends AppCompatActivity {
    private ArrayList<PreviewData> mPreviewDataList;
    private String originalString;
    private Boolean isSummarised;

    private RecyclerView mRecyclerView;
    private PreviewAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private Button buttonAdd, buttonSaveToPDF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        createPreviewDataList();
        buildRecyclerView();

        buttonAdd = findViewById(R.id.btnAddPara);
        buttonSaveToPDF = findViewById(R.id.btnSavePDF);

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = mAdapter.getItemCount();
                insertItem(position);
            }
        });
    }

    public void insertItem(int position){
        mPreviewDataList.add(position, new PreviewData("Wow Epic!"));
        mAdapter.notifyItemInserted(position);
    }

    public void removeItem(int position){
        mPreviewDataList.remove(position);
        mAdapter.notifyItemRemoved(position);
    }

    public void changeItem(int position, String text){
        mPreviewDataList.get(position).changeText(text);
        mAdapter.notifyItemChanged(position);
    }

    public void createPreviewDataList(){
        mPreviewDataList = new ArrayList<>();
        mPreviewDataList.add(new PreviewData("Great job!"));
        mPreviewDataList.add(new PreviewData("Amazing job!"));
        mPreviewDataList.add(new PreviewData("Amazing Work!"));
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


}