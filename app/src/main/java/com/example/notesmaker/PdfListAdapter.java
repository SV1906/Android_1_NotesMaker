package com.example.notesmaker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.Arrays;

class PdfListAdapter extends RecyclerView.Adapter<PdfListAdapter.PdfViewHolder> {


    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    File[] PDFs;

    public PdfListAdapter(File[] PDFs) {
        Arrays.sort(PDFs);
        this.PDFs = PDFs;
    }

    @NonNull
    @Override
    public PdfViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pdf_list_item, parent, false);
        return new PdfViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PdfViewHolder holder, int position) {
        holder.pdfName.setText(PDFs[position].getName());

    }

    @Override
    public int getItemCount() {
        return PDFs.length;
    }

    public class PdfViewHolder extends RecyclerView.ViewHolder {
        TextView pdfName;

        public PdfViewHolder(@NonNull View itemView) {
            super(itemView);
            pdfName = itemView.findViewById(R.id.pdfName);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onItemClick(position);
                        }
                    }
                }
            });

        }
    }
}
