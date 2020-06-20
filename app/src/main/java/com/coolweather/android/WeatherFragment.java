package com.coolweather.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.coolweather.android.gson.Forecast;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.service.AutoUpdateService;
import com.coolweather.android.util.CacheUtils;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * auther : leo
 * create date : 2020/6/17
 * describe :
 */
public class WeatherFragment extends Fragment {

    private View mInflate;
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private ImageView bingPicImg;
    public SwipeRefreshLayout swipeRefreshLayout;
    private String WeatherId;//记录城市天气ID
    public DrawerLayout drawerLayout;
    private Button navButton;
    private String mWeatherString;

    public static WeatherFragment newInstance(String weather) {

        Bundle args = new Bundle();
        args.putString("weather",weather);
        WeatherFragment fragment = new WeatherFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mInflate = inflater.inflate(R.layout.fragment_weather, container, false);
        initView();
        initData();
        return mInflate;
    }

    private void initData() {
        mInflate.findViewById(R.id.tv_skin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("tag","---------");

                ((WeatherActivity)getActivity()).skin();
            }
        });
    }

    private void initView() {
        weatherLayout = mInflate.findViewById(R.id.weather_layout);
        titleCity = mInflate.findViewById(R.id.title_city);
        titleUpdateTime = mInflate.findViewById(R.id.title_update_time);
        degreeText = mInflate.findViewById(R.id.degree_text);
        weatherInfoText = mInflate.findViewById(R.id.weather_info_text);
        forecastLayout = mInflate.findViewById(R.id.forecast_layout);
        aqiText = mInflate.findViewById(R.id.aqi_text);
        pm25Text = mInflate.findViewById(R.id.pm25_text);
        comfortText = mInflate.findViewById(R.id.comfort_text);
        carWashText = mInflate.findViewById(R.id.car_wash_text);
        sportText = mInflate.findViewById(R.id.sport_text);
        bingPicImg = mInflate.findViewById(R.id.bing_pic_img);
        swipeRefreshLayout = mInflate.findViewById(R.id.swipe_refresh);
        navButton = mInflate.findViewById(R.id.nav_button);
        drawerLayout = mInflate.findViewById(R.id.drawer_layout);

        //调用setColorSchemeResources方法设置下拉刷新进度条颜色
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
//        String weatherString = prefs.getString("weather", null);
        mWeatherString = getArguments().getString("weather");

        if (mWeatherString != null) {
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(mWeatherString);
            WeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        } else {
            //无缓存时去服务器查询数据
            WeatherId = getActivity().getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);    //暂时将ScrollView设为不可见
            requestWeather(WeatherId);
        }

        //下拉刷新监听器
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(WeatherId);
            }
        });

        //设置滑动器监听
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                drawerLayout.openDrawer(GravityCompat.START);//打开滑动菜单

                getActivity().startActivityForResult(new Intent(getContext(),CityManagementActivity.class),0);
            }
        });

        //获取必应一图
        String bingPic = prefs.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(getContext()).load(bingPic).into(bingPicImg);
        } else {
            loadBingPic();  //下次打代码要小心
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
                        getDefaultSharedPreferences(getActivity()).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(getActivity()).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }

    /**
     * 处理并展示Weather实体类中的数据
     *
     * @param weather
     */
    private void showWeatherInfo(Weather weather) {
        // 从Weather对象中获取数据
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1]; //按24小时计时的时间
        String degree = weather.now.temperature + "°C";
        String weatherInfo = weather.now.more.info;
        // 将数据显示到对应控件上
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {    // 循环处理每天的天气信息
            View view = LayoutInflater.from(getContext()).inflate(R.layout.forecast_item, forecastLayout, false);
            // 加载布局
            TextView dateText = view.findViewById(R.id.date_text);
            TextView infoText = view.findViewById(R.id.info_text);
            TextView maxText = view.findViewById(R.id.max_text);
            TextView minText = view.findViewById(R.id.min_text);
            // 设置数据
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            // 添加到父布局
            forecastLayout.addView(view);
        }
        if (weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度: " + weather.suggestion.comfort.info;
        String carWash = "洗车指数: " + weather.suggestion.carWash.info;
        String sport = "运动建议: " + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);  // 将天气信息设置为可见

        // 在showWeatherInfo()方法的最后加入了启动 AutoUpdateService 这个服务的代码
        // 这样只要选中了某个城市并成功更新天气之后， AutoUpdateService 就会一直在后台运行，每小时更新一次天气。
//        Intent intent = new Intent(getActivity(), AutoUpdateService.class);
//        getActivity().startService(intent);
        ((WeatherActivity)getActivity()).startService(mWeatherString);
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
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            //缓存有效的weather对象(实际上缓存的是字符串)
                            SharedPreferences sp = PreferenceManager
                                    .getDefaultSharedPreferences(getActivity());

                            Set<String> weatherSet = sp.getStringSet("weather", new HashSet<String>());
                            weatherSet.add(responseText);
                            SharedPreferences.Editor editor = sp.edit();


//                            editor.putString("weather", responseText);
                            editor.putStringSet("weather", weatherSet);
                            Set<String> ids = sp.getStringSet("id", new HashSet<String>());
                            ids.add(weatherId);
                            editor.putStringSet("id",ids);

                            editor.apply();

                            HashMap<String,String> data = (HashMap<String, String>) CacheUtils.get(getActivity()).getAsObject("data");
                            if (data == null){
                                data = new HashMap<>();
                            }
                            data.put(weatherId,responseText);
                            CacheUtils.get(getActivity()).put("data",data);


                            showWeatherInfo(weather);   // 显示内容
                        } else {
                            Toast.makeText(getActivity(), "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);//请求结束后，调用方法表示刷新时间结束，并隐藏进度条
                    }
                });
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "获取天气信息失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        loadBingPic();
    }
}
