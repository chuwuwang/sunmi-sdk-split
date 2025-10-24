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
import com.sm.sdk.demo.utils.DataOperateUtil;
import com.sm.sdk.demo.utils.DesAesUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.Utility;
import com.sm.sdk.demo.wrapper.CheckCardCallbackV2Wrapper;
import com.sunmi.pay.hardware.aidl.AidlConstants.CardType;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;

import java.util.Arrays;
import java.util.Random;

public class MifareDesfireEv1Activity extends BaseAppCompatActivity {
    private TextView tvGetVersion;
    private TextView tvAuthenticate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_mifare_desfire_ev1);
        initView();
        checkCard();
    }

    private void initView() {
        initToolbarBringBack(R.string.card_test_mifare_desfire_ev1);
        findViewById(R.id.mb_get_version).setOnClickListener(this);
        findViewById(R.id.mb_authenticate).setOnClickListener(this);
        tvGetVersion = findViewById(R.id.tv_get_version);
        tvAuthenticate = findViewById(R.id.tv_authenticate);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mb_get_version:
                getVersion();
                break;
            case R.id.mb_authenticate:
                authenticate();
                break;
        }
    }

    /** Mifare Desfire EV1 getVersion */
    private void getVersion() {
        //3.send successfully using the “Wrapping of Native DESFire APDUs“ (see section 9.7.4 of [MFDESEV1]) the MIFARE DESFire EV1 GetVersion command (see section 5.2 and section 8.2) retrieving from the command response the version and the memory size of the MIFARE DESFire EV1 (see bold characters below).
        //a.If the received data is the following, the card is the DESFire EV1 with 2 Kbytes
        //  Software Major Version bigger or equal to 0x01
        //  Software Storage Size exactly 2048 Byte
        //b.If the received data is the following, the card is the DESFire EV1 with 4 Kbytes
        //  Software Major Version bigger or equal to 0x01
        //  Software Storage Size exactly 4096 Byte
        //c.If the received data is the following, the card is the DESFire EV1 with 8 Kbytes
        //  Software Major Version bigger or equal to 0x01
        //  Software Storage Size exactly 8192 bytes

        //The example of MIFARE DESFire EV1 GetVersion command is described below using the wrapping of Native DESFire APDUs:
        //8. MIFARE DESFire EV1 GetVersion command 1
        //  Command: 90 60 00 00 00h
        //  Expected Response: XX XX XX XX XX XX XX 91 AFh
        //9. MIFARE DESFire EV1 GetVersion command 2. The Storage Size code (SS) value indicates the storage size, in particular: 1Ah indicates 8192 bytes, 18h indicates 4096 bytes and 16h indicates 2048 bytes
        //  Command: 90 AF 00 00 00h
        //  Expected Response: XX XX XX SW XX SS XX 91 AFh
        //10. MIFARE DESFire EV1 GetVersion command 3
        //  Command: 90 AF 00 00 00h
        //  Expected Response: XX XX XX XX XX XX XX XX XX XX XX XX XX 91 00
        tvGetVersion.setText(null);

        //1.GetVersion command 1:
        byte[] cmd1 = ByteUtil.hexStr2Bytes("9060000000");
        byte[] recv1 = transmitApduExtended(cmd1, tvGetVersion);
        if (recv1 == null || recv1.length == 0) {
            addTextViewText("getVersion() failed..", tvGetVersion);
            return;
        }
        //2.GetVersion command 2:
        byte[] cmd2 = ByteUtil.hexStr2Bytes("90AF000000");
        byte[] recv2 = transmitApduExtended(cmd2, tvGetVersion);
        if (recv2 == null || recv2.length == 0) {
            addTextViewText("getVersion() failed..", tvGetVersion);
            return;
        }
        //3.GetVersion command 3:
        byte[] cmd3 = ByteUtil.hexStr2Bytes("90AF000000");
        byte[] recv3 = transmitApduExtended(cmd3, tvGetVersion);
        if (recv3 == null || recv3.length == 0) {
            addTextViewText("getVersion() failed..", tvGetVersion);
            return;
        }
        if (recv2.length >= 7) {
            String majorVersion = "Major version: " + ByteUtil.bytes2HexStr(recv2[3]);
            String storage = null;
            int ss = recv2[5] & 0xff;
            if (ss == 0x1A) {//software storage size is 8192 bytes
                storage = "Software storage size: 8192 bytes";
            } else if (ss == 0x18) {//software storage size is 4096 bytes
                storage = "Software storage size: 4096 bytes";
            } else if (ss == 0x16) {//software storage size is 2048 bytes
                storage = "Software storage size: 2048 bytes";
            }
            addTextViewText(majorVersion, tvGetVersion);
            addTextViewText(storage, tvGetVersion);
        }
    }

    /** Mifare desfire ev1 authenticate */
    private void authenticate() {
        try {
            String msg = null;
            tvAuthenticate.setText(null);
            // PICC master key: 16 bytes 0
            final byte[] key = new byte[16];
            //iv initialize as 8 bytes 0
            byte[] iv = new byte[8];

            //1.Select PICC Level, AID is 000000
            byte[] send = ByteUtil.hexStr2Bytes("90 5A 00 00 03 000000 00", true);
            byte[] recv = transmitApduExtended(send, tvAuthenticate);
            if (recv.length != 0x02) {
                addTextViewText("Select PICC Level error, recv data length error", tvAuthenticate);
                return;
            }
            int sw1 = recv[recv.length - 2] & 0xff;
            int sw2 = recv[recv.length - 1] & 0xff;
            if (sw1 != 0x91 || sw2 != 0x00) {
                addTextViewText(Utility.formatStr("Select PICC Level error, sw1:%02X, sw2:%02X", sw1, sw2), tvAuthenticate);
                return;
            }
            //2.AuthenticationISO-1st: command code=0x1A, keyNo=00, PICC return Enc(RndB)
            send = ByteUtil.hexStr2Bytes("90 1A 00 00 01 00 00", true);
            recv = transmitApduExtended(send, tvAuthenticate);
            if (recv.length != 0x0A && recv.length != 0x12) {//recv data not 10B or 18B
                addTextViewText("AuthenticationISO-1st error, recv data length error", tvAuthenticate);
                return;
            }
            sw1 = recv[recv.length - 2] & 0xff;
            sw2 = recv[recv.length - 1] & 0xff;
            if (sw1 != 0x91 || (sw2 != 0x00 && sw2 != 0xaf)) {
                addTextViewText(Utility.formatStr("AuthenticationISO-1st error, sw1:%02X, sw2:%02X", sw1, sw2), tvAuthenticate);
                return;
            }
            byte[] enc = Arrays.copyOf(recv, 8);//ciphertext data
            byte[] dec = DesAesUtil.desDecrypt(key, iv, enc, DesAesUtil.DATA_MODE_CBC);//plaintext data
            byte[] RndB = dec;//RndB
            byte[] RndB_ = DataOperateUtil.leftRotation(RndB, 1);
            iv = enc;//iv set as the Enc(RndB)(the latest cipher block)
            addTextViewText("PICC->RndB=" + ByteUtil.bytes2HexStr(RndB), tvAuthenticate);
            addTextViewText("PICC->RndB'=" + ByteUtil.bytes2HexStr(RndB_), tvAuthenticate);
            addTextViewText("PICC->iv=" + ByteUtil.bytes2HexStr(iv), tvAuthenticate);

            //3.AuthenticationISO-2nd: Send Enc(RndA||RndB') to PICC, PICC return Enc(RndA')
            Random ran = new Random();
            byte[] RndA = new byte[8];
            ran.nextBytes(RndA);
            addTextViewText("PCD->RndA=" + ByteUtil.bytes2HexStr(RndA), tvAuthenticate);
            byte[] tmp = ByteUtil.concatByteArrays(RndA, RndB_);
            byte[] dataIn = DesAesUtil.desEncrypt(key, iv, tmp, DesAesUtil.DATA_MODE_CBC);
            iv = Arrays.copyOfRange(dataIn, 8, 16);//iv set as the last 8 bytes Enc(RndA || RndB')(the latest cipher block)
            addTextViewText("PCD->enc(RndA||RndB')=" + ByteUtil.bytes2HexStr(dataIn), tvAuthenticate);
            addTextViewText("PCD->iv=" + ByteUtil.bytes2HexStr(iv), tvAuthenticate);
            //send = 90AF0000||Lc(temp.length)||temp||Le(00)
            byte[] cmd = ByteUtil.hexStr2Bytes("90 AF 00 00", true);
            byte[] lc = {(byte) dataIn.length};
            byte[] le = {(byte) 0x00};
            send = ByteUtil.concatByteArrays(cmd, lc, dataIn, le);
            recv = transmitApduExtended(send, tvAuthenticate);
            if (recv.length != 0x0A && recv.length != 0x12) {//recv data not 10B or 18B
                addTextViewText("AuthenticationISO-2nd error, recv data length error", tvAuthenticate);
                return;
            }
            sw1 = recv[recv.length - 2] & 0xff;
            sw2 = recv[recv.length - 1] & 0xff;
            if (sw1 != 0x91 && sw2 != 0x00) {
                addTextViewText(Utility.formatStr("AuthenticationISO-2nd error, sw1:%02X, sw2:%02X", sw1, sw2), tvAuthenticate);
                return;
            }
            enc = Arrays.copyOf(recv, 8);//encrypted data
            dec = DesAesUtil.desDecrypt(key, iv, enc, DesAesUtil.DATA_MODE_CBC);//RndA'
            byte[] RndA_ = dec;
            iv = enc;//iv set as the Enc(RndB)(the latest cipher block)
            addTextViewText("PICC->RndA'=" + ByteUtil.bytes2HexStr(RndA_), tvAuthenticate);
            addTextViewText("PICC->iv=" + ByteUtil.bytes2HexStr(iv), tvAuthenticate);

            //4.AuthenticationISO-3rd: PCD verify RndA'
            byte[] tmpA = DataOperateUtil.leftRotation(RndA, 1);
            if (!Arrays.equals(tmpA, RndA_)) {
                addTextViewText("AuthenticationISO-3rd failed: verify RndA' failed.", tvAuthenticate);
                return;
            }
            addTextViewText("AuthenticationISO success.", tvAuthenticate);

            //5.3DES session key := RndA_1st_half + RndB_1st_half + RndA_2nd_half + RndB_2nd_half.
            // This scrambling of RndA and RndB isdone to avoid that a malicious PcD could degenerate 3DES cryptography to single DES operation by forcing RndA =RndB.
            byte[] RndA_1st_half = Arrays.copyOf(RndA, 4);
            byte[] RndA_2nd_half = Arrays.copyOfRange(RndA, 4, 8);
            byte[] RndB_1st_half = Arrays.copyOf(RndB, 4);
            byte[] RndB_2nd_half = Arrays.copyOfRange(RndB, 4, 8);
            byte[] SessionKey = ByteUtil.concatByteArrays(RndA_1st_half, RndB_1st_half, RndA_2nd_half, RndB_2nd_half);
            addTextViewText("SessionKey=" + ByteUtil.bytes2HexStr(SessionKey), tvAuthenticate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Mifare desfire ev1 response APDU contains SWA SWB */
    private byte[] transmitApduExtended(byte[] send, TextView tv) {
        byte[] result = new byte[0];
        try {
            String msg = "send>>" + ByteUtil.bytes2HexStr(send);
            addTextViewText(msg, tv);

            final Bundle pIn = new Bundle();
            final Bundle pOut = new Bundle();
            pIn.putInt("cardType", CardType.MIFARE_DESFIRE.getValue());
            pIn.putInt("ctrCode", 0x20);
            pIn.putByteArray("sendBuff", send);
            addStartTimeWithClear("transmitApduExtended()");
            int code = MyApplication.app.readCardOptV2.transmitApduExtended(pIn, pOut);
            addEndTime("transmitApduExtended()");
            if (code < 0) {
                msg = "transmitApduExtended()->failed, code:" + code;
                addTextViewText(msg, tv);
                return result;
            }
            byte[] outData = pOut.getByteArray("outData");
            outData = outData == null ? new byte[0] : outData;
            byte swa = pOut.getByte("swa");//swa
            byte swb = pOut.getByte("swb");//swb

            result = Arrays.copyOf(outData, outData.length + 2);
            result[outData.length] = swa;
            result[outData.length + 1] = swb;

            msg = "recv<<" + ByteUtil.bytes2HexStr(result);
            addTextViewText(msg, tv);
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
