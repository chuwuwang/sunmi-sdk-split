package com.sm.sdk.demo.pin;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.wrapper.PinPadListenerV2Wrapper;
import com.sunmi.pay.hardware.aidl.AidlConstants;
import com.sunmi.pay.hardware.aidlv2.AidlErrorCodeV2;
import com.sunmi.pay.hardware.aidlv2.pinpad.PinPadListenerV2;

import java.nio.charset.StandardCharsets;

public class VisualImpairmentPinActivity extends BaseAppCompatActivity {
    private static final int PIK_INDEX = 1;
    private TextView tvInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_pad_vi);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.pin_pad_vi_keybaoard);
        findViewById(R.id.mb_start_pin).setOnClickListener((v) -> startBlindPin());
        tvInfo = findViewById(R.id.tv_info);
    }

    /** save a 3DES PIK(PIN key) */
    private void savePIK() {
        try {
            Bundle bundle = new Bundle();
            bundle.putInt("keyType", AidlConstants.Security.KEY_TYPE_PIK);
            bundle.putByteArray("keyValue", ByteUtil.hexStr2Bytes("33DD20C9A0B5B861F2914D44BC2AF055"));
            bundle.putByteArray("checkValue", ByteUtil.hexStr2Bytes("28DBDB489D28BC92"));
            bundle.putInt("encryptIndex", 0);
            bundle.putInt("keyAlgType", AidlConstants.Security.KEY_ALG_TYPE_3DES);
            bundle.putInt("keyIndex", PIK_INDEX);

            int code = MyApplication.app.securityOptV2.saveKeyEx(bundle);
            Log.e(TAG, "savePIK() " + (code == 0 ? "success" : "failed, code:" + code));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** start blind PinPad */
    private void startBlindPin() {
        savePIK();
        setVisualImpairmentParam();
        try {
            Bundle bundle = new Bundle();
            // PinAlgType: 0-3DES, 1-SM4, 2-AES
            int pinAlgType = 0;//3DES
            int pinBlockFormat = AidlConstants.PinBlockFormat.SEC_PIN_BLK_ISO_FMT0;
            // PinPadType: 0-SDK built-in PinPad, 1-Client customized PinPad, 2-default blind PinPad, 3-rnib auth blind PinPad
            // 4-rnib auth normal PinPad, 5-customized blind PinPad
            bundle.putInt("pinPadType", 2);
            // PinType: 0-online PIN, 1-offline PIN
            bundle.putInt("pinType", 0);
            // isOrderNumberKey: true-order number PinPad, false-disorder number PinPad
            bundle.putInt("isOrderNumKey", 0);
            // PAN(Person Identify Number) ASCII格式转换成的byte 例如 “123456”.getBytes("us ascii")
            String cardNo = "123456789123456";
            byte[] panBytes = cardNo.substring(cardNo.length() - 13, cardNo.length() - 1).getBytes(StandardCharsets.US_ASCII);
            bundle.putByteArray("pan", panBytes);
            // PIK(PIN key) index
            bundle.putInt("pinKeyIndex", PIK_INDEX);
            // Minimum input PIN number
            bundle.putInt("minInput", 0);
            // Maximum input number(Max value is 12)
            bundle.putInt("maxInput", 12);
            // The input step if input PIN, default 1
            bundle.putInt("inputStep", 1);
            // Input PIN timeout time
            bundle.putInt("timeout", 120 * 1000);//120s
            // is support bypass PIN, 0-not support, 1-support
            bundle.putInt("isSupportbypass", 1);
            // PIN block format
            bundle.putInt("pinblockFormat", pinBlockFormat);
            // PinAlgType: 0-3DES, 1-SM4, 2-AES
            bundle.putInt("algorithmType", pinAlgType);
            // PIK key system: 0-MKSK, 1-Dukpt
            bundle.putInt("keySystem", 0);
            MyApplication.app.pinPadOptV2.initPinPadEx(bundle, mPinPadListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Set visualImpairment mode param */
    private void setVisualImpairmentParam() {
        int rnibSelectMode = 0;//0-double tap confirm, 1-long press confirm
        int rnibHoldTime = 30; //press holding time in long press confirm mode
        try {
            Bundle bundle = new Bundle();
            bundle.putInt("timeoutGap1", 5); // the duration time of finger touch screen which considered as touch screen, range:0~100，unit：100ms, default: 10
            bundle.putInt("timeoutGap2", 10); // the time between two screen taps, range:0~100，unit：100ms, default: 10
            bundle.putInt("ttsLanguage", 0); // language of the voice announcement (0-follow system (default), 1-English, 2-Polish, 3-French, 4-Portugal, 5-chinese, 6-Spanish)
            bundle.putInt("rnibSelectMode", rnibSelectMode);//PIN number confirm mode, 0-double tap to confirm(default), 1-long press to confirm
            bundle.putInt("rnibHoldTime", rnibHoldTime);//the necessary press time of long press to confirm mode, range:0~100，unit: 100ms, default: 30
            int code = MyApplication.app.pinPadOptV2.setVisualImpairmentModeParam(bundle);
            String msg = "setVisualImpairmentModeParam() " + (code == 0 ? "success" : "failed");
            Log.e(TAG, msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateInfo(String msg) {
        runOnUiThread(() -> tvInfo.setText(msg));
    }

    private final PinPadListenerV2 mPinPadListener = new PinPadListenerV2Wrapper() {
        @Override
        public void onPinLength(int len) {
            Log.e(TAG, "onPinLength:" + len);
        }

        @Override
        public void onConfirm(int pinType, byte[] pinBlock) {
            String hexStr = ByteUtil.bytes2HexStr(pinBlock);
            Log.e(TAG, "onConfirm, pinType:" + pinType + ",PinBlock:" + hexStr);
            updateInfo("PinBlock:" + hexStr);
        }

        @Override
        public void onCancel() {
            Log.e(TAG, "onCancel");
            showToast("user cancel");
        }

        @Override
        public void onError(int code) {
            Log.e(TAG, "onError:" + code);
            String msg = AidlErrorCodeV2.valueOf(code).getMsg();
            showToast("error:" + msg + " -- " + code);
        }
    };
}
