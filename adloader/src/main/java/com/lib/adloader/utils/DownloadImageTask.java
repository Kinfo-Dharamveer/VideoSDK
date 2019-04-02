package com.lib.adloader.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    ImageView bmImage;

    public DownloadImageTask(ImageView bmImage, Bitmap placeholderImage) {
        this.bmImage = bmImage;
        if(placeholderImage != null) {
            this.bmImage.setImageBitmap(placeholderImage);
        }
    }

    protected Bitmap doInBackground(String... urls) {

        String urldisplay = urls[0];
        if (urldisplay.equals(""))
            return null;

        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            MLog.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        if(result != null) {
            bmImage.setImageBitmap(result);
        }
    }
}

// Usage:
// new DownloadImageTask((ImageView) findViewById(R.id.imageView1))
//        .execute(MY_URL_STRING);