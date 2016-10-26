package com.siscos.interviewapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

public class ImageDownLoader extends AsyncTask<Object, ImageView, Bitmap> {

    ImageView imageView = null;
    @Override
    protected Bitmap doInBackground(Object... params) {
        Bitmap mBitmap = null;
        imageView = (ImageView)params[0];
        String url = (String)params[1];
        InputStream in = null;
        try {
            in = new java.net.URL(url).openStream();
            mBitmap = BitmapFactory.decodeStream(in);
            in.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return mBitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bm) {
        super.onPostExecute(bm);
        imageView.setImageBitmap(bm);
    }
}
