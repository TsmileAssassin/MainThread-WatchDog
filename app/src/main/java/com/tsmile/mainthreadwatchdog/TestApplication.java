package com.tsmile.mainthreadwatchdog;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.tsmile.debug.MainThreadWatchDog;

/**
 * Created by tsmile on 2017/3/13.
 */

public class TestApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        MainThreadWatchDog.defaultInstance().startWatch();
        initFunc1();
        initFunc2();
        initFunc3();
    }

    private void initFunc1() {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void initFunc2() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void initFunc3() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
