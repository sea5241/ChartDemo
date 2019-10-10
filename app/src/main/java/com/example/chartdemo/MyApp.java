package com.example.chartdemo;

import android.app.Application;
import android.content.Context;

/**
 * MyApp
 *
 * @author sea
 * @date 2019/9/28
 */
public class MyApp extends Application {
    private static Context mContext;
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }
    public static Context getAppContext(){
        return mContext;
    }
}
