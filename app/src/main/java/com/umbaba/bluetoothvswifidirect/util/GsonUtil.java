package com.umbaba.bluetoothvswifidirect.util;


import com.google.gson.GsonBuilder;

public class GsonUtil {
    public static String gsonToJson(Object src) {
        return
                new GsonBuilder()
                        .create()
                        .toJson(src);
    }

    public static <T> T gsonFromJson(String json, Class<T> classOfT) {
        return
                new GsonBuilder()
                        .create()
                        .fromJson(json, classOfT);
    }

    public static String gsonToJson(Object src, Class tClass) {
        return
                new GsonBuilder()
                        .create()
                        .toJson(src, tClass);
    }

}

