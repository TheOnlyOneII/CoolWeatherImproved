package com.coolweather.android;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.coolweather.android.gson.Weather;
import com.coolweather.android.service.AutoUpdateService;
import com.coolweather.android.util.CacheUtils;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 首先在OnCreate()方法中获取一些控件的实例，然后尝试从本地缓存中读取天气数据。
 * 如果本地没有缓存就从 Intent 中取出天气 id
 * 并调用requestWeather()方法来从服务器请求天气数据。如果缓存中有数据，则直接解析，并显示天气数据。
 */
public class WeatherActivity extends AppCompatActivity {


    private ViewPager mVpContainer;
    private VpAdapter mVpAdapter;
    private ArrayList<Fragment> mFmList;
    private String mWeather_id;
    private LinearLayout mLlPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_weather);

        //初始化各部件
        mVpContainer = findViewById(R.id.vpContainer);
        mLlPoint = findViewById(R.id.ll_point);

        mFmList = new ArrayList<>();

        initViewPager();


        mWeather_id = getIntent().getStringExtra("weather_id");
        if (mWeather_id != null)
        {
            requestWeather(mWeather_id);
        }
        else
        {
            showFm();
        }


    }

    private void initViewPager() {
        mVpAdapter = new VpAdapter(getSupportFragmentManager(), this, mFmList);
        mVpContainer.setAdapter(mVpAdapter);

        mVpContainer.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                for (int j = 0; j < mLlPoint.getChildCount(); j++) {
                    View v = mLlPoint.getChildAt(j);
                    v.setSelected(j == position ? true : false);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    /**
     * 根据天气Id请求城市天气信息
     */
    public void requestWeather(final String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId
                + "&key=8e3cfe337e5e43c0b9ae8cfe9d36eab0"; // 这里的key设置为第一个实训中获取到的API Key

        // 组装地址并发出请求
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);   // 将返回数据转换为Weather对象
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            //缓存有效的weather对象(实际上缓存的是字符串)
                            SharedPreferences sp = PreferenceManager
                                    .getDefaultSharedPreferences(WeatherActivity.this);
                            SharedPreferences.Editor editor = sp.edit();
                            Set<String> weatherSet = sp.getStringSet("weather", new HashSet<String>());
                            weatherSet.add(responseText);
                            editor.putStringSet("weather", weatherSet);

                            Set<String> ids = sp.getStringSet("id", new HashSet<String>());
                            ids.add(weatherId);
                            editor.putStringSet("id",ids);
                            editor.apply();


                            HashMap<String,String> data = (HashMap<String, String>) CacheUtils.get(WeatherActivity.this).getAsObject("data");
                            if (data == null){
                                data = new HashMap<>();
                            }
                            data.put(weatherId,responseText);
                            CacheUtils.get(WeatherActivity.this).put("data",data);

                            showFm();

                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
//                        swipeRefreshLayout.setRefreshing(false);//请求结束后，调用方法表示刷新时间结束，并隐藏进度条
                    }
                });
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        loadBingPic();
    }


    /**
     * 显示fragment
     */
    private void showFm() {
        HashMap<String,String> datas = (HashMap<String, String>) CacheUtils.get(this).getAsObject("data");
        Set<Map.Entry<String, String>> set = datas.entrySet();
        mFmList.clear();

        for (Map.Entry<String,String> entry : set)
        {
            mFmList.add(WeatherFragment.newInstance(entry.getValue()));
        }

       initViewPager();
//        mVpAdapter.notifyDataSetChanged();


        //添加 指示点
        mLlPoint.removeAllViews();
        for (int i = 0; i < mFmList.size(); i++) {
            ImageView iv = new ImageView(this);
            iv.setImageResource(R.drawable.selector_point);
            if (i == 0){
                iv.setSelected(true);
            }
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(10,0,10,0);
            iv.setLayoutParams(lp);
            mLlPoint.addView(iv);
        }


        //显示添加的城市
        if ( getIntent().getBooleanExtra("isNew",false)){
            int i = 0;
            for (Map.Entry<String,String> entry : set)
            {
              if (entry.getKey().equals(mWeather_id))
              {
                  mVpContainer.setCurrentItem(i);
                  for (int j = 0; j < mLlPoint.getChildCount(); j++) {
                      View v = mLlPoint.getChildAt(j);
                      v.setSelected(j == i ? true : false);
                  }
                  break;
              }
              i++;
            }

        }

    }

    /**
     * 加载必应每日一图
     */
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.
                        getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
//                    }
//                });
            }
        });
    }

    public void startService(String weather){
        Intent intent = new Intent(this, AutoUpdateService.class);
        intent.putExtra("weather",weather);
        startService(intent);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK)
        {
            showFm();
//            recreate();
        }
    }

    public void skin() {
        SharedPreferences sp = getSharedPreferences("mode", MODE_PRIVATE);
        int skin = sp.getInt("skin", AppCompatDelegate.MODE_NIGHT_NO);
        if (skin == AppCompatDelegate.MODE_NIGHT_NO)
        {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        }
        else
        {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        recreate();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = getSharedPreferences("mode", MODE_PRIVATE).edit();
        editor.putInt("skin",AppCompatDelegate.getDefaultNightMode());
        editor.commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        System.exit(0);
    }
}


