package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Now implements Serializable {
    @SerializedName("tmp")
    public String temperature;  // 当前温度

    @SerializedName("cond")
    public More more;   // 更多信息

    public class More implements Serializable{
        @SerializedName("txt")
        public String info; // 天气信息
    }
}
