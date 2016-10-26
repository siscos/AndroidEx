package com.siscos.interviewapp;

import android.app.ProgressDialog;
import android.content.Context;

public class ProgressDialogSingleton {
    private static ProgressDialogSingleton instance = new ProgressDialogSingleton();
    private ProgressDialog progressDialog = null;

    private ProgressDialogSingleton(){

    }

    public static ProgressDialogSingleton getInstance(){
        return instance;
    }

    public ProgressDialog getProgressDialog(Context c){
        if (progressDialog != null)
            return progressDialog;

        progressDialog = new ProgressDialog(c);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(c.getString(R.string.loading));
        return progressDialog;
    }
}
