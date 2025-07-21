package com.sm.sdk.demo.pin;

import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.view.FixPasswordKeyboard;
import com.sm.sdk.demo.view.PasswordEditText;
import com.sm.sdk.demo.view.TitleView;
import com.sm.sdk.demo.wrapper.PinPadListenerV2Wrapper;
import com.sunmi.pay.hardware.aidl.AidlConstants.PinBlockFormat;
import com.sunmi.pay.hardware.aidl.AidlConstants.Security;
import com.sunmi.pay.hardware.aidlv2.bean.PinPadConfigV2;
import com.sunmi.pay.hardware.aidlv2.bean.PinPadDataV2Ex;
import com.sunmi.pay.hardware.aidlv2.pinpad.PinPadListenerV2;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Arrays;

public class CustomizedPinPadActivity extends BaseAppCompatActivity {
    private final int[] mKeyboardCoordinate = {0, 0};  // 密码键盘第一个button左顶点位置（绝对位置）
    private final int[] mCancelCoordinate = {0, 0};    // 取消键左顶点位置（绝对位置）

    private ImageView mBackView;
    private Button btnClear;
    private Button btnConfirm;
    private PasswordEditText mPasswordEditText;
    private FixPasswordKeyboard mFixPasswordKeyboard;

    public String cardNo = "";
    public PinPadConfigV2 customPinPadConfigV2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_pad_customized);
        initView();
        getKeyboardCoordinate();
    }

    private void initView() {
        setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        TitleView titleView = findViewById(R.id.title_view);
        TextView tvCenter = titleView.getCenterTextView();
        tvCenter.setText(getString(R.string.pin_pad_customized_keyboard));
        mBackView = titleView.getLeftImageView();
        mBackView.setOnClickListener(v -> finish());
        TextView tvMoney = findViewById(R.id.tv_money);
        tvMoney.setText(longCent2DoubleMoneyStr(1));
        TextView tvCardNo = findViewById(R.id.tv_card_num);
        btnClear = findViewById(R.id.btn_clear);
        btnConfirm = findViewById(R.id.btn_confirm);
        mPasswordEditText = findViewById(R.id.passwordEditText);
        mFixPasswordKeyboard = findViewById(R.id.fixPasswordKeyboard);
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
            bundle.putInt("keyType", Security.KEY_TYPE_PIK);
            bundle.putByteArray("keyValue", ByteUtil.hexStr2Bytes("33DD20C9A0B5B861F2914D44BC2AF055"));
            bundle.putByteArray("checkValue", ByteUtil.hexStr2Bytes("28DBDB489D28BC92"));
            bundle.putInt("encryptIndex", 0);
            bundle.putInt("keyAlgType", Security.KEY_ALG_TYPE_3DES);
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
            config.setPinblockFormat(PinBlockFormat.SEC_PIN_BLK_ISO_FMT0);
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

    private void getKeyboardCoordinate() {
        Log.e(TAG, "getKeyboardCoordinate()...");
        mFixPasswordKeyboard.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Log.e(TAG, "onGlobalLayout()...");
                        mFixPasswordKeyboard.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        String keyNumber = initPinPad();
                        if (!TextUtils.isEmpty(keyNumber)) {
                            importPinPadData(keyNumber);
                        } else {
                            showToast("init PinPad failed..");
                        }
                    }
                }
        );
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
                mFixPasswordKeyboard.setKeepScreenOn(true);
                mFixPasswordKeyboard.setKeyBoard(keyNumber);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return keyNumber;
    }

    /** Import PinPad data to sdk */
    private void importPinPadData(String keyNumber) {
        //1.get key view location
        TextView key_0 = mFixPasswordKeyboard.getKey_0();
        if (isRTL()) {
            key_0 = mFixPasswordKeyboard.getKey_2();
        }
        key_0.getLocationOnScreen(mKeyboardCoordinate);
        // key view item width
        int keyWidth = key_0.getWidth();
        // key view item height
        int keyHeight = key_0.getHeight();
        // width of divider line
        int divider = 1;
        mBackView.getLocationOnScreen(mCancelCoordinate);
        // cancel key width
        int cancelKeyWidth = mBackView.getWidth();
        // cancel key height
        int cancelKeyHeight = mBackView.getHeight();

        //2.import key view data to sdk
        PinPadDataV2Ex data = new PinPadDataV2Ex();
        data.numX = mKeyboardCoordinate[0];
        data.numY = mKeyboardCoordinate[1];
        data.numW = keyWidth;
        data.numH = keyHeight;
        data.lineW = divider;
        data.cancelX = mCancelCoordinate[0];
        data.cancelY = mCancelCoordinate[1];
        data.cancelW = cancelKeyWidth;
        data.cancelH = cancelKeyHeight;
        //enter key X,Y,H,W
        int[] buffer = new int[2];
        btnConfirm.getLocationOnScreen(buffer);
        data.enterX = buffer[0];
        data.enterY = buffer[1];
        data.enterH = btnConfirm.getHeight();
        data.enterW = btnConfirm.getWidth();
        //clear key X,Y,H,W
        Arrays.fill(buffer, 0);
        btnClear.getLocationOnScreen(buffer);
        data.clearX = buffer[0];
        data.clearY = buffer[1];
        data.clearH = btnClear.getHeight();
        data.clearW = btnClear.getWidth();
        data.rows = 5;
        data.clos = 3;
        if (isRTL()) {
            keyMapRTL(keyNumber, data);
        } else {
            keyMapLTR(keyNumber, data);
        }
        try {
            addStartTimeWithClear("importPinPadDataEx()");
            MyApplication.app.pinPadOptV2.importPinPadDataEx(data);
            addEndTime("importPinPadDataEx()");
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
        runOnUiThread(() -> {
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

    /** LTR（Left-to-right）layout direction */
    private void keyMapLTR(String keyNumber, PinPadDataV2Ex data) {
        data.keyMap = new byte[64];
        for (int i = 0, j = 0; i < 15; i++, j++) {
            if (i == 9 || i == 12) {
                data.keyMap[i] = 0x1B;//cancel
                j--;
            } else if (i == 13) {
                data.keyMap[i] = 0x0C;//clear
                j--;
            } else if (i == 11 || i == 14) {
                data.keyMap[i] = 0x0D;//confirm
                j--;
            } else {
                data.keyMap[i] = (byte) keyNumber.charAt(j);
            }
        }
//        data.keyMap[9] = 0x4B;//disable key(when press key, no number entered and no beep sound played)
//        data.keyMap[11] = 0x4B;//disable key( when press key, no number entered and no beep sound played)
    }

    /** RTL（Right-to-left）layout direction */
    private void keyMapRTL(String keyNumber, PinPadDataV2Ex data) {
        data.keyMap = new byte[64];
        for (int i = 0; i < 9; i += 3) {
            for (int j = 0; j < 3; j++) {
                data.keyMap[i + j] = (byte) keyNumber.charAt(i + 2 - j);
            }
        }
        data.keyMap[9] = 0x0D;//confirm
        data.keyMap[10] = (byte) keyNumber.charAt(9);
        data.keyMap[11] = 0x1B;//cancel
        data.keyMap[12] = 0x0D;//confirm
        data.keyMap[13] = 0x0C;//clear
        data.keyMap[14] = 0x1B;//cancel
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
