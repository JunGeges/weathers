import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gaojun.hasee.weathers.R;
import com.gaojun.hasee.weathers.db.City;
import com.gaojun.hasee.weathers.db.County;
import com.gaojun.hasee.weathers.db.Province;
import com.gaojun.hasee.weathers.util.HttpUtil;
import com.gaojun.hasee.weathers.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/4/11.
 */

public class ChooseAreaFragment extends Fragment {
    private static final String TAG = "ChooseAreaFragment";
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog mProgressDialog;

    private Button backBtn;
    private TextView titleText;
    private ListView mListView;

    private ArrayAdapter<String> mArrayAdapter;

    private List<String> dataList = new ArrayList<>();

    private List<Province> mProvinceList = new ArrayList<>();//省集合
    private List<City> mCityList = new ArrayList<>();//市集合
    private List<County> mCountyList = new ArrayList<>();//县集合

    private Province selectedProvince;//选中的省
    private City selectedCity;//选中的市
    private County selectedCounty;//选中的县

    private int currentLevel;//当前等级

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        backBtn = (Button) view.findViewById(R.id.back_button);
        titleText = (TextView) view.findViewById(R.id.title_text);
        mListView = (ListView) view.findViewById(R.id.list_view);
        mArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, dataList);
        mListView.setAdapter(mArrayAdapter);
        return view;
    }

    //当这个fragment依附的activity初始化完成时的回调
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = mProvinceList.get(i);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = mCityList.get(i);
                    queryCounties();
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLevel == LEVEL_COUNTY) {
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    queryProvince();
                }
            }
        });
        queryProvince();
    }

    /**
     * 查询该市下所有的县，优先从数据库查询，如果没有查询到再去服务器查询
     */
    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        backBtn.setVisibility(View.VISIBLE);
        mCountyList=DataSupport.where("cityId=?",String.valueOf(selectedCity.getId())).find(County.class);
        if(mCountyList.size()>0){
            //把之前的市级数据清空
            dataList.clear();
            for (County county:mCountyList) {
                dataList.add(county.getCountyName());
            }
            mArrayAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel=LEVEL_COUNTY;
        }else {
            int selectProvinceCode=selectedProvince.getProvinceCode();
            int selectCityCode=selectedCity.getCityCode();
            Log.d(TAG, "queryCounties: "+selectProvinceCode+"---"+selectCityCode);
            String address="http://guolin.tech/api/china/"+selectProvinceCode+"/"+selectCityCode;
            queryFromServer(address,"county");
        }
    }

    /**
     * 查询该省下所有的市，优先从数据库查询，如果没有查询到再去服务器查询
     */
    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());
        backBtn.setVisibility(View.VISIBLE);
        mCityList = DataSupport.where("provinceId=?",String.valueOf(selectedProvince.getId())).find(City.class);
        if (mCityList.size() > 0) {
            dataList.clear();
            for (City city :mCityList) {
                dataList.add(city.getCityName());
            }
            mArrayAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel=LEVEL_CITY;
        }else {
            int provinceCode=selectedProvince.getProvinceCode();
            Log.d(TAG, "queryCities: "+provinceCode);
            String address="http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address,"city");
        }
    }

    /**
     * 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器查询
     */
    private void queryProvince() {
        titleText.setText("中国");
        backBtn.setVisibility(View.GONE);
        mProvinceList = DataSupport.findAll(Province.class);
        if ((mProvinceList.size() > 0)) {
            dataList.clear();
            //说明有数据库缓存
            for (Province province : mProvinceList) {
                dataList.add(province.getProvinceName());
            }
            mArrayAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            //从服务器获取
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }

    private void queryFromServer(String address, final String type) {
        //显示加载中的弹窗
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //这个方法执行在子线程不能直接更新UI
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getActivity(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();
                boolean result = false;
                if (type.equals("province")) {
                    result = Utility.handleProvinceResponse(responseStr);
                } else if (type.equals("city")) {
                    result = Utility.handleCityResponse(responseStr, selectedProvince.getId());
                } else if (type.equals("county")) {
                    result = Utility.handleCountyResponse(responseStr, selectedCity.getId());
                }

                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (type.equals("province")) {
                                queryProvince();
                                closeProgressDialog();
                            } else if (type.equals("city")) {
                                queryCities();
                                closeProgressDialog();
                            } else if (type.equals("county")) {
                                queryCounties();
                                closeProgressDialog();
                            }
                        }
                    });
                }
            }
        });
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("正在加载...");
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }

    private void closeProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }
}
