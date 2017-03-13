package com.tsmile.mainthreadwatchdog;

import android.app.Activity;
import android.os.Bundle;
import android.os.Looper;
import android.os.MessageQueue;

import com.tsmile.debug.MainThreadWatchDog;

/**
 * Created by tsmile on 2017/3/13.
 */

public class TestMainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_man);
        initUi();
        Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
            @Override
            public boolean queueIdle() {
                MainThreadWatchDog.defaultInstance().stopWatch();
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeUiStatus();
    }

    private void initUi() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void resumeUiStatus() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
