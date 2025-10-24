package com.sm.sdk.demo.card;

import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.wrapper.CheckCardCallbackV2Wrapper;
import com.sunmi.pay.hardware.aidl.AidlConstants.CardType;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MifareDesfireEv2Activity extends BaseAppCompatActivity {
    private TextView tvResult;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_mifare_desfire_ev2);
        initView();
        checkCard();
    }

    private void initView() {
        initToolbarBringBack(R.string.card_test_mifare_desfire_ev2);
        findViewById(R.id.mb_get_app_id).setOnClickListener(this);
        findViewById(R.id.mb_select_app).setOnClickListener(this);
        findViewById(R.id.mb_read_data_file).setOnClickListener(this);
        findViewById(R.id.mb_write_data_file).setOnClickListener(this);
        tvResult = findViewById(R.id.tv_result);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mb_get_app_id:
                getApplicationIds();
                break;
            case R.id.mb_select_app:
                selectApplication();
                break;
            case R.id.mb_read_data_file:
                break;
            case R.id.mb_write_data_file:
                break;
        }
    }

    private void getApplicationIds() {
        List<byte[]> list = new ArrayList<>();
        byte[] cmd = ByteUtil.hexStr2Bytes("9060000000");
        byte[] recv = transmitApdu(cmd);
        if (recv.length > 0) {
            list.add(recv);
        }
        while (true) {
            recv = transmitApdu(ByteUtil.hexStr2Bytes("90AF000000"));
            if (recv.length > 0) {
                list.add(recv);
            }
            if (recv.length == 0 || (recv[recv.length - 1] & 0xff) != 0xaf) {
                break;
            }
        }
        for (byte[] version : list) {
            LogUtil.e(TAG, "version:" + ByteUtil.byte2PrintHex(version, 0, version.length));
        }
    }

    private void selectApplication() {
        //MIFARE DESFire SelectApplication with AID equal to 000000h
        byte[] cmd = ByteUtil.hexStr2Bytes("905A00000300000000");
        byte[] recv = transmitApdu(cmd);
        LogUtil.e(TAG, "applicationId:" + ByteUtil.bytes2HexStr(recv));
    }

    /** Mifare desfire ev2 response APDU not contains SWA SWB */
    private byte[] transmitApdu(byte[] send) {
        byte[] result = new byte[0];
        try {
            byte[] buffer = new byte[1024];
            addStartTimeWithClear("transmitApdu()");
            String msg = "apdu send:" + ByteUtil.bytes2HexStr(send);
            addTextViewText(msg, tvResult);
            int len = MyApplication.app.readCardOptV2.transmitApdu(CardType.MIFARE_DESFIRE.getValue(), send, buffer);
            addEndTime("transmitApdu()");
            if (len < 0) {
                msg = "transmitApdu() failed,code:" + len;
            } else {
                result = Arrays.copyOf(buffer, len);
                msg = "apdu recv:" + ByteUtil.bytes2HexStr(result);
            }
            addTextViewText(msg, tvResult);
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private void checkCard() {
        try {
            showSwingCardHintDialog(0);
            addStartTimeWithClear("checkCard()");
            MyApplication.app.readCardOptV2.checkCard(CardType.MIFARE_DESFIRE.getValue(), mCheckCardCallback, 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final CheckCardCallbackV2 mCheckCardCallback = new CheckCardCallbackV2Wrapper() {

        @Override
        public void findMagCard(Bundle bundle) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(Constant.TAG, "findMagCard");
            dismissSwingCardHintDialog();
            showSpendTime();
        }

        @Override
        public void findICCard(String atr) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(Constant.TAG, "findICCard:" + atr);
            dismissSwingCardHintDialog();
            showSpendTime();
        }

        @Override
        public void findRFCard(String uuid) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(Constant.TAG, "findRFCard:" + uuid);
            dismissSwingCardHintDialog();
            showSpendTime();
        }

        @Override
        public void onError(int code, String message) throws RemoteException {
            addEndTime("checkCard()");
            showSpendTime();
            dismissSwingCardHintDialog();
            String tip = "check card failed, code:" + code + ",msg:" + message;
            LogUtil.e(TAG, tip);
            showToast(tip);
        }
    };

    @Override
    protected void onDestroy() {
        cancelCheckCard();
        super.onDestroy();
    }

    private void cancelCheckCard() {
        try {
            MyApplication.app.readCardOptV2.cancelCheckCard();
            MyApplication.app.readCardOptV2.cardOff(CardType.MIFARE_DESFIRE.getValue());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
