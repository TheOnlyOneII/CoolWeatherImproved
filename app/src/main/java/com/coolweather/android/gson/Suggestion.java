package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Suggestion implements Serializable {
    @SerializedName("comf")
    public Comfort comfort; // 舒适度

    @SerializedName("cw")
    public CarWash carWash; // 洗车建议
    public Sport sport; // 运动建议

    public class Comfort implements Serializable{
        @SerializedName("txt")
        public String info;
    }

    public class CarWash implements Serializable{
        @SerializedName("txt")
        public String info;
    }

    public class Sport implements Serializable{
        @SerializedName("txt")
        public String info;
    }
}
