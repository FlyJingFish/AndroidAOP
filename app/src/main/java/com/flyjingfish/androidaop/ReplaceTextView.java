package com.flyjingfish.androidaop;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceExtendsClass;

//@AndroidAopReplaceExtendsClass("com.google.android.material.textview.MaterialTextView")
public class ReplaceTextView extends AppCompatTextView {
    public ReplaceTextView(@NonNull Context context) {
        super(context);
    }
    public ReplaceTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ReplaceTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
