package com.gaojun.hasee.weathers.db;

import org.litepal.crud.DataSupport;

/**
 * Created by 高俊 on 2017/4/10.
 */

public class City extends DataSupport {

    private int id;

    private String cityName;

    private int cityCode;

    private int provinceId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getCityCode() {
        return cityCode;
    }

    public void setCityCode(int cityCode) {
        this.cityCode = cityCode;
    }

    public int getProvinceid() {
        return provinceId;
    }

    public void setProvinceid(int provinceid) {
        this.provinceId = provinceid;
    }
}
