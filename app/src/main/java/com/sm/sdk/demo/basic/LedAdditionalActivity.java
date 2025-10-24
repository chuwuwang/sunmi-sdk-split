package com.sm.sdk.demo.basic;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sunmi.pay.hardware.aidl.AidlConstants.LedLight;

public class LedAdditionalActivity extends BaseAppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_led_additional);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.basic_led_additional);
        findViewById(R.id.mb_corner_red_open).setOnClickListener(this);
        findViewById(R.id.mb_corner_red_close).setOnClickListener(this);
        findViewById(R.id.mb_corner_green_open).setOnClickListener(this);
        findViewById(R.id.mb_corner_green_close).setOnClickListener(this);
        findViewById(R.id.mb_corner_blue_open).setOnClickListener(this);
        findViewById(R.id.mb_corner_blue_close).setOnClickListener(this);
        findViewById(R.id.mb_corner_white_open).setOnClickListener(this);
        findViewById(R.id.mb_corner_white_close).setOnClickListener(this);
        findViewById(R.id.mb_indicator_yellow_open).setOnClickListener(this);
        findViewById(R.id.mb_indicator_yellow_close).setOnClickListener(this);
        findViewById(R.id.mb_charge_red_open).setOnClickListener(this);
        findViewById(R.id.mb_camera_indicator_open).setOnClickListener(this);
        findViewById(R.id.mb_camera_indicator_close).setOnClickListener(this);
        findViewById(R.id.mb_charge_red_close).setOnClickListener(this);
        findViewById(R.id.mb_charge_green_open).setOnClickListener(this);
        findViewById(R.id.mb_charge_green_close).setOnClickListener(this);
        findViewById(R.id.mb_charge_blue_open).setOnClickListener(this);
        findViewById(R.id.mb_charge_blue_close).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mb_corner_red_open:
                ledControl(LedLight.CORNER_RED_LIGHT, 0);
                break;
            case R.id.mb_corner_red_close:
                ledControl(LedLight.CORNER_RED_LIGHT, 1);
                break;
            case R.id.mb_corner_green_open:
                ledControl(LedLight.CORNER_GREEN_LIGHT, 0);
                break;
            case R.id.mb_corner_green_close:
                ledControl(LedLight.CORNER_GREEN_LIGHT, 1);
                break;
            case R.id.mb_corner_blue_open:
                ledControl(LedLight.CORNER_BLUE_LIGHT, 0);
                break;
            case R.id.mb_corner_blue_close:
                ledControl(LedLight.CORNER_BLUE_LIGHT, 1);
                break;
            case R.id.mb_corner_white_open:
                ledControl(LedLight.CORNER_WHITE_LIGHT, 0);
                break;
            case R.id.mb_corner_white_close:
                ledControl(LedLight.CORNER_WHITE_LIGHT, 1);
                break;
            case R.id.mb_indicator_yellow_open:
                ledControl(LedLight.INDICATOR_YELLOW_LIGHT, 0);
                break;
            case R.id.mb_indicator_yellow_close:
                ledControl(LedLight.INDICATOR_YELLOW_LIGHT, 1);
                break;
            case R.id.mb_camera_indicator_open:
                ledControl(LedLight.CAMERA_LIGHT, 0);
                break;
            case R.id.mb_camera_indicator_close:
                ledControl(LedLight.CAMERA_LIGHT, 1);
                break;
            case R.id.mb_charge_red_open:
                ledControl(LedLight.CHARGE_RED_LIGHT, 0);
                break;
            case R.id.mb_charge_red_close:
                ledControl(LedLight.CHARGE_RED_LIGHT, 1);
                break;
            case R.id.mb_charge_green_open:
                ledControl(LedLight.CHARGE_GREEN_LIGHT, 0);
                break;
            case R.id.mb_charge_green_close:
                ledControl(LedLight.CHARGE_GREEN_LIGHT, 1);
                break;
            case R.id.mb_charge_blue_open:
                ledControl(LedLight.CHARGE_BLUE_LIGHT, 0);
                break;
            case R.id.mb_charge_blue_close:
                ledControl(LedLight.CHARGE_BLUE_LIGHT, 1);
                break;
        }
    }

    /**
     * LED light control
     *
     * @param ledIndex  LED light index
     * @param ledStatus LED light status, 0-on, 1-off
     */
    private void ledControl(int ledIndex, int ledStatus) {
        try {
            int code = MyApplication.app.basicOptV2.ledStatusOnDevice(ledIndex, ledStatus);
            Log.e(TAG, "ledStatusOnDevice()-->code:" + code + ", ledIndex:" + ledIndex + ", status:" + ledStatus);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
