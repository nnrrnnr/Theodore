package com.github.watanabear.sampleapplication;

import android.app.Application;

import com.github.watanabear.sampleapplication.util.Theodore;

/**
 * Created by ryo on 2017/06/18.
 */

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Theodore.init(this);
    }
}
