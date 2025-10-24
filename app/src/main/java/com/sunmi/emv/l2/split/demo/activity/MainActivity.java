package com.sunmi.emv.l2.split.demo.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sunmi.emv.l2.split.demo.MyApplication;
import com.sunmi.emv.l2.split.demo.R;

public class MainActivity extends BaseAppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.emv_test);

        View view = findViewById(R.id.get_emv_info);
        TextView leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.get_emv_info);

        view = findViewById(R.id.nfc_ic_process);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.nfc_ic_process);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!MyApplication.app.isConnectPaySDK()) {
            MyApplication.app.bindPaySDKService();
        }
    }

    @Override
    public void onClick(View v) {
        if (!MyApplication.app.isConnectPaySDK()) {
            MyApplication.app.bindPaySDKService();
            showToast(R.string.connect_loading);
            return;
        }
        int id = v.getId();
        if (id == R.id.get_emv_info) {
            openActivity(GetEmvInfoActivity.class);
        } else if (id == R.id.nfc_ic_process) {
            openActivity(EmvTransactProcessActivity.class);
        }
    }
}
