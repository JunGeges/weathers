package com.gaojun.hasee.weathers.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by 高俊 on 2017/4/10.
 */

public class HttpUtil {

    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        //异步请求数据(GET)
        OkHttpClient client=new OkHttpClient();
        Request request=new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}
