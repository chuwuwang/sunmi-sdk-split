package com.sm.sdk.demo.pin;

import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.security.KeyIndexUtil;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.Utility;
import com.sm.sdk.demo.wrapper.PinPadListenerV2Wrapper;
import com.sunmi.pay.hardware.aidl.AidlConstants.Security;
import com.sunmi.pay.hardware.aidlv2.AidlErrorCodeV2;
import com.sunmi.pay.hardware.aidlv2.pinpad.PinPadListenerV2;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class StartInputPinActivity extends BaseAppCompatActivity {
    private static final int PIK_INDEX = 1;
    private EditText edtPikIndex;
    private EditText edtCardNo;
    private TextView tvResult;
    private int pikKeySystem = Security.SEC_MKSK;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_input_pin);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.pin_pad_start_input_pin);
        RadioGroup group = findViewById(R.id.rdg_pik_system);
        group.setOnCheckedChangeListener((group1, checkedId) -> {
            switch (checkedId) {
                case R.id.rdo_key_system_mksk:
                    pikKeySystem = Security.SEC_MKSK;
                    break;
                case R.id.rdo_key_system_dukpt:
                    pikKeySystem = Security.SEC_DUKPT;
                    break;
            }
        });
        edtPikIndex = findViewById(R.id.edt_pik_index);
        edtCardNo = findViewById(R.id.edt_card_no);
        tvResult = findViewById(R.id.tv_pinblock_resuult);
        findViewById(R.id.mb_start_input_pin).setOnClickListener((v) -> startInputPIN());
        findViewById(R.id.mb_get_pinblock).setOnClickListener((v) -> getPiBlock());
        edtPikIndex.setText("1");
        edtCardNo.setText("123456789123456");
    }

    /** start input PIN, not return pinBLock at PinPadListenerV2.onConfirm() callback */
    private void startInputPIN() {
        try {
            Bundle bundle = new Bundle();
            bundle.putInt("pinPadType", 0);
            bundle.putInt("pinType", 0);
            bundle.putInt("isOrderNumKey", 0);
            bundle.putInt("minInput", 4);
            bundle.putInt("maxInput", 12);
            bundle.putInt("inputStep", 1);
            bundle.putInt("isSupportbypass", 1);
            bundle.putInt("timeout", 60000);
            int code = MyApplication.app.pinPadOptV2.startInputPin(bundle, pinPadListenerV2);
            String msg = "startInputPin()," + Utility.getStateString(code);
            Log.e(TAG, msg);
            if (code < 0) {
                showToast(msg);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private final PinPadListenerV2 pinPadListenerV2 = new PinPadListenerV2Wrapper() {
        @Override
        public void onPinLength(int len) {
            Log.e(TAG, "onPinLength:" + len);
        }

        @Override
        public void onConfirm(int pinType, byte[] pinBlock) {
            String hexStr = ByteUtil.bytes2HexStr(pinBlock);
            String msg = "onConfirm, pinType:" + pinType + ",PinBlock:" + hexStr;
            Log.e(TAG, msg);
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

    private void getPiBlock() {
        try {
            String pikIndexStr = edtPikIndex.getText().toString();
            if (TextUtils.isEmpty(pikIndexStr)) {
                showToast(R.string.pin_pad_key_index_hint);
                return;
            }
            int pinKeyIndex = Integer.parseInt(pikIndexStr);
            boolean isDukpt = pikKeySystem == Security.SEC_DUKPT;
            if (!KeyIndexUtil.checkMkskOrDukptKeyIndex(pinKeyIndex, isDukpt)) {
                showToast(R.string.pin_pad_key_index_hint);
                edtPikIndex.requestFocus();
                return;
            }
            String cardNo = edtCardNo.getText().toString().trim();
            if (cardNo.length() < 13 || cardNo.length() > 19) {
                showToast(R.string.pin_pad_card_no_hint);
                edtCardNo.requestFocus();
                return;
            }
            byte[] pan = cardNo.substring(cardNo.length() - 13, cardNo.length() - 1).getBytes(StandardCharsets.US_ASCII);
            Bundle bundle = new Bundle();
            bundle.putInt("keySystem", pikKeySystem);
            bundle.putInt("pinKeyIndex", pinKeyIndex);
            bundle.putInt("algorithmType", 0);
            bundle.putInt("pinblockFormat", 0);
            bundle.putByteArray("pan", pan);
            byte[] buffer = new byte[16];
            int len = MyApplication.app.pinPadOptV2.getPinBlock(bundle, buffer);
            String msg = null;
            if (len < 0) {
                msg = "getPinBlock() " + Utility.getStateString(len);
                Log.e(TAG, msg);
            } else {
                byte[] pinBlock = Arrays.copyOf(buffer, len);
                msg = "pinBlock:" + ByteUtil.bytes2HexStr(pinBlock);
                Log.e(TAG, msg);
            }
            tvResult.setText(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
