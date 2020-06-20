package com.coolweather.android;

import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import org.litepal.LitePalApplication;

/**
 * auther : leo
 * create date : 2020/6/19
 * describe :
 */
public class BaseApplication extends LitePalApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences sp = getSharedPreferences("mode", MODE_PRIVATE);
        int skin = sp.getInt("skin", AppCompatDelegate.MODE_NIGHT_NO);

            AppCompatDelegate.setDefaultNightMode(skin);

    }
}
