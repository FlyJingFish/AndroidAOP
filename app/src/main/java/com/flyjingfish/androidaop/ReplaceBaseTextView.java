package com.flyjingfish.androidaop;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.flyjingfish.android_aop_annotation.anno.AndroidAopModifyExtendsClass;

//@AndroidAopModifyExtendsClass(value = "androidx.appcompat.widget.AppCompatTextView",isParent = true)
public class ReplaceBaseTextView extends AppCompatTextView {
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
