package com.example.notesmaker;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.Arrays;

class PdfListAdapter extends RecyclerView.Adapter<PdfListAdapter.PdfViewHolder> {


    private OnItemClickListener mListener;
    private Context context;

    public interface OnItemClickListener{
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

    public class PdfViewHolder extends RecyclerView.ViewHolder implements PopupMenu.OnMenuItemClickListener {
        TextView pdfName;
        ImageButton dotBtn;

        public PdfViewHolder(@NonNull View itemView) {
            super(itemView);

            pdfName = itemView.findViewById(R.id.pdfName);
            dotBtn = itemView.findViewById(R.id.drop_down);

            dotBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Log.d("chk","clicked");
//                    ((NotesActivity)context).deleteNote(getAdapterPosition());
                        showPopupMenu(v);

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

        private void showPopupMenu(View v) {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
            popupMenu.inflate(R.menu.pop_up_menu);
            popupMenu.setOnMenuItemClickListener(this);
            popupMenu.show();
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch(item.getItemId())
            {
                case R.id.men_rename:
                    ((NotesActivity)context).renameNote(getAdapterPosition());
                    break;
                case R.id.men_share:
                    ((NotesActivity)context).shareNote(getAdapterPosition());

                    break;
                case R.id.men_delete:
                    ((NotesActivity)context).deleteNote(getAdapterPosition());
                    break;
            }
        return true;
        }
    }
}
