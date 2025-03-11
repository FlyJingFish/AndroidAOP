package com.flyjingfish.androidaop;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

import com.flyjingfish.android_aop_annotation.anno.AndroidAopModifyExtendsClass;

@AndroidAopModifyExtendsClass("com.flyjingfish.androidaop.BaseVActivity")
public class ReplaceBaseV extends AppCompatActivity {
}
