package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Forecast implements Serializable {
    public String date; // 预报日期
    @SerializedName("tmp")
    public Temperature temperature; // 预报气温

    @SerializedName("cond")
    public More more;   // 更多信息

    public class Temperature implements Serializable{
        public String max;  //最高温
        public String min;  // 最低温
    }

    public class More implements Serializable{
        @SerializedName("txt_d")
        public String info; // 预测的天气信息
    }
}
