package com.coolweather.android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.swipelayout.SwipeLayoutManager;
import com.coolweather.android.util.CacheUtils;
import com.coolweather.android.util.Utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CityManagementActivity extends AppCompatActivity {

    private RecyclerView mRvList;
    private ArrayList<String> mIdList;
    private ArrayList<String> mDatas;
    private RvCityAdapter mRvCityAdapter;
    private boolean mRefreshState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_management);


        SwipeLayoutManager.getInstance().closeOpenInstance();

        ImageView ivBg = findViewById(R.id.iv_bg);


        mRvList = findViewById(R.id.rv_list);
        findViewById(R.id.tv_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CityManagementActivity.this,AddCityActivity.class));
            }
        });

        mRvList.setLayoutManager(new LinearLayoutManager(this));

        mDatas = new ArrayList<>();
        mIdList = new ArrayList<>();
        mRvCityAdapter = new RvCityAdapter(this, mDatas, mIdList);
        mRvList.setAdapter(mRvCityAdapter);
        mRvCityAdapter.setOnClickCallBack(new RvCityAdapter.OnClickCallBack() {
            @Override
            public void onItemDelete(String id) {
                if (mDatas.size() == 1)
                {
                    Toast.makeText(getBaseContext(),"至少保留一个城市",Toast.LENGTH_LONG).show();
                    return;
                }
                SwipeLayoutManager.getInstance().closeOpenInstance();
                HashMap<String,String> map = (HashMap<String, String>) CacheUtils.get(CityManagementActivity.this).getAsObject("data");

                Set<Map.Entry<String, String>> set = map.entrySet();
                for (Map.Entry<String, String> entry : set)
                {
                    if (entry.getKey().equals(id))
                    {
                        map.remove(id);
                        CacheUtils.get(CityManagementActivity.this).put("data",map);
                        break;
                    }
                }


                mDatas.clear();
                mIdList.clear();
                for (Map.Entry<String,String> s : set)
                {
                    String value = s.getValue();
                    Weather weather = Utility.handleWeatherResponse(value);
                    mDatas.add(weather.basic.cityName);
                    mIdList.add(weather.basic.weatherId);
                }

                mRvCityAdapter.notifyDataSetChanged();

                mRefreshState = true;
            }
        });


        HashMap<String,String> map = (HashMap<String, String>) CacheUtils.get(this).getAsObject("data");
        if (map == null)
        {
            map = new HashMap<>();
            CacheUtils.get(this).put("data",map);
        }

        Set<Map.Entry<String, String>> entrySet = map.entrySet();
        for (Map.Entry<String,String> set : entrySet)
        {
            String value = set.getValue();
            Weather weather = Utility.handleWeatherResponse(value);
            mDatas.add(weather.basic.cityName);
            mIdList.add(weather.basic.weatherId);
        }

        mRvCityAdapter.notifyDataSetChanged();
    }

    public void back(View view) {
        if (mRefreshState)
        {
            Log.e("tag"," ------------ setResult -1  ");
            setResult(RESULT_OK);
        }
        finish();
    }
}
