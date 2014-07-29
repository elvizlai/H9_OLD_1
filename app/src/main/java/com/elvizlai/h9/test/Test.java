package com.elvizlai.h9.test;

import android.util.Log;

import com.elvizlai.h9.BuildConfig;

/**
 * Created by elvizlai on 14-3-25.
 */
public class Test {
    private static final String TAG = "Elvizlai";

    public static void LogD(String str) {
        if (BuildConfig.DEBUG) {
            Log.d("", "*******************************Elvizlai*******************************");
            Log.d(TAG, str);
            Log.d("", "*******************************Elvizlai*******************************");
        }
    }
}
