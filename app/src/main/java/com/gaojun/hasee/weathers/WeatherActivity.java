package com.gaojun.hasee.weathers;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gaojun.hasee.weathers.gson.HeWeather5Bean;
import com.gaojun.hasee.weathers.util.HttpUtil;
import com.gaojun.hasee.weathers.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    private static final String TAG = "WeatherActivity";

    private ScrollView weatherLayout;
    private TextView mTitleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private ImageView bingPicImg;
    public SwipeRefreshLayout mSwipeRefreshLayout;
    public DrawerLayout mDrawerLayout;
    private Button navButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>=21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            |View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        initViews();
    }

    private void initViews() {
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        mTitleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        bingPicImg=(ImageView)findViewById(R.id.bing_pic_img);
        navButton=(Button)findViewById(R.id.nav_button);
        mDrawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);

        mSwipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        final String countyName;//加载天气的城市
        //判断有没有缓存
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = preferences.getString("weather", null);
        String bingPic=preferences.getString("bing_pic",null);
        if(bingPic!=null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else {
            loadBingPic();
        }
        if (weatherString != null) {
            HeWeather5Bean heWeather5Bean = Utility.handleWeatherResponse(weatherString);
            countyName=heWeather5Bean.getBasic().getCity();
            showWeatherInfo(heWeather5Bean);
        } else {
            countyName = getIntent().getStringExtra("county_name");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(countyName);
        }

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(countyName);
            }
        });

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    private void loadBingPic() {
        String address="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPicResp = response.body().string();
                SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                edit.putString("bing_pic",bingPicResp);
                edit.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPicResp).into(bingPicImg);
                    }
                });
            }
        });
    }

    /**
     * 请求服务器天气数据
     */
    public void requestWeather(String countyName) {
        Log.d(TAG, "requestWeather: " + countyName);
        String address = "https://free-api.heweather.com/v5/weather?city=" + countyName + "&key=230968cc10bf4505b8b562b47305cc7f";
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String weatherInfoResp = response.body().string();
                final HeWeather5Bean heWeather5Bean = Utility.handleWeatherResponse(weatherInfoResp);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (heWeather5Bean != null && "ok".equals(heWeather5Bean.getStatus())) {
                            SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            edit.putString("weather", weatherInfoResp);
                            edit.apply();
                            showWeatherInfo(heWeather5Bean);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();
    }

    /**
     * 展示天气信息到界面上
     */
    private void showWeatherInfo(HeWeather5Bean heWeather5Bean) {
        String cityName = heWeather5Bean.getBasic().getCity();
        String updateTime = heWeather5Bean.getBasic().getUpdate().getLoc().split(" ")[1];
        String degree = heWeather5Bean.getNow().getTmp() + "℃";
        String weatherInfo = heWeather5Bean.getNow().getCond().getTxt();
        mTitleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        //先清空布局里面的预报天气
        forecastLayout.removeAllViews();
        for (HeWeather5Bean.DailyForecastBean dailyForecastBean : heWeather5Bean.getDaily_forecast()) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dataText = (TextView) view.findViewById(R.id.date_text);
            TextView maxDegree = (TextView) view.findViewById(R.id.max_text);
            TextView minDegree = (TextView) view.findViewById(R.id.min_text);
            TextView weatherInfoText = (TextView) view.findViewById(R.id.info_text);
            dataText.setText(dailyForecastBean.getDate());
            maxDegree.setText(dailyForecastBean.getTmp().getMax());
            minDegree.setText(dailyForecastBean.getTmp().getMin());
            weatherInfoText.setText(dailyForecastBean.getCond().getTxt_d());
            forecastLayout.addView(view);
        }
        if(heWeather5Bean.getAqi()!=null){
            aqiText.setText(heWeather5Bean.getAqi().getCity().getAqi());
            pm25Text.setText(heWeather5Bean.getAqi().getCity().getPm25());
        }
        String ComfTxt = "舒适度:" + heWeather5Bean.getSuggestion().getComf().getTxt();
        comfortText.setText(ComfTxt);
        String cwtxt = "洗车指数:" + heWeather5Bean.getSuggestion().getCw().getTxt();
        carWashText.setText(cwtxt);
        String sportTxt = "运动建议:" + heWeather5Bean.getSuggestion().getSport().getTxt();
        sportText.setText(sportTxt);
        weatherLayout.setVisibility(View.VISIBLE);
    }
}
