package com.sm.sdk.demo.card;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.Utility;
import com.sm.sdk.demo.wrapper.CheckCardCallbackV2Wrapper;
import com.sunmi.pay.hardware.aidl.AidlConstants.CardType;
import com.sunmi.pay.hardware.aidlv2.AidlErrorCodeV2;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;

import java.util.Arrays;
import java.util.regex.Pattern;


public class SAMMultiActivity extends BaseAppCompatActivity {
    private EditText edtCtrCodeChkCard;
    private EditText edtCtrCodeApdu;
    private EditText apdu;
    private TextView tvSamStatus;
    private TextView tvApduResult;
    private final SparseBooleanArray statusMap = new SparseBooleanArray();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_sam_multi);
        initView();
        initData();
    }

    private void initView() {
        initToolbarBringBack(R.string.card_test_sam_multi);
        edtCtrCodeChkCard = findViewById(R.id.edt_ctrl_check_card);
        edtCtrCodeApdu = findViewById(R.id.edt_ctrl_apdu);
        apdu = findViewById(R.id.apdu);
        findViewById(R.id.check_sam0).setOnClickListener(this);
        findViewById(R.id.check_sam1).setOnClickListener(this);
        findViewById(R.id.check_sam2).setOnClickListener(this);
        findViewById(R.id.check_sam3).setOnClickListener(this);
        findViewById(R.id.check_sam4).setOnClickListener(this);
        findViewById(R.id.check_sam5).setOnClickListener(this);
        findViewById(R.id.apdu_sam0).setOnClickListener(this);
        findViewById(R.id.apdu_sam1).setOnClickListener(this);
        findViewById(R.id.apdu_sam2).setOnClickListener(this);
        findViewById(R.id.apdu_sam3).setOnClickListener(this);
        findViewById(R.id.apdu_sam4).setOnClickListener(this);
        findViewById(R.id.apdu_sam5).setOnClickListener(this);
        findViewById(R.id.card_off_sam0).setOnClickListener(this);
        findViewById(R.id.card_off_sam1).setOnClickListener(this);
        findViewById(R.id.card_off_sam2).setOnClickListener(this);
        findViewById(R.id.card_off_sam3).setOnClickListener(this);
        findViewById(R.id.card_off_sam4).setOnClickListener(this);
        findViewById(R.id.card_off_sam5).setOnClickListener(this);
//        statusMap.put(CardType.PSAM0.getValue(), findViewById(R.id.status_sam0));
//        statusMap.put(CardType.SAM1.getValue(), findViewById(R.id.status_sam1));
//        statusMap.put(CardType.SAM2.getValue(), findViewById(R.id.status_sam2));
//        statusMap.put(CardType.SAM3.getValue(), findViewById(R.id.status_sam3));
        tvSamStatus = findViewById(R.id.tv_sam_status);
        tvApduResult = findViewById(R.id.apdu_result);
    }

    private void initData() {
        apdu.setText("00A404000CF064656D6F6170702E61707000");
        statusMap.put(CardType.PSAM0.getValue(), false);
        statusMap.put(CardType.SAM1.getValue(), false);
        statusMap.put(CardType.SAM2.getValue(), false);
        statusMap.put(CardType.SAM3.getValue(), false);
        statusMap.put(CardType.SAM4.getValue(), false);
        statusMap.put(CardType.SAM5.getValue(), false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.check_sam0:
                handleClearData();
                checkCard(CardType.PSAM0.getValue());
                break;
            case R.id.check_sam1:
                handleClearData();
                checkCard(CardType.SAM1.getValue());
                break;
            case R.id.check_sam2:
                handleClearData();
                checkCard(CardType.SAM2.getValue());
                break;
            case R.id.check_sam3:
                handleClearData();
                checkCard(CardType.SAM3.getValue());
                break;
            case R.id.check_sam4:
                handleClearData();
                checkCard(CardType.SAM4.getValue());
                break;
            case R.id.check_sam5:
                handleClearData();
                checkCard(CardType.SAM5.getValue());
                break;
            case R.id.apdu_sam0:
                transmitApduExx(CardType.PSAM0.getValue());
                break;
            case R.id.apdu_sam1:
                transmitApduExx(CardType.SAM1.getValue());
                break;
            case R.id.apdu_sam2:
                transmitApduExx(CardType.SAM2.getValue());
                break;
            case R.id.apdu_sam3:
                transmitApduExx(CardType.SAM3.getValue());
                break;
            case R.id.apdu_sam4:
                transmitApduExx(CardType.SAM4.getValue());
                break;
            case R.id.apdu_sam5:
                transmitApduExx(CardType.SAM5.getValue());
                break;
            case R.id.card_off_sam0:
                cardOff(CardType.PSAM0.getValue());
                break;
            case R.id.card_off_sam1:
                cardOff(CardType.SAM1.getValue());
                break;
            case R.id.card_off_sam2:
                cardOff(CardType.SAM2.getValue());
                break;
            case R.id.card_off_sam3:
                cardOff(CardType.SAM3.getValue());
                break;
            case R.id.card_off_sam4:
                cardOff(CardType.SAM4.getValue());
                break;
            case R.id.card_off_sam5:
                cardOff(CardType.SAM5.getValue());
                break;
        }
    }

    /** clean */
    private void handleClearData() {
        tvApduResult.setText("");
    }

    /** 检卡 */
    private void checkCard(int cardType) {
        try {
            String ctrCodeStr = edtCtrCodeChkCard.getText().toString();
            if (!Utility.checkHexValue(ctrCodeStr)) {
                showToast("ctrCode should be hex value");
                edtCtrCodeChkCard.requestFocus();
                return;
            }
            int ctrCode = Integer.parseInt(ctrCodeStr, 16);
            MyApplication.app.readCardOptV2.checkCardEx(cardType, ctrCode, 0, mReadCardCallback, 5);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final CheckCardCallbackV2 mReadCardCallback = new CheckCardCallbackV2Wrapper() {
        @Override
        public void findMagCard(Bundle bundle) throws RemoteException {
            LogUtil.e(TAG, "findMagCard,bundle:" + bundle);
        }

        @Override
        public void findICCardEx(Bundle info) throws RemoteException {
            int cardType = info.getInt("cardType");
            String atr = info.getString("atr");
            String msg = "findICCard, cardType:" + CardType.getDeviceId(cardType) + ", atr:" + atr;
            LogUtil.e(TAG, msg);
            updateSAMStatus(cardType, true);
        }

        @Override
        public void findRFCardEx(Bundle info) throws RemoteException {
            String uuid = info.getString("uuid");
            LogUtil.e(TAG, "findRFCard, uuid:" + uuid);
        }

        @Override
        public void onErrorEx(Bundle info) throws RemoteException {
            int cardType = info.getInt("cardType");
            int code = info.getInt("code");
            String msg = info.getString("message");
            LogUtil.e(TAG, "check card error,code:" + code + "message:" + msg);
            updateSAMStatus(cardType, false);
            handleCheckCardFailed(code, msg);
        }
    };

    private void handleCheckCardFailed(int code, final String msg) {
        addText("check card error,code:" + code + ", message:" + msg + "\n");
    }

    private boolean checkInputData() {
        String apduStr = apdu.getText().toString();
        if (TextUtils.isEmpty(apduStr)) {
            apdu.requestFocus();
            showToast("apdu should not be empty!");
            return false;
        }
        if (!Pattern.matches("[0-9a-fA-F]+", apduStr)) {
            apdu.requestFocus();
            showToast("apdu should hex characters!");
            return false;
        }
        return true;
    }

    /**
     * Transmit APDU to sam card
     */
    private void transmitApduExx(int cardType) {
        if (!checkInputData()) {
            return;
        }
        String ctrCodeStr = edtCtrCodeApdu.getText().toString();
        if (!Utility.checkHexValue(ctrCodeStr)) {
            showToast("ctrCode should be hex value");
            edtCtrCodeApdu.requestFocus();
            return;
        }
        int ctrCode = Integer.parseInt(ctrCodeStr, 16);
        byte[] send = ByteUtil.hexStr2Bytes(apdu.getText().toString());
        byte[] recv = new byte[2048];
        try {
            int len = MyApplication.app.readCardOptV2.transmitApduExx(cardType, ctrCode, send, recv);
            if (len < 0) {
                LogUtil.e(TAG, "transmitApduExx failed,code:" + len);
                showToast(AidlErrorCodeV2.valueOf(len).getMsg());
            } else {
                LogUtil.e(TAG, "transmitApduExx success,recv:" + ByteUtil.bytes2HexStr(recv));
                byte[] valid = Arrays.copyOf(recv, len);
                // received data contains swa,swb
                byte[] outData = Arrays.copyOf(valid, valid.length - 2);
                byte swa = valid[valid.length - 2];//swa
                byte swb = valid[valid.length - 1];//swb
                showApduRecv(cardType, true, outData, swa, swb);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * show received APDU response data
     */
    private void showApduRecv(int cardType, boolean hasSW, byte[] outData, byte swa, byte swb) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        SpannableString sb = new SpannableString("------------------- APDU Receive-------------------\n");
        sb.setSpan(new StyleSpan(Typeface.BOLD), 0, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.append(sb);

        ssb.append("cardType:");
        ssb.append(CardType.getDeviceId(cardType));
        ssb.append("outData:");
        ssb.append(ByteUtil.bytes2HexStr(outData));
        ssb.append("\n");
        if (hasSW) {
            ssb.append("SWA:");
            ssb.append(ByteUtil.bytes2HexStr(swa));
            ssb.append("\n");
            ssb.append("SWB:");
            ssb.append(ByteUtil.bytes2HexStr(swb));
            ssb.append("\n");
        }
        addText(ssb);
    }

    private void addText(CharSequence msg) {
        CharSequence preMsg = tvApduResult.getText();
        runOnUiThreadEx(() -> tvApduResult.setText(TextUtils.concat(msg, preMsg)));
    }

    private void cardOff(int cardType) {
        try {
            int code = MyApplication.app.readCardOptV2.cardOff(cardType);
            updateSAMStatus(cardType, code != 0);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void updateSAMStatus(int cardType, boolean active) {
        statusMap.put(cardType, active);
        runOnUiThreadEx(() -> {
            StringBuilder sb = new StringBuilder("SAM status:\n");
            for (int i = 0; i < statusMap.size(); i++) {
                sb.append(CardType.getDeviceId(statusMap.keyAt(i)));
                sb.append(":");
                sb.append(statusMap.valueAt(i) ? "active" : "deactive");
                sb.append(" ");
            }
            tvSamStatus.setText(sb);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelCheckCard();
    }

    private void cancelCheckCard() {
        try {
            MyApplication.app.readCardOptV2.cancelCheckCard();
            for (int i = 0; i < statusMap.size(); i++) {
                MyApplication.app.readCardOptV2.cardOff(statusMap.keyAt(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
