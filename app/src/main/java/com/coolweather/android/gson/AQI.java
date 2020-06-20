package com.coolweather.android.gson;

import java.io.Serializable;

public class AQI implements Serializable {
    public AQICity city;    // 城市

    public class AQICity implements Serializable{
        public String aqi;  // 空气质量指数
        public String pm25; // pm2.5浓度
    }
}
