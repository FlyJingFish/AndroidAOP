package com.flyjingfish.androidaop;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textview.MaterialTextView;

public class ReplaceBaseTextView extends MaterialTextView {
    public ReplaceBaseTextView(@NonNull Context context) {
        super(context);
    }
    public ReplaceBaseTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ReplaceBaseTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText("ReplaceTextView:"+text, type);
        Log.e("ReplaceTextView","ReplaceTextView-setText");
    }

    @Override
    public void append(CharSequence text, int start, int end) {
        super.append("ReplaceTextView:"+text, start, end);
        Log.e("ReplaceTextView","ReplaceTextView-append");
    }
}
