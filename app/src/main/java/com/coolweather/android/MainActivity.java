package com.coolweather.android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import java.util.HashSet;
import java.util.Set;

/**
 * 在应用刚启动时，需要判断是否有缓存数据，进而决定进入哪个页面。
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /**
         * 先从 SharedPreferences 文件中读取缓存数据，如果不为空
         * 则说明之前已经请求过天气数据了，就没必要让用户再次选择城市，而是直接跳转到 WeatherActivity 即可
         */
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // 从 SharedPreferences 中读取缓存数据
        Set weatherSet = prefs.getStringSet("weather", new HashSet<String>());
        if(weatherSet.isEmpty() == false){
            // 之前请求过则直接跳转到天气信息
            Intent intent = new Intent(this, WeatherActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
