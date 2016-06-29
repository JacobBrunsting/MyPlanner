package com.myplanner.myplanner;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.myplanner.myplanner.UserData.DataRetriever;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread loadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                DataRetriever.getInstance().LoadData(getApplicationContext());
                startActivity(new Intent(SplashActivity.this, Main.class));
            }
        });
        loadThread.start();
    }
}
