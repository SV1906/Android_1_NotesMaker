package com.example.notesmaker;

public class PreviewData {
    private String mPreviewText;

    public PreviewData(String previewText){
        mPreviewText = previewText;
    }

    public void changeText(String text){
        mPreviewText = text;
    }

    public String getPreviewText(){
        return mPreviewText;
    }
}
