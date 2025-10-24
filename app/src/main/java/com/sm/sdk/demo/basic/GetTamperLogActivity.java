package com.sm.sdk.demo.basic;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sunmi.pay.hardware.aidl.AidlConstants;

public class GetTamperLogActivity extends BaseAppCompatActivity {
    private TextView tvResult;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_get_tamper_log);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.basic_get_tamper_log);
        findViewById(R.id.mb_ok).setOnClickListener(v -> getTamperLog());
        tvResult = findViewById(R.id.tv_result);
    }

    private void getTamperLog() {
        try {
            String tamperLog = MyApplication.app.basicOptV2.getSysParam(AidlConstants.SysParam.TAMPER_LOG);
            String msg = "tamperLog: \n" + tamperLog;
            Log.e(TAG, msg);
            tvResult.setText(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
