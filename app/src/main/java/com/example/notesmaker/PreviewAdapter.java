package com.example.notesmaker;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PreviewAdapter extends RecyclerView.Adapter<PreviewAdapter.PreviewViewHolder> {
    private ArrayList<PreviewData> mPreviewDataList;
    private OnItemClickListener mListener;

    private String finalString;

    public interface OnItemClickListener {
        void onDeleteClick(int position);
        void onSummariseClick(int position);
        void onResetClick(int position);
    }

    public void setOnItemClickListener (OnItemClickListener listener) {
        mListener = listener;
    }

    public static class PreviewViewHolder extends RecyclerView.ViewHolder {
        public TextView mPreviewTextView;
        public Button mSaveButton, mSummariseButton, mResetButton, mDeleteButton;
        public EditText mPreviewEditText;
        public LinearLayout mPreviewLinearLayout;

        public PreviewViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            mPreviewTextView = itemView.findViewById(R.id.prevTextView);
            mSaveButton = itemView.findViewById(R.id.prevLayoutSave);
            mSummariseButton = itemView.findViewById(R.id.prevLayoutSummary);
            mResetButton = itemView.findViewById(R.id.prevLayoutReset);
            mDeleteButton = itemView.findViewById(R.id.prevLayoutDelete);
            mPreviewEditText = itemView.findViewById(R.id.prevEditText);
            mPreviewLinearLayout = itemView.findViewById(R.id.prevLinearLayout);

            mDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onDeleteClick(position);
                        }
                    }
                }
            });
            mSummariseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onSummariseClick(position);
                        }
                    }
                }
            });
            mResetButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onResetClick(position);
                        }
                    }
                }
            });
        }
    }

    public PreviewAdapter(ArrayList<PreviewData> previewDataList){
        mPreviewDataList = previewDataList;
    }

    @NonNull
    @Override
    public PreviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.preview_layout, parent, false);
        PreviewViewHolder previewViewHolder = new PreviewViewHolder(view, mListener);
        return previewViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final PreviewViewHolder holder, final int position) {
        PreviewData currentItem = mPreviewDataList.get(position);
        holder.mPreviewTextView.setText(currentItem.getPreviewText());
        holder.mPreviewEditText.setText(currentItem.getPreviewText());
        holder.mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalString = holder.mPreviewEditText.getText().toString();
                mPreviewDataList.get(position).changeText(finalString);
                holder.mPreviewTextView.setText(finalString);
                holder.mPreviewLinearLayout.setVisibility(View.GONE);
                holder.mPreviewTextView.setVisibility(View.VISIBLE);
            }
        });
        holder.mPreviewTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.mPreviewLinearLayout.setVisibility(View.VISIBLE);
                holder.mPreviewTextView.setVisibility(View.GONE);
            }
        });
        holder.mPreviewEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
//                if (s != null) {
//                    mPreviewDataList.get(position).changeText(String.valueOf(s));

            }
        });

    }

    @Override
    public int getItemCount() {
        return mPreviewDataList.size();
    }
}
