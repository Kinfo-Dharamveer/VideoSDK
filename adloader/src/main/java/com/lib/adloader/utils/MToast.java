package com.lib.adloader.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.widget.Toast;

public class MToast extends Toast {
    static final boolean LOG = false;

    /**
     * Construct an empty Toast object.  You must call {@link #setView} before you
     * can call {@link #show}.
     *
     * @param context The context to use.  Usually your {@link Application}
     *                or {@link Activity} object.
     */
    public MToast(Context context) {
        super(context);
    }

//    public static MToast makeText(Context context, CharSequence text, int duration) {
//        return (MToast) Toast.makeText(context, text, duration);
//    }
//
//    @Override
//    public void show() {
//        if (LOG) super.show();
//    }
}
