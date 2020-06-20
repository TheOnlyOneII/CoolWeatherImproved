package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * 对 Basic 、AQI 、Now 、Suggestion 和 Forecast 类进行了引用。其中，
 * 由于 daily_forecast 中包含的是一个数组，因此这里使用了 List 集合来引用 Forecast 类。另外，
 * 返回的天气数据中还会包含一项 status 数据，成功则返回 ok ，失败会返回具体的原因，这里也做了引用。
 */
public class Weather implements Serializable {
    // 引用其他类
    public String status;   // status数据，成功返回ok
    public Basic basic;
    public AQI aqi;
    public Now now;
    public Suggestion suggestion;
    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
}
