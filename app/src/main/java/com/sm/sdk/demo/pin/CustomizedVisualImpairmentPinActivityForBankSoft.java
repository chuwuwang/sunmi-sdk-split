package com.sm.sdk.demo.pin;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.tts.PinPadTTS;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.Utility;
import com.sm.sdk.demo.view.FixPasswordKeyboard;
import com.sm.sdk.demo.view.PasswordEditText;
import com.sm.sdk.demo.view.TitleView;
import com.sm.sdk.demo.wrapper.PinPadListenerV2Wrapper;
import com.sunmi.pay.hardware.aidl.AidlConstants;
import com.sunmi.pay.hardware.aidlv2.bean.PinPadDataV2;
import com.sunmi.pay.hardware.aidlv2.pinpad.PinPadListenerV2;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * This page show how to customize a VisualImpairment keyboard, the keyboard has following function:
 * <li>support play voice with tts</li>
 */
public class CustomizedVisualImpairmentPinActivityForBankSoft extends BaseAppCompatActivity {
    private ImageView mBackView;
    private PasswordEditText mPasswordEditText;
    private FixPasswordKeyboard mFixPasswordKeyboard;
    private String cardNo = "";
    private final int PIK_INDEX = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_pad_customized_vi);
        initView();
        getKeyboardCoordinate();
        savePIK();
        playInstruction();
    }

    private void initView() {
        TitleView titleView = findViewById(R.id.title_view);
        TextView tvCenter = titleView.getCenterTextView();
        tvCenter.setText(getString(R.string.pin_pad_customized_vi_keyboard_for_softbank));
        mBackView = titleView.getLeftImageView();
        mBackView.setOnClickListener(v -> finish());
        TextView tvMoney = findViewById(R.id.tv_money);
        tvMoney.setText(longCent2DoubleMoneyStr(1));
        TextView tvCardNo = findViewById(R.id.tv_card_num);
        mPasswordEditText = findViewById(R.id.passwordEditText);
        mFixPasswordKeyboard = findViewById(R.id.fixPasswordKeyboard);
        cardNo = "123456789123456";
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
        super.onDestroy();
        Log.e(TAG, "onDestroy()->this:" + this);
        screenMonopoly(-1);
        cancelInputPIN();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.e(TAG, "onConfigurationChanged()...");
    }

    /** save a mksk PIK to calculate pinBlock */
    private void savePIK() {
        try {
            //1.save a 3DES PIK(PIN key)
            Bundle bundle = new Bundle();
            bundle.putInt("keyType", AidlConstants.Security.KEY_TYPE_PIK);
            bundle.putByteArray("keyValue", ByteUtil.hexStr2Bytes("33DD20C9A0B5B861F2914D44BC2AF055"));
            bundle.putByteArray("checkValue", ByteUtil.hexStr2Bytes("28DBDB489D28BC92"));
            bundle.putInt("encryptIndex", 0);
            bundle.putInt("keyAlgType", AidlConstants.Security.KEY_ALG_TYPE_3DES);
            bundle.putInt("keyIndex", PIK_INDEX);
            int code = MyApplication.app.securityOptV2.saveKeyEx(bundle);
            Log.e(TAG, "savePIK() " + (code == 0 ? "success" : "failed, code:" + code));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void playInstruction() {
        PinPadTTS.getInstance().play(100);
    }

    private void getKeyboardCoordinate() {
        Log.e(TAG, "getKeyboardCoordinate()...");
        mFixPasswordKeyboard.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Log.e(TAG, "onGlobalLayout()...");
                        mFixPasswordKeyboard.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                        setVisualImpairmentParam();
//                        getVisualImpairmentModeParam();
                        String keyNumber = initPinPad();
                        if (TextUtils.isEmpty(keyNumber)) {
                            showToast("init PinPad failed..");
                        } else {
                            importPinPadData(keyNumber);
                        }
                    }
                }
        );
    }

    /** 设置视障模式参数 */
    private void setVisualImpairmentParam() {
        try {
            Bundle bundle = new Bundle();
            bundle.putInt("timeoutGap1", 5); // the duration time of finger touch screen which considered as touch screen, range:0~100，unit：100ms, default: 10
            bundle.putInt("timeoutGap2", 10); // the time between two screen taps, range:0~100，unit：100ms, default: 10
            bundle.putInt("ttsLanguage", 0); // language of the voice announcement (0-follow system (default), 1-English, 2-Polish, 3-French, 4-Portugal, 5-chinese, 6-Spanish)
            bundle.putInt("rnibSelectMode", 0);//PIN number confirm mode, 0-double tap to confirm(default), 1-long press to confirm, 2-stay and double click to confirm
            bundle.putInt("rnibHoldTime", 30);//the necessary press time of long press to confirm mode, range:0~100，unit: 100ms, default: 30
            bundle.putInt("beepOnConfirm", 0);////whether to play beep sound when double tap confirm or long press confirm, 0-not pay sound, 1- pay sound(default value)
            bundle.putInt("stayTime", 10); //In the stay and double click to confirm mode, the duration of finger holding, range:5~100, unit:100ms, default: 10
            int code = MyApplication.app.pinPadOptV2.setVisualImpairmentModeParam(bundle);
            String msg = "setVisualImpairmentModeParam() code:" + code;
            LogUtil.e(TAG, msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 获取视障模式参数 */
    private void getVisualImpairmentModeParam() {
        try {
            Bundle bundle = new Bundle();
            int code = MyApplication.app.pinPadOptV2.getVisualImpairmentModeParam(bundle);
            LogUtil.e(TAG, "getVisualImpairmentModeParam() code:" + code);
            LogUtil.e(TAG, "getVisualImpairmentModeParam():\n" + Utility.bundle2String(bundle));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** init PinPad */
    private String initPinPad() {
        try {
            Bundle bundle = new Bundle();
            // PinPadType: 0-SDK built-in PinPad, 1-Client customized PinPad, 2-default blind PinPad, 3-rnib auth blind PinPad
            // 4-rnib auth normal PinPad, 5-customized blind PinPad
            bundle.putInt("pinPadType", 5);
            // PinType: 0-online PIN, 1-offline PIN
            bundle.putInt("pinType", 0);
            // isOrderNumberKey: true-order number PinPad, false-disorder number PinPad
            bundle.putInt("isOrderNumKey", 0);
            // PAN(Person Identify Number) convert ASCII characters to bytes, eg: “123456”.getBytes("US-ASCII")
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
            // Input PIN timeout time, unit: ms
            bundle.putInt("timeout", 120 * 1000);
            // is support bypass PIN, 0-not support, 1-support
            bundle.putInt("isSupportbypass", 1);
            // PIN block format
            bundle.putInt("pinblockFormat", AidlConstants.PinBlockFormat.SEC_PIN_BLK_ISO_FMT0);
            // PinAlgType: 0-3DES, 1-SM4, 2-AES
            bundle.putInt("algorithmType", 0);
            // PIK key system: 0-MKSK, 1-Dukpt
            bundle.putInt("keySystem", 0);
            addStartTimeWithClear("initPinPadEx()");
            Log.e(TAG, "initPinPadEx()...");
            String keyNumber = MyApplication.app.pinPadOptV2.initPinPadEx(bundle, mPinPadListener);
            if (TextUtils.isEmpty(keyNumber)) {//启动自定义密码键盘失败
                String msg = "initPinPad failed";
                LogUtil.e(TAG, msg);
                showToast(msg);
            } else {
                mPasswordEditText.clearText();
                mFixPasswordKeyboard.setKeepScreenOn(true);//设置屏幕常亮
                mFixPasswordKeyboard.setKeyBoard(keyNumber);
            }
            return keyNumber;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Import PinPad data to sdk */
    private void importPinPadData(String keyNumber) {
        //1.Determine the starting key based on the LTR and RTL layouts
        TextView key_0 = mFixPasswordKeyboard.getKey_0();
        if (isRTL()) {
            key_0 = mFixPasswordKeyboard.getKey_2();
        }
        // divider line width, 1px
        final int divider = 1;

        //2.import key view data to sdk
        PinPadDataV2 data = new PinPadDataV2();
        //first number key X,Y,H,W
        int[] location = new int[2];
        key_0.getLocationOnScreen(location);
        data.numX = location[0];
        data.numY = location[1];
        data.numW = key_0.getWidth();
        data.numH = key_0.getHeight();
        data.lineW = divider;

        //cancel key X,Y,H,W
        Arrays.fill(location, 0);
        mBackView.getLocationOnScreen(location);
        data.cancelX = location[0];
        data.cancelY = location[1];
        data.cancelW = mBackView.getWidth();
        data.cancelH = mBackView.getHeight();
        data.rows = 5;
        data.clos = 3;
        if (isRTL()) {
            keyMapRTL(keyNumber, data);
        } else {
            keyMapLTR(keyNumber, data);
        }
        try {
            addStartTimeWithClear("importPinPadData()");
            Log.e(TAG, "importPinPadDataEx()...");
            MyApplication.app.pinPadOptV2.importPinPadData(data);
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
            updateUpdatePinLength(len);
        }

        @Override
        public void onConfirm(int type, byte[] pinBlock) throws RemoteException {
            addEndTime("initPinPad()");
            String pinBlockStr = ByteUtil.bytes2HexStr(pinBlock);
            LogUtil.e(TAG, "onConfirm pinType:" + type + ", pinBlock:" + pinBlockStr);
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

        @Override
        public void onHover(int event, byte[] data) throws RemoteException {
            LogUtil.e(TAG, "onHover(), event:" + event + ", data:" + Arrays.toString(data));
            switch (event) {
                case 4:  //touch enter key
                    PinPadTTS.getInstance().play(15);
                    break;
                case 5:  //touch CLEAR key
                    PinPadTTS.getInstance().play(14);
                    break;
                case 6:  //touch CANCEL key
                    PinPadTTS.getInstance().play(13);
                    break;
                case 8:  //Input PIN numbers reach max input length
                    PinPadTTS.getInstance().play(20);
                    break;
                case 9:  //touch NUMBER key
                    if (PinPadTTS.getInstance().getPlayTextId() > 12) {
                        PinPadTTS.getInstance().stop();
                    }
                    playBeep(2750);
                    break;
                case 10:  //touch above invalid area
                    PinPadTTS.getInstance().play(17);
                    break;
                case 11:  //touch bottom invalid area-unsupported
                    PinPadTTS.getInstance().play(16);
                    break;
                case 12:  //touch left invalid area
                    PinPadTTS.getInstance().play(19);
                    break;
                case 13:  //touch right invalid area
                    PinPadTTS.getInstance().play(18);
                    break;
                case 14:   //Input PIN numbers reach max input length, and finger touching number key
                    PinPadTTS.getInstance().play(20);
                    break;
                case 15:  //in stay and double-click to confirm mode, the user already confirmed current key
                    playBeep(1000);
                    break;
            }
        }
    };

    private void updateUpdatePinLength(int len) {
        PinPadTTS.getInstance().play(len);
        runOnUiThreadEx(() -> {
            char[] stars = new char[len];
            Arrays.fill(stars, '*');
            mPasswordEditText.setText(new String(stars));
        });
    }

    private void playBeep(int frequency) {
        try {
            MyApplication.app.basicOptV2.buzzerOnDevice(1, frequency, 200, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleOnConfirm(String pinBlock) {
        showToast("CONFIRM, pinBlock:" + pinBlock);
        finish();
    }

    private void handleOnCancel() {
        if (!isDestroyed()) {
            showToast("CANCEL");
            finish();
        }
    }

    private void handleOnError() {
        showToast("ERROR");
        finish();
    }

    /** LTR（Left-to-right）layout direction */
    private void keyMapLTR(String keyNumber, PinPadDataV2 data) {
        data.keyMap = new byte[64];
        final int SIZE = data.rows * data.clos;
        for (int i = 0, j = 0; i < SIZE; i++) {
            if (i == 9 || i == 12) {
                data.keyMap[i] = 0x1B;//cancel
            } else if (i == 13) {
                data.keyMap[i] = 0x0C;//clear
            } else if (i == 11 || i == 14) {
                data.keyMap[i] = 0x0D;//confirm
            } else {
                data.keyMap[i] = (byte) keyNumber.charAt(j++);
            }
        }
//        data.keyMap[9] = 0x4B;//disable key(when press key, no number entered and no beep sound played)
//        data.keyMap[11] = 0x4B;//disable key( when press key, no number entered and no beep sound played)
    }

    /**
     * LTR（Left-to-right）layout direction, there are several special keycodes:
     * <li>0x1B-cancel</li>
     * <li>0x0C-clear</li>
     * <li>0x0D-confirm</li>
     * <li>0x4B-disable(no touch effect)</li>
     */
    private void keyMapLTRForRotation180(String keyNumber, PinPadDataV2 data) {
        data.keyMap = new byte[64];
        final int SIZE = data.rows * data.clos;
        for (int i = SIZE - 1, j = 0; i >= 0; i--) {
            if (i == 5 || i == 2) {
                data.keyMap[i] = 0x1B;//cancel
            } else if (i == 1) {
                data.keyMap[i] = 0x0C;//clear
            } else if (i == 3 || i == 0) {
                data.keyMap[i] = 0x0D;//confirm
            } else {
                data.keyMap[i] = (byte) keyNumber.charAt(j++);
            }
        }
//        data.keyMap[9] = 0x4B;//disable key(when press key, no number entered and no beep sound played)
//        data.keyMap[11] = 0x4B;//disable key( when press key, no number entered and no beep sound played)
    }

    /** RTL（Right-to-left）layout direction */
    private void keyMapRTL(String keyNumber, PinPadDataV2 data) {
        data.keyMap = new byte[64];
        final int NUMBER_ROW_COUNT = 3;//number row count
        for (int i = 0; i < NUMBER_ROW_COUNT; i++) {
            for (int j = 0; j < data.clos; j++) {
                data.keyMap[i * data.clos + j] = (byte) keyNumber.charAt(i * data.clos + 2 - j);
            }
        }
        data.keyMap[9] = 0x0D;//confirm
        data.keyMap[10] = (byte) keyNumber.charAt(9);
        data.keyMap[11] = 0x1B;//cancel
        data.keyMap[12] = 0x0D;//confirm
        data.keyMap[13] = 0x0C;//clear
        data.keyMap[14] = 0x1B;//cancel
    }

    /**
     * RTL（Right-to-left）layout direction , there are several special keycodes:
     * <li>0x1B-cancel</li>
     * <li>0x0C-clear</li>
     * <li>0x0D-confirm</li>
     * <li>0x4B-disable(no touch effect)</li>
     */
    private void keyMapRTLForRotation180(String keyNumber, PinPadDataV2 data) {
        data.keyMap = new byte[64];
        int SIZE = data.rows * data.clos;
        final int numberRow = 3;
        for (int i = 0; i < numberRow; i++) {
            for (int j = 0; j < data.clos; j++) {
                data.keyMap[SIZE - i * data.clos - j - 1] = (byte) keyNumber.charAt(i * data.clos + 2 - j);
            }
        }
        data.keyMap[5] = 0x0D;//confirm
        data.keyMap[4] = (byte) keyNumber.charAt(9);
        data.keyMap[3] = 0x1B;//cancel
        data.keyMap[2] = 0x0D;//confirm
        data.keyMap[1] = 0x0C;//clear
        data.keyMap[0] = 0x1B;//cancel
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

    /** 是否是RTL（Right-to-left）语系 */
    private boolean isRTL() {
        return mFixPasswordKeyboard.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
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
