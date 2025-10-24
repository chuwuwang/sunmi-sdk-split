package com.sm.sdk.demo.basic;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.Utility;
import com.sunmi.pay.hardware.aidl.AidlConstants.LedLight;
import com.sunmi.pay.hardware.aidl.AidlConstants.SysParam;

/**
 * This page show how to control physical keyboard backlight on device with physical keyboard(such as P2_smartPad, P3K, P3KH, etc),
 * Test items contains turn on/off light, set light brightness value, etc.
 */
public class KBBacklightActivity extends BaseAppCompatActivity {
    private TextView tvBrightness;
    private TextView edtBrightness;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_kb_light);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.basic_kb_backlight);
        findViewById(R.id.mb_open).setOnClickListener(this);
        findViewById(R.id.mb_close).setOnClickListener(this);
        findViewById(R.id.mb_get_brightness).setOnClickListener(this);
        findViewById(R.id.mb_set_brightness).setOnClickListener(this);
        tvBrightness = findViewById(R.id.tv_brightness);
        edtBrightness = findViewById(R.id.edt_brightness);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mb_open:
                openBacklight();
                break;
            case R.id.mb_close:
                closeBacklight();
                break;
            case R.id.mb_get_brightness:
                getBrightness();
                break;
            case R.id.mb_set_brightness:
                setBrightness();
                break;
        }
    }

    /** Open backlight */
    private void openBacklight() {
        try {
            int code = MyApplication.app.basicOptV2.ledStatusOnDevice(LedLight.WHITE_LIGHT, 0);
            LogUtil.e(TAG, "ledStatusOnDevice()->open keyboard backlight, code:" + code);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Close backlight */
    private void closeBacklight() {
        try {
            int code = MyApplication.app.basicOptV2.ledStatusOnDevice(LedLight.WHITE_LIGHT, 1);
            LogUtil.e(TAG, "ledStatusOnDevice()->close keyboard backlight, code:" + code);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get keyboard backlight brightness
     * Only support on P3K or P3KH
     */
    private void getBrightness() {
        try {
            String value = MyApplication.app.basicOptV2.getSysParam(SysParam.KB_BRIGHTNESS);
            if (!TextUtils.isEmpty(value) && TextUtils.isDigitsOnly(value)) {
                tvBrightness.setText(value);
            } else {
                showToast("get brightness failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Set keyboard backlight brightness
     * Only support on P3K or P3KH
     */
    private void setBrightness() {
        try {
            String value = edtBrightness.getText().toString();
            if (TextUtils.isEmpty(value)) {
                showToast("brightness value shouldn't be empty");
                edtBrightness.requestFocus();
                return;
            }
            int iv = Integer.parseInt(value);
            if (iv < 1 || iv > 100) {
                showToast("brightness value range should be in [1,100]");
                edtBrightness.requestFocus();
                return;
            }
            int code = MyApplication.app.basicOptV2.setSysParam(SysParam.KB_BRIGHTNESS, value);
            LogUtil.e(TAG, "set brightness, code:" + code);
            showToast("set brightness " + Utility.getStateString(code));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
