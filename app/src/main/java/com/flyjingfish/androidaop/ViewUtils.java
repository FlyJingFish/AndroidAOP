package com.flyjingfish.androidaop;

import android.util.LayoutDirection;
import android.view.View;

import androidx.core.text.TextUtilsCompat;

import java.util.Locale;

class ViewUtils {

    public static int getViewPaddingLeft(View view){
        boolean isRtl = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            isRtl = TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == LayoutDirection.RTL;
        }
        int paddingStart = view.getPaddingStart();
        int paddingEnd = view.getPaddingEnd();
        int paddingLeft = view.getPaddingLeft();
        int paddingLeftMax;

        if (isRtl){
            if (paddingEnd != 0){
                paddingLeftMax = paddingEnd;
            }else {
                paddingLeftMax = paddingLeft;
            }
        }else {
            if (paddingStart != 0){
                paddingLeftMax = paddingStart;
            }else {
                paddingLeftMax = paddingLeft;
            }

        }

        return paddingLeftMax;
    }

    public static int getViewPaddingRight(View view){
        boolean isRtl = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            isRtl = TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == LayoutDirection.RTL;
        }
        int paddingStart = view.getPaddingStart();
        int paddingEnd = view.getPaddingEnd();
        int paddingRight = view.getPaddingRight();
        int paddingRightMax;
        if (isRtl){
            if (paddingStart != 0){
                paddingRightMax = paddingStart;
            }else {
                paddingRightMax = paddingRight;
            }
        }else {
            if (paddingEnd != 0){
                paddingRightMax = paddingEnd;
            }else {
                paddingRightMax = paddingRight;
            }

        }
        return paddingRightMax;
    }

    public static float getRtlValue(float startEndValue,float leftRightValue){
        float value;
        if (startEndValue != 0){
            value = startEndValue;
        }else {
            value = leftRightValue;
        }
        return value;
    }
}