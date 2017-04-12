package com.gaojun.hasee.weathers;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    long firstTime=0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        long secondTime=System.currentTimeMillis();
        if(keyCode==KeyEvent.KEYCODE_BACK&&event.getAction()==KeyEvent.ACTION_DOWN){
            if(secondTime-firstTime>2000){
                firstTime=secondTime;
                Toast.makeText(MainActivity.this,"再按一次退出应用程序",Toast.LENGTH_SHORT).show();
            }else {
                finish();
            }
        }
        return true;
    }
}
