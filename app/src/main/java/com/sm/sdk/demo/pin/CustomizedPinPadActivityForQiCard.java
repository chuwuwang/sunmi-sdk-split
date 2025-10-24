package com.sm.sdk.demo.pin;

import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.view.TitleView;
import com.sm.sdk.demo.wrapper.PinPadListenerV2Wrapper;
import com.sunmi.pay.hardware.aidl.AidlConstants;
import com.sunmi.pay.hardware.aidlv2.bean.PinPadConfigV2;
import com.sunmi.pay.hardware.aidlv2.bean.PinPadDataV2;
import com.sunmi.pay.hardware.aidlv2.pinpad.PinPadListenerV2;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class CustomizedPinPadActivityForQiCard extends BaseAppCompatActivity {
    private static final int PIK_INDEX = 1;
    private TextView tv_k0, tv_k1, tv_k2, tv_k3, tv_k4, tv_k5, tv_k6, tv_k7, tv_k8, tv_k9;

    private EditText tvPin;
    private ImageView mBackView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isRTL()) {
            setContentView(R.layout.activity_pin_pad_customized_for_qicard_rtl);
        } else {
            setContentView(R.layout.activity_pin_pad_customized_for_qicard_ltr);
        }
        initView();
        initData();
        getKeyboardCoordinate();
    }

    private void initView() {
        setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        TitleView titleView = findViewById(R.id.title_view);
        TextView tvCenter = titleView.getCenterTextView();
        tvCenter.setText("(Qi)PinPad");
        mBackView = titleView.getLeftImageView();
        mBackView.setOnClickListener(v -> finish());
        tvPin = findViewById(R.id.tv_pin);
        tv_k0 = findViewById(R.id.tv_k0);
        tv_k1 = findViewById(R.id.tv_k1);
        tv_k2 = findViewById(R.id.tv_k2);
        tv_k3 = findViewById(R.id.tv_k3);
        tv_k4 = findViewById(R.id.tv_k4);
        tv_k5 = findViewById(R.id.tv_k5);
        tv_k6 = findViewById(R.id.tv_k6);
        tv_k7 = findViewById(R.id.tv_k7);
        tv_k8 = findViewById(R.id.tv_k8);
        tv_k9 = findViewById(R.id.tv_k9);
    }

    private void initData() {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private void getKeyboardCoordinate() {
        Log.e(TAG, "getKeyboardCoordinate()...");
        tvPin.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Log.e(TAG, "onGlobalLayout()...");
                        tvPin.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        String keyNumber = initPinPad();
                        if (!TextUtils.isEmpty(keyNumber)) {
                            showKeyNumberText(keyNumber);
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
            String cardNo = "123456789123456";
            byte[] panBytes = cardNo.substring(cardNo.length() - 13, cardNo.length() - 1).getBytes(StandardCharsets.US_ASCII);
            config.setPan(panBytes);
            // Input PIN timeout time
            config.setTimeout(60 * 1000);
            // PIN block format
            config.setPinblockFormat(AidlConstants.PinBlockFormat.SEC_PIN_BLK_ISO_FMT0);
            // PIK(PIN key) index
            config.setPinKeyIndex(PIK_INDEX);
            // Minimum input PIN number
            config.setMinInput(4);
            // Maximum input number(Max value is 12)
            config.setMaxInput(12);
            addStartTimeWithClear("initPinPad()");
            keyNumber = MyApplication.app.pinPadOptV2.initPinPad(config, mPinPadListener);
            if (TextUtils.isEmpty(keyNumber)) {
                String msg = "initPinPad failed";
                LogUtil.e(TAG, msg);
                showToast(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return keyNumber;
    }

    private void showKeyNumberText(String numbers) {
        tvPin.setKeepScreenOn(true);
        if (numbers == null || numbers.length() != 10) {
            return;
        }
        tv_k0.setText(numbers.substring(0, 1));
        tv_k1.setText(numbers.substring(1, 2));
        tv_k2.setText(numbers.substring(2, 3));
        tv_k3.setText(numbers.substring(3, 4));
        tv_k4.setText(numbers.substring(4, 5));
        tv_k5.setText(numbers.substring(5, 6));
        tv_k6.setText(numbers.substring(6, 7));
        tv_k7.setText(numbers.substring(7, 8));
        tv_k8.setText(numbers.substring(8, 9));
        tv_k9.setText(numbers.substring(9, 10));
    }

    /** Import PinPad data to sdk */
    private void importPinPadData(String keyNumber) {
        //1.get key view location
        TextView key_0 = tv_k0;
        if (isRTL()) {
            key_0 = tv_k2;
        }
        //2.import key view data to sdk
        PinPadDataV2 data = new PinPadDataV2();
        //first number key X,Y,H,W
        int[] location = new int[2];
        key_0.getLocationOnScreen(location);
        data.numX = location[0];
        data.numY = location[1];
        data.numW = key_0.getWidth();
        data.numH = key_0.getHeight();
        // width of divider line
        data.lineW = getResources().getDimensionPixelSize(R.dimen.qicard_keyboard_divider);
        //cancel key X,Y,H,W
        Arrays.fill(location, 0);
        mBackView.getLocationOnScreen(location);
        data.cancelX = location[0];
        data.cancelY = location[1];
        data.cancelW = mBackView.getWidth();
        data.cancelH = mBackView.getHeight();
        data.rows = 4;
        data.clos = 3;
        if (isRTL()) {
            keyMapRTL(keyNumber, data);
        } else {
            keyMapLTR(keyNumber, data);
        }
        try {
            addStartTimeWithClear("importPinPadDataEx()");
            MyApplication.app.pinPadOptV2.importPinPadData(data);
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
        runOnUiThreadEx(() -> {
            char[] stars = new char[len];
            Arrays.fill(stars, '*');
            tvPin.setText(new String(stars));
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

    /**
     * LTR（Left-to-right）layout direction , there are several special keycodes:
     * <li>0x1B-cancel</li>
     * <li>0x0C-clear</li>
     * <li>0x0D-confirm</li>
     * <li>0x4B-disable(no touch effect)</li>
     */
    private void keyMapLTR(String keyNumber, PinPadDataV2 data) {
        data.keyMap = new byte[64];
        for (int i = 0, j = 0; i < 12; i++, j++) {
            if (i == 9) {
                data.keyMap[i] = 0x0C;//clear
                j--;
            } else if (i == 11) {
                data.keyMap[i] = 0x0D;//confirm
                j--;
            } else {
                data.keyMap[i] = (byte) keyNumber.charAt(j);
            }
        }
//        data.keyMap[9] = 0x4B;//disable key(when press key, no number entered and no beep sound played)
//        data.keyMap[11] = 0x4B;//disable key( when press key, no number entered and no beep sound played)
    }

    /**
     * RTL（Right-to-left）layout direction , there are several special keycodes:
     * <li>0x1B-cancel</li>
     * <li>0x0C-clear</li>
     * <li>0x0D-confirm</li>
     * <li>0x4B-disable(no touch effect)</li>
     */
    private void keyMapRTL(String keyNumber, PinPadDataV2 data) {
        data.keyMap = new byte[64];
        for (int i = 0; i < 9; i += 3) {
            for (int j = 0; j < 3; j++) {
                data.keyMap[i + j] = (byte) keyNumber.charAt(i + 2 - j);
            }
        }
        data.keyMap[9] = 0x0D;//confirm
        data.keyMap[10] = (byte) keyNumber.charAt(9);
        data.keyMap[11] = 0x0C;//clear
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
        View view = getWindow().getDecorView();
        return view.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
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
