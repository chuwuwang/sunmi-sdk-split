package com.sm.sdk.demo.pin;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.NumberUtil;
import com.sm.sdk.demo.utils.SystemPropertiesUtil;
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
    private ImageView mBackView;
    private Button btnClear;
    private Button btnConfirm;
    private PasswordEditText mPasswordEditText;
    private FixPasswordKeyboard mFixPasswordKeyboard;
    private int currentOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    private String cardNo = "";
    private PinPadConfigV2 customPinPadConfigV2;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate()->this:" + this);
        setContentView(R.layout.activity_pin_pad_customized);
        initView();
        getKeyboardCoordinate();
    }

    private void initView() {
        currentOrientation = getRequestedOrientation();
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

    private void lockScreenOrientation() {
        int orientation = getRequestedOrientation();
        if (orientation != ActivityInfo.SCREEN_ORIENTATION_LOCKED) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        }
    }

    private void unlockScreenOrientation() {
        setRequestedOrientation(currentOrientation);
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
                        if (TextUtils.isEmpty(keyNumber)) {
                            showToast("init PinPad failed..");
                        } else if (isRotation180()) {
                            importPinPadDataForRotation180(keyNumber);
                        } else {
                            importPinPadData(keyNumber);
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
            Log.e(TAG, "initPinPad()...");
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
        //1.Determine the starting key based on the LTR and RTL layouts
        TextView key_0 = mFixPasswordKeyboard.getKey_0();
        if (isRTL()) {
            key_0 = mFixPasswordKeyboard.getKey_2();
        }
        // divider line width, 1px
        final int divider = 1;

        //2.import key view data to sdk
        PinPadDataV2Ex data = new PinPadDataV2Ex();
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
        //enter key X,Y,H,W
        Arrays.fill(location, 0);
        btnConfirm.getLocationOnScreen(location);
        data.enterX = location[0];
        data.enterY = location[1];
        data.enterW = btnConfirm.getWidth();
        data.enterH = btnConfirm.getHeight();
        //clear key X,Y,H,W
        Arrays.fill(location, 0);
        btnClear.getLocationOnScreen(location);
        data.clearX = location[0];
        data.clearY = location[1];
        data.clearW = btnClear.getWidth();
        data.clearH = btnClear.getHeight();
        data.rows = 5;
        data.clos = 3;
        if (isRTL()) {
            keyMapRTL(keyNumber, data);
        } else {
            keyMapLTR(keyNumber, data);
        }
        try {
            addStartTimeWithClear("importPinPadDataEx()");
            Log.e(TAG, "importPinPadDataEx()...");
            MyApplication.app.pinPadOptV2.importPinPadDataEx(data);
            addEndTime("importPinPadDataEx()");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** For some device type(eg: P3-MIX), the screen can be rotate 180° */
    private void importPinPadDataForRotation180(String keyNumber) {
        //1.Determine the starting key based on the LTR and RTL layouts
        View startView = mFixPasswordKeyboard.getEnter_1();
        if (isRTL()) {
            startView = mFixPasswordKeyboard.getCancel_1();
        }
        // divider line width, 1px
        final int divider = 1;

        //2.import key view data to sdk
        PinPadDataV2Ex data = new PinPadDataV2Ex();
        //first number key X,Y,H,W
        RectF startF = getViewBoundsInOriginalCoordinateSystem(startView);
        data.numX = NumberUtil.floatToInt(startF.left);
        data.numY = NumberUtil.floatToInt(startF.top);
        data.numW = NumberUtil.floatToInt(startF.width());
        data.numH = NumberUtil.floatToInt(startF.height());
        data.lineW = divider;
        //cancel key X,Y,H,W
        RectF cancelF = getViewBoundsInOriginalCoordinateSystem(mBackView);
        data.cancelX = NumberUtil.floatToInt(cancelF.left);
        data.cancelY = NumberUtil.floatToInt(cancelF.top);
        data.cancelW = NumberUtil.floatToInt(cancelF.width());
        data.cancelH = NumberUtil.floatToInt(cancelF.height());
        //enter key X,Y,H,W
        RectF confirmF = getViewBoundsInOriginalCoordinateSystem(btnConfirm);
        data.enterX = NumberUtil.floatToInt(confirmF.left);
        data.enterY = NumberUtil.floatToInt(confirmF.top);
        data.enterW = NumberUtil.floatToInt(confirmF.width());
        data.enterH = NumberUtil.floatToInt(confirmF.height());
        //clear key X,Y,H,W
        RectF clearF = getViewBoundsInOriginalCoordinateSystem(btnClear);
        data.clearX = NumberUtil.floatToInt(clearF.left);
        data.clearY = NumberUtil.floatToInt(clearF.top);
        data.clearW = NumberUtil.floatToInt(clearF.width());
        data.clearH = NumberUtil.floatToInt(clearF.height());
        data.rows = 5;
        data.clos = 3;
        if (isRTL()) {
            keyMapRTLForRotation180(keyNumber, data);
        } else {
            keyMapLTRForRotation180(keyNumber, data);
        }
        try {
            addStartTimeWithClear("importPinPadDataEx()");
            Log.e(TAG, "importPinPadDataEx()...");
            MyApplication.app.pinPadOptV2.importPinPadDataEx(data);
            addEndTime("importPinPadDataEx()");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** check whether device rotate 180° */
    private boolean isRotation180() {
        WindowManager manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        int rotation = manager.getDefaultDisplay().getRotation();
        Log.e(TAG, "rotation:" + rotation);
        switch (rotation) {
            case Surface.ROTATION_0:
                Log.e(TAG, "ROTATION_0");
                break;
            case Surface.ROTATION_90:
                Log.e(TAG, "ROTATION_90");
                break;
            case Surface.ROTATION_180:
                Log.e(TAG, "ROTATION_180");
                break;
            case Surface.ROTATION_270:
                Log.e(TAG, "ROTATION_270");
                break;
        }
        //rotationFlag: 0-not support 4 directions rotation, 1-support 4 directions rotation
        int rotationFlag = SystemPropertiesUtil.getInt("ro.sm.support_pinpad_rotate", 0);
        return rotationFlag == 0 && rotation == Surface.ROTATION_180;
    }

    /**
     * Get the absolute coordinates of the View in the coordinate system before the screen is rotated.
     *
     * @param view view object
     * @return view's absolutely coordinate in original coordinate system(no-rotation coordinate system)
     */
    private RectF getViewBoundsInOriginalCoordinateSystem(View view) {
        // 1.获取屏幕信息
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int rotation = display.getRotation();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getRealMetrics(metrics);
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;
        // 2.创建旋转矩阵
        Matrix rotationMatrix = createRotationMatrix(rotation, screenWidth, screenHeight);
        // 3.获取视图的绝对坐标并转换
        RectF originRect = convertViewBoundsToOriginal(view, rotationMatrix);
        Log.e(TAG, "原始坐标: X=" + originRect.left + ", Y=" + originRect.top);
        return originRect;
    }

    /**
     * Create a screen rotation coordinate transformation matrix
     *
     * @param rotation     current screen rotation value
     * @param screenWidth  screen width
     * @param screenHeight screen height
     * @return the transformation matrix
     */
    public Matrix createRotationMatrix(int rotation, int screenWidth, int screenHeight) {
        Matrix matrix = new Matrix();
        switch (rotation) {
            case Surface.ROTATION_90: // 顺时针旋转90度
                matrix.setRotate(90);
                matrix.postTranslate(screenHeight, 0);
                break;
            case Surface.ROTATION_180: // 顺时针旋转180度
                matrix.setRotate(180);
                matrix.postTranslate(screenWidth, screenHeight);
                break;
            case Surface.ROTATION_270: // 顺时针旋转270度(即逆时针90度)
                matrix.setRotate(270);
                matrix.postTranslate(0, screenWidth);
                break;
            case Surface.ROTATION_0: //无旋转，单位矩阵
            default:
                matrix.reset();
                break;
        }
        return matrix;
    }

    /**
     * Convert the target view bounds to original bounds(no-rotation bounds)
     *
     * @param view           the view object
     * @param rotationMatrix rotation matrix
     * @return view's bound rect in original coordinate system(no-rotation coordinate system)
     */
    public RectF convertViewBoundsToOriginal(View view, Matrix rotationMatrix) {
        // 获取视图在屏幕上的位置和尺寸
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        RectF rect = new RectF(
                location[0],
                location[1],
                location[0] + view.getWidth(),
                location[1] + view.getHeight()
        );
        // 创建逆矩阵
        Matrix inverseMatrix = new Matrix();
        rotationMatrix.invert(inverseMatrix);
        // 转换矩形
        inverseMatrix.mapRect(rect);
        return rect;
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
            unlockScreenOrientation();
            LogUtil.e(Constant.TAG, "onConfirm pinType:" + type);
            String pinBlockStr = ByteUtil.bytes2HexStr(pinBlock);
            LogUtil.e(Constant.TAG, "pinBlock:" + pinBlockStr);
            if (TextUtils.equals("00", pinBlockStr)) {
                handleOnConfirm("");
            } else {
                handleOnConfirm(pinBlockStr);
            }
        }

        @Override
        public void onCancel() throws RemoteException {
            addEndTime("initPinPad()");
            unlockScreenOrientation();
            LogUtil.e(Constant.TAG, "onCancel");
            handleOnCancel();
        }

        @Override
        public void onError(int code) throws RemoteException {
            addEndTime("initPinPad()");
            unlockScreenOrientation();
            LogUtil.e(Constant.TAG, "onError code:" + code);
            handleOnError();
        }
    };

    private void updatePasswordView(int len) {
        runOnUiThreadEx(() -> {
            if (len > 0) {//pinLen>0，disable screen rotation
                lockScreenOrientation();
            } else {//pinLen==0，enable screen rotation
                unlockScreenOrientation();
            }
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
        if (!isDestroyed()) {
            showToast("CANCEL");
            finish();
        }
    }

    private void handleOnError() {
        showToast("ERROR");
        finish();
    }

    /**
     * LTR（Left-to-right）layout direction, there are several special keycodes:
     * <li>0x1B-cancel</li>
     * <li>0x0C-clear</li>
     * <li>0x0D-confirm</li>
     * <li>0x4B-disable(no touch effect)</li>
     */
    private void keyMapLTR(String keyNumber, PinPadDataV2Ex data) {
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
    private void keyMapLTRForRotation180(String keyNumber, PinPadDataV2Ex data) {
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

    /**
     * RTL（Right-to-left）layout direction , there are several special keycodes:
     * <li>0x1B-cancel</li>
     * <li>0x0C-clear</li>
     * <li>0x0D-confirm</li>
     * <li>0x4B-disable(no touch effect)</li>
     */
    private void keyMapRTL(String keyNumber, PinPadDataV2Ex data) {
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
    private void keyMapRTLForRotation180(String keyNumber, PinPadDataV2Ex data) {
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
    private String longCent2DoubleMoneyStr(long amount) {
        BigDecimal bd = new BigDecimal(amount);
        double doubleValue = bd.divide(new BigDecimal("100")).doubleValue();
        DecimalFormat df = new DecimalFormat("#0.00");
        return df.format(doubleValue);
    }

    /** 屏幕独占 */
    private void screenMonopoly(int mode) {
        try {
            MyApplication.app.basicOptV2.setScreenMode(mode);
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
