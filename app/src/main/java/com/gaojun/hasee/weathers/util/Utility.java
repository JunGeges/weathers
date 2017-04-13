package com.gaojun.hasee.weathers.util;

import android.text.TextUtils;

import com.gaojun.hasee.weathers.db.City;
import com.gaojun.hasee.weathers.db.County;
import com.gaojun.hasee.weathers.db.Province;
import com.gaojun.hasee.weathers.gson.HeWeather5Bean;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 高俊 on 2017/4/10.
 */

public class Utility {
    /**
     * 解析和处理服务器返回的省级数据
     */
    public static boolean handleProvinceResponse(String response){
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allProvince=new JSONArray(response);
                for (int i = 0; i < allProvince.length(); i++) {
                    JSONObject provinceObject = allProvince.getJSONObject(i);
                    Province province=new Province();
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.setProvinceName(provinceObject.getString("name"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    /**
     *  解析服务器返回的市级数据
     */
    public static boolean handleCityResponse(String response,int provinceId){
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCity=new JSONArray(response);
                for (int i = 0; i < allCity.length(); i++) {
                    JSONObject cityObject= allCity.getJSONObject(i);
                    City city=new City();
                    city.setCityCode(cityObject.getInt("id"));
                    city.setCityName(cityObject.getString("name"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析服务器返回的县级数据
     */

    public static boolean handleCountyResponse(String response,int cityId){
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCounty =new JSONArray(response);
                for (int i = 0; i < allCounty.length(); i++) {
                    JSONObject countyObject=allCounty.getJSONObject(i);
                    County county=new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(cityId);
                    //存到数据库
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public  static HeWeather5Bean handleWeatherResponse(String response){
        try {
            JSONObject jsonObject=new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather5");
            String weatherContent = jsonArray.get(0).toString();
            return new Gson().fromJson(weatherContent,HeWeather5Bean.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
