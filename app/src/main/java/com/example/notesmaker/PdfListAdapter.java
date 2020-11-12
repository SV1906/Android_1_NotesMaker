package com.example.notesmaker;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.Arrays;

class PdfListAdapter extends RecyclerView.Adapter<PdfListAdapter.PdfViewHolder> {


    private OnItemClickListener mListener;
    private Context context;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    File[] PDFs;

    public PdfListAdapter(Context context, File[] PDFs) {
        Arrays.sort(PDFs);
        this.PDFs = PDFs;
        this.context = context;
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
        ImageButton deleteBtn;

        public PdfViewHolder(@NonNull View itemView) {
            super(itemView);

            pdfName = itemView.findViewById(R.id.pdfName);
            deleteBtn = itemView.findViewById(R.id.delete_btn);

            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Log.d("chk","clicked");
                    ((NotesActivity)context).deleteNote(getAdapterPosition());
                }
            });

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
