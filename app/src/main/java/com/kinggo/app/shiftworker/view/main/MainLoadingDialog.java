package com.kinggo.app.shiftworker.view.main;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kinggo.app.shiftworker.R;

public class MainLoadingDialog extends Dialog {

    public MainLoadingDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_main_loading);
    }
}