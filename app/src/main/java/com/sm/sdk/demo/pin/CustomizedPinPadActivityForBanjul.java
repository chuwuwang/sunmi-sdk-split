package com.sm.sdk.demo.pin;

import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.view.PasswordEditText;
import com.sm.sdk.demo.wrapper.PinPadListenerV2Wrapper;
import com.sunmi.pay.hardware.aidl.AidlConstants;
import com.sunmi.pay.hardware.aidlv2.bean.PinPadConfigV2;
import com.sunmi.pay.hardware.aidlv2.bean.PinPadDataV2;
import com.sunmi.pay.hardware.aidlv2.pinpad.PinPadListenerV2;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Arrays;

public class CustomizedPinPadActivityForBanjul extends BaseAppCompatActivity {
    private PasswordEditText mPasswordEditText;

    public String cardNo = "";
    public PinPadConfigV2 customPinPadConfigV2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_pad_customized_for_banjul);
        initView();
        startInputPIN();
    }

    private void initView() {
        setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        initToolbarBringBack(R.string.pin_pad_customized_keyboard);
        TextView tvMoney = findViewById(R.id.tv_money);
        tvMoney.setText(longCent2DoubleMoneyStr(1));
        TextView tvCardNo = findViewById(R.id.tv_card_num);
        mPasswordEditText = findViewById(R.id.passwordEditText);
        Intent intent = getIntent();
        if (intent.hasExtra("PinPadConfigV2")) {
            cardNo = intent.getStringExtra("cardNo");
            customPinPadConfigV2 = (PinPadConfigV2) intent.getSerializableExtra("PinPadConfigV2");
        } else {
            cardNo = "123456789123456";
            customPinPadConfigV2 = creatPinPadConfigV2();
        }
        tvCardNo.setText(cardNo);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume()...");
        screenMonopoly(getApplicationInfo().uid);
    }

    @Override
    protected void onDestroy() {
        screenMonopoly(-1);
        cancelInputPIN();
        super.onDestroy();
    }

    /** create PinPadConfigV2 */
    private PinPadConfigV2 creatPinPadConfigV2() {
        try {
            //1.save a 3DES PIK(PIN key)
            final int PIK_INDEX = 1;
            Bundle bundle = new Bundle();
            bundle.putInt("keyType", AidlConstants.Security.KEY_TYPE_PIK);
            bundle.putByteArray("keyValue", ByteUtil.hexStr2Bytes("33DD20C9A0B5B861F2914D44BC2AF055"));
            bundle.putByteArray("checkValue", ByteUtil.hexStr2Bytes("28DBDB489D28BC92"));
            bundle.putInt("encryptIndex", 0);
            bundle.putInt("keyAlgType", AidlConstants.Security.KEY_ALG_TYPE_3DES);
            bundle.putInt("keyIndex", PIK_INDEX);
            int code = MyApplication.app.securityOptV2.saveKeyEx(bundle);
            Log.e(TAG, "savePIK() " + (code == 0 ? "success" : "failed, code:" + code));

            //2. create PinPadConfigV2
            PinPadConfigV2 config = new PinPadConfigV2();
            // PinPadType: 0-SDK built-in PinPad, 1-Client customized PinPad
            config.setPinPadType(1);
            // PinType: 0-online PIN, 1-offline PIN
            config.setPinType(0);
            // isOrderNumerKey: true:order number PinPad, false:disorder number PinPad
            config.setOrderNumKey(false);
            // PinAlgType: 0-3DES, 1-SM4, 2-AES
            config.setAlgorithmType(0);
            // PIK key system: 0-MKSK, 1-Dukpt
            config.setKeySystem(0);
            byte[] panBytes = cardNo.substring(cardNo.length() - 13, cardNo.length() - 1).getBytes(StandardCharsets.US_ASCII);
            config.setPan(panBytes);
            // Input PIN timeout time
            config.setTimeout(60 * 1000);
            // PIN block format
            config.setPinblockFormat(AidlConstants.PinBlockFormat.SEC_PIN_BLK_ISO_FMT0);
            // PIK(PIN key) index
            config.setPinKeyIndex(PIK_INDEX);
            // Minimum input PIN number
            config.setMinInput(0);
            // Maximum input number(Max value is 12)
            config.setMaxInput(12);
            return config;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void startInputPIN() {
        String keyNumber = initPinPad();
        Log.e(TAG, "initPinPad keyNumber:" + keyNumber);
        importPinPadData();
    }

    /** init PinPad */
    private String initPinPad() {
        String keyNumber = null;
        try {
            PinPadConfigV2 config = new PinPadConfigV2();
            config.setMaxInput(12);
            config.setMinInput(4);
            config.setPinPadType(1);//Set customize PinPad
            config.setAlgorithmType(customPinPadConfigV2.getAlgorithmType());
            config.setPinType(customPinPadConfigV2.getPinType());
            config.setTimeout(customPinPadConfigV2.getTimeout());
            config.setOrderNumKey(customPinPadConfigV2.isOrderNumKey());
            config.setPinblockFormat(customPinPadConfigV2.getPinblockFormat());
            config.setKeySystem(customPinPadConfigV2.getKeySystem());
            config.setPinKeyIndex(customPinPadConfigV2.getPinKeyIndex());
            int length = cardNo.length();
            byte[] panBlock = cardNo.substring(length - 13, length - 1).getBytes("US-ASCII");
            config.setPan(panBlock);

            addStartTimeWithClear("initPinPad()");
            keyNumber = MyApplication.app.pinPadOptV2.initPinPad(config, mPinPadListener);
            if (TextUtils.isEmpty(keyNumber)) {
                String msg = "initPinPad failed";
                LogUtil.e(TAG, msg);
                showToast(msg);
            } else {
                mPasswordEditText.clearText();
                mPasswordEditText.setKeepScreenOn(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return keyNumber;
    }

    /** Import PinPad data to sdk */
    private void importPinPadData() {
        try {
            //1.get key view location
            PinPadDataV2 dataV2 = new PinPadDataV2();
            addStartTimeWithClear("importPinPadData()");
            MyApplication.app.pinPadOptV2.importPinPadData(dataV2);
            addEndTime("importPinPadData()");
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final PinPadListenerV2 mPinPadListener = new PinPadListenerV2Wrapper() {

        @Override
        public void onPinLength(int len) throws RemoteException {
            LogUtil.e(Constant.TAG, "onPinLength len:" + len);
            updatePasswordView(len);
        }

        @Override
        public void onConfirm(int type, byte[] pinBlock) throws RemoteException {
            addEndTime("initPinPad()");
            LogUtil.e(Constant.TAG, "onConfirm pinType:" + type);
            String pinBlockStr = ByteUtil.bytes2HexStr(pinBlock);
            LogUtil.e(Constant.TAG, "pinBlock:" + pinBlockStr);
            if (TextUtils.equals("00", pinBlockStr)) {
                handleOnConfirm("");
            } else {
                handleOnConfirm(pinBlockStr);
            }
            showSpendTime();
        }

        @Override
        public void onCancel() throws RemoteException {
            addEndTime("initPinPad()");
            LogUtil.e(Constant.TAG, "onCancel");
            handleOnCancel();
            showSpendTime();
        }

        @Override
        public void onError(int code) throws RemoteException {
            addEndTime("initPinPad()");
            LogUtil.e(Constant.TAG, "onError code:" + code);
            handleOnError();
            showSpendTime();
        }
    };

    private void updatePasswordView(int len) {
        runOnUiThreadEx(() -> {
            char[] stars = new char[len];
            Arrays.fill(stars, '*');
            mPasswordEditText.setText(new String(stars));
        });
    }

    private void handleOnConfirm(String pinBlock) {
        showToast("CONFIRM, pinblock:" + pinBlock);
        Intent intent = getIntent();
        intent.putExtra("pinCipher", pinBlock);
        setResult(0, intent);
        finish();
    }

    private void handleOnCancel() {
        showToast("CANCEL");
        finish();
    }

    private void handleOnError() {
        showToast("ERROR");
        finish();
    }

    /** 将Long类型的钱（单位：分）转化成String类型的钱（单位：元） */
    public static String longCent2DoubleMoneyStr(long amount) {
        BigDecimal bd = new BigDecimal(amount);
        double doubleValue = bd.divide(new BigDecimal("100")).doubleValue();
        DecimalFormat df = new DecimalFormat("#0.00");
        return df.format(doubleValue);
    }

    /** 屏幕独占 */
    private void screenMonopoly(int mode) {
        try {
            addStartTimeWithClear("setScreenMode()");
            MyApplication.app.basicOptV2.setScreenMode(mode);
            addEndTime("setScreenMode()");
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** cancel input PIN */
    private void cancelInputPIN() {
        try {
            MyApplication.app.pinPadOptV2.cancelInputPin();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
