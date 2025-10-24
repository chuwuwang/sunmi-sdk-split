package com.sm.sdk.demo.basic;

import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.RadioButton;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.LogUtil;

public class LogControlActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_log_control);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.basic_log_control);
        findViewById(R.id.btn_ok).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                logControl();
                break;
        }
    }

    private void logControl() {
        try {
            RadioButton rdoOpenLog = findViewById(R.id.rdo_open_log);
            RadioButton rdoConsoleLog = findViewById(R.id.rdo_enable_console_log);
            RadioButton rdoLogToFile = findViewById(R.id.rdo_enable_log_to_file);
            Bundle bundle = new Bundle();
            bundle.putBoolean("openLog", rdoOpenLog.isChecked());
            bundle.putBoolean("showConsoleLog", rdoConsoleLog.isChecked());
            bundle.putBoolean("printLogToFile", rdoLogToFile.isChecked());
            int code = MyApplication.app.basicOptV2.logControl(bundle);
            LogUtil.e(TAG, "logControl(), code:" + code);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
