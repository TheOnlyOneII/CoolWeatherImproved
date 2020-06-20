package com.coolweather.android.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 先在onStartCommand()方法中调用updateWeather()方法来更新天气，然后调用updateBigPic()方法更新背景图片。
 * 这里将更新后的数据直接存储到 SharedPreferences 文件
 * 之后就设置了一个定时任务，每小时执行一次 AutoUpdateService 的onStartCommand()方法。
 */
public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }
    // tijiaoceshi
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String weather = intent.getStringExtra("weather");
        updateWeather(weather);    // 更新天气
        updateBingPic();    // 更新背景图片
        //定时任务
        AlarmManager manager = (AlarmManager)getSystemService(ALARM_SERVICE);
        int anHour = 60 * 60 * 1000; // 1 小时
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;    // 设置触发时间
        Intent i = new Intent(this,AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this,0,i,0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand(intent,flags,startId);
    }

    /**
     * 更新天气信息
     */
    private void updateWeather(String s) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//        String weatherString = prefs.getString("weather", null);
        String weatherString = s;
        if (weatherString != null) {
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            String weatherId = weather.basic.weatherId;
            String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId
                    + "&key=8e3cfe337e5e43c0b9ae8cfe9d36eab0";  // 注意此处替换为之前申请的 API Key
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    final String responseText = response.body().string();
                    final Weather weather = Utility.handleWeatherResponse(responseText);
                    if (weather != null && "ok".equals(weather.status)){
                        //缓存有效的weather对象(实际上缓存的是字符串)
                        SharedPreferences sp = PreferenceManager
                                .getDefaultSharedPreferences(AutoUpdateService.this);
                        SharedPreferences.Editor editor = sp.edit();
                        Set<String> weatherSet = sp.getStringSet("weather", new HashSet<String>());
                        weatherSet.add(responseText);
                        editor.putStringSet("weather", weatherSet);
                        editor.apply();
                    }
                }
            });
        }
    }

    /**
     * 更行必应每日一图
     */
    private void updateBingPic(){
        String requestBingPic="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
            }
        });
    }

}
