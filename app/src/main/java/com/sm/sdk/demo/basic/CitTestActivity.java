package com.sm.sdk.demo.basic;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.IOUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.Utility;

import java.io.FileWriter;

/** This page only used for device factory testing, please don't refer to any code */
public class CitTestActivity extends BaseAppCompatActivity {
    private TextView tvWakeAlarm;
    private TextView tvRst;
    private WakeAlarmReceiver receiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_cit_test);
        initView();
        initData();
    }

    private void initView() {
        initToolbarBringBack(R.string.basic_cit_test);
        findViewById(R.id.btn_wake_alarm).setOnClickListener(this);
        findViewById(R.id.btn_rst).setOnClickListener(this);
        tvWakeAlarm = findViewById(R.id.txt_wake_alarm_result);
        tvRst = findViewById(R.id.txt_rst_result);
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void initData() {
        receiver = new WakeAlarmReceiver();
        IntentFilter filter = new IntentFilter("com.sunmi.event.CIT_WAKE_ALARM");
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_wake_alarm:
                testWakeAlarm();
                break;
            case R.id.btn_rst:
                testRst();
                break;
        }
    }


    private void testWakeAlarm() {
        try {
            tvWakeAlarm.setText(null);
            //1.启动wake/alarm测试
            Bundle bundle = new Bundle();
            int code = MyApplication.app.basicOptV2.sysCitTest(0, bundle);
            String msg = "sysCitTest()->code:" + code;
            //2.拉wake节点
            writeNode("/sys/class/sunmi/base/sp_wakeup");
            LogUtil.e(TAG, msg);
            showToast(msg);
            runOnUiThreadEx(() -> {//5s后还未收到广播，认为测试失败
                if (TextUtils.isEmpty(tvWakeAlarm.getText())) {
                    String tip = "wake/alarm test failed";
                    tvWakeAlarm.setText(tip);
                }
            }, 1000L);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void testRst() {
        try {
            tvRst.setText(null);
            //1.拉rst节点
            writeNode("/sys/class/sunmi/base/sp_reset");
            //2.开始本地计时
            long startT = SystemClock.elapsedRealtime();
            IOUtil.delay(1000);
            //3.延迟1s获取SP启动时间
            Bundle bundle = new Bundle();
            int code = MyApplication.app.basicOptV2.sysCitTest(1, bundle);
            long endT = SystemClock.elapsedRealtime();
            String msg = "sysCitTest()->code:" + code;
            LogUtil.e(TAG, msg);
            showToast(msg);
            if (code != 0) {//测试失败
                msg = "rst test failed";
                tvRst.setText(msg);
                return;
            }
            //SP启动时间，单位：秒
            long startupTime = bundle.getInt("spStartupTime") * 1000L;
            long localTime = endT - startT;
            String status = Utility.getStateString(localTime >= startupTime);
            msg = Utility.formatStr("spStartupTime:%dms, localTime:%dms, rst test %s", startupTime, localTime, status);
            tvRst.setText(msg);
            LogUtil.e(TAG, msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 测试前执行 adb shell setenforce 0关闭SELinux，否则无权限 */
    private void writeNode(String node) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(node);
            writer.write("1");
            writer.flush();
            LogUtil.e(TAG, "write node:" + node + ", value:1, success.");
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            IOUtil.close(writer);
        }
    }

    private final class WakeAlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String status = intent.getStringExtra("cit_wake_alarm_status");
            LogUtil.e(TAG, "WakeAlarmReceiver-> action:" + action + ", cit_wake_alarm_status:" + status);
            if (!TextUtils.isEmpty(status)) {
                boolean bStatus = Boolean.parseBoolean(status);
                String msg = "cit wake/alarm status:" + bStatus;
                tvWakeAlarm.setText(msg);
                LogUtil.e(TAG, msg);
            }
        }
    }
}
