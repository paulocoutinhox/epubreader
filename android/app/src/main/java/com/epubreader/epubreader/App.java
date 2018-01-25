package com.epubreader.epubreader;

import android.app.Application;

public class App extends Application {

    private static Application instance;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        try {
            System.loadLibrary("epubreader");
        } catch (UnsatisfiedLinkError use) {
            //Logger.e("WARNING: Could not load native library");
        }
    }

    public static Application getInstance() {
        return instance;
    }

}
