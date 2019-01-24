package com.qiniu.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

public final class Json {
    private Json() {
    }

    public static String encode(StringMap map) {
        return new Gson().toJson(map.map());
    }

    public static String encode(Object obj) {
        return new GsonBuilder().serializeNulls().create().toJson(obj);
    }

    public static <T> T decode(String json, Class<T> classOfT) {
        return new Gson().fromJson(json, classOfT);
    }

    public static <T> T decode(JsonElement jsonElement, Class<T> clazz) {
        Gson gson = new Gson();
        return gson.fromJson(jsonElement, clazz);
    }

    public static StringMap decode(String json) {
        // CHECKSTYLE:OFF
        Type t = new TypeToken<Map<String, Object>>() {
        }.getType();
        // CHECKSTYLE:ON
        Map<String, Object> x = new Gson().fromJson(json, t);
        return new StringMap(x);
    }
}
