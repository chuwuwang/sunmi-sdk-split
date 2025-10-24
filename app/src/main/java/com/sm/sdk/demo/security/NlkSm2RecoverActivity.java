package com.sm.sdk.demo.security;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.NumberUtil;
import com.sm.sdk.demo.utils.Utility;

import java.util.Arrays;

public class NlkSm2RecoverActivity extends BaseAppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_nlk_sm2_recover);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.security_nlk_sm2_recover);
        findViewById(R.id.btn_nlk_sm2_sign).setOnClickListener(this);
        findViewById(R.id.btn_nlk_sm2_verify).setOnClickListener(this);
        findViewById(R.id.btn_nlk_sm2_encrypt).setOnClickListener(this);
        findViewById(R.id.btn_nlk_sm2_decrypt).setOnClickListener(this);
        findViewById(R.id.btn_nlk_calc_sm3_hash).setOnClickListener(this);
        findViewById(R.id.btn_nlk_single_sign).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_nlk_sm2_sign:
                nlkSm2Sign();
                break;
            case R.id.btn_nlk_sm2_verify:
                nlkSm2VerifySignature();
                break;
            case R.id.btn_nlk_sm2_encrypt:
                nlkSm2Encrypt();
                break;
            case R.id.btn_nlk_sm2_decrypt:
                nlkSm2Decrypt();
                break;
            case R.id.btn_nlk_calc_sm3_hash:
                nlkCalcSM3HashWithID();
                break;
            case R.id.btn_nlk_single_sign:
                nlkSm2SingleSign();
                break;
        }
    }

    /**
     * SM2 sign
     * <br/>SM2 signature data length is 64B
     */
    private void nlkSm2Sign() {
        try {
            String pubKeyIndexStr = this.<EditText>findViewById(R.id.edt_nlk_sign_pub_key_index).getText().toString();
            String pvtKeyIndexStr = this.<EditText>findViewById(R.id.edt_nlk_sign_pvt_key_index).getText().toString();
            int pubKeyIndex = NumberUtil.parseInt(pubKeyIndexStr);
            int pvtKeyIndex = NumberUtil.parseInt(pvtKeyIndexStr);
            if (!KeyIndexUtil.checkNlkSm2KeyIndex(pubKeyIndex) || !KeyIndexUtil.checkNlkSm2KeyIndex(pvtKeyIndex)) {
                showToast("key index should in [0,19]");
                return;
            }
            String userIdStr = this.<EditText>findViewById(R.id.edt_nlk_sign_user_id).getText().toString();
            if (TextUtils.isEmpty(userIdStr) || !Utility.checkHexValue(userIdStr)) {
                showToast("userId should not empty and should be hex string");
                return;
            }
            String dataInStr = this.<EditText>findViewById(R.id.edt_nlk_sign_data_in).getText().toString();
            if (TextUtils.isEmpty(dataInStr) || !Utility.checkHexValue(dataInStr)) {
                showToast("dataIn should not empty and should be hex string");
                return;
            }
            byte[] userId = ByteUtil.hexStr2Bytes(userIdStr);
            byte[] dataIn = ByteUtil.hexStr2Bytes(dataInStr);
            byte[] dataOut = new byte[64];
            addStartTimeWithClear("nlk->sm2Sign()");
            int len = MyApplication.app.noLostKeyManagerV2.sm2Sign(pubKeyIndex, pvtKeyIndex, userId, dataIn, dataOut);
            addEndTime("nlk->sm2Sign()");
            LogUtil.e(TAG, "sm2Sign()->code:" + len);
            if (len >= 0) {//success
                String validStr = ByteUtil.bytes2HexStr(Arrays.copyOf(dataOut, len));
                LogUtil.e(TAG, "sm2 sign success, signature:" + validStr);
                TextView textView = findViewById(R.id.tv_nlk_sign_result);
                textView.setText("signature:" + validStr);
            } else {//failed
                showToast("sm2 sign failed, code:" + len);
            }
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * SM2 verify signature
     * <br/> SM2 signature data length is 64B
     */
    private void nlkSm2VerifySignature() {
        try {
            String pubKeyIndexStr = this.<EditText>findViewById(R.id.edt_nlk_verify_pub_key_index).getText().toString();
            int pubKeyIndex = NumberUtil.parseInt(pubKeyIndexStr);
            if (!KeyIndexUtil.checkNlkSm2KeyIndex(pubKeyIndex)) {
                showToast("public key index should in [0,19]");
                return;
            }
            String userIdStr = this.<EditText>findViewById(R.id.edt_nlk_verify_user_id).getText().toString();
            if (TextUtils.isEmpty(userIdStr) || !Utility.checkHexValue(userIdStr)) {
                showToast("userId should not empty and should be hex string");
                return;
            }
            String dataInStr = this.<EditText>findViewById(R.id.edt_nlk_verify_data_in).getText().toString();
            if (TextUtils.isEmpty(dataInStr) || !Utility.checkHexValue(dataInStr)) {
                showToast("dataIn should not empty and should be hex string");
                return;
            }
            String signatureStr = this.<EditText>findViewById(R.id.edt_nlk_verify_signature_data).getText().toString();
            if (TextUtils.isEmpty(signatureStr) || !Utility.checkHexValue(signatureStr)) {
                showToast("signature should not empty and should be hex string");
                return;
            }
            byte[] userId = ByteUtil.hexStr2Bytes(userIdStr);
            byte[] dataIn = ByteUtil.hexStr2Bytes(dataInStr);
            byte[] signature = ByteUtil.hexStr2Bytes(signatureStr);
            addStartTimeWithClear("nlk->sm2VerifySign()");
            int code = MyApplication.app.noLostKeyManagerV2.sm2VerifySign(pubKeyIndex, userId, dataIn, signature);
            addEndTime("nlk->sm2VerifySign()");
            LogUtil.e(TAG, "sm2 verify signature code:" + code);
            if (code == 0) {//success
                LogUtil.e(TAG, "sm2 verify signature success");
                showToast("sm2 verify signature success");
            } else {//failed
                LogUtil.e(TAG, "sm2 verify signature failed,code:" + code);
                showToast("sm2 verify signature failed,code:" + code);
            }
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** SM2 encryption */
    private void nlkSm2Encrypt() {
        try {
            String pubKeyIndexStr = this.<EditText>findViewById(R.id.edt_nlk_enc_pub_key_index).getText().toString();
            int pubKeyIndex = NumberUtil.parseInt(pubKeyIndexStr);
            if (!KeyIndexUtil.checkNlkSm2KeyIndex(pubKeyIndex)) {
                showToast("public key index should in [0,19]");
                return;
            }
            String dataInStr = this.<EditText>findViewById(R.id.edt_nlk_enc_data_in).getText().toString();
            if (TextUtils.isEmpty(dataInStr) || !Utility.checkHexValue(dataInStr)) {
                showToast("dataIn should not empty and should be hex string");
                return;
            }
            byte[] dataIn = ByteUtil.hexStr2Bytes(dataInStr);
            byte[] dataOut = new byte[1024];
            addStartTimeWithClear("nlk->sm2EncryptData()");
            int len = MyApplication.app.noLostKeyManagerV2.sm2EncryptData(pubKeyIndex, dataIn, dataOut);
            addEndTime("nlk->sm2EncryptData()");
            LogUtil.e(TAG, "sm2 encrypt data code:" + len);
            if (len >= 0) {//success
                String validStr = ByteUtil.bytes2HexStr(Arrays.copyOf(dataOut, len));
                LogUtil.e(TAG, "sm2 encrypt data, out:" + validStr);
                TextView textView = findViewById(R.id.tv_nlk_encrypt_result);
                textView.setText("encrypted data:" + validStr);
            } else {//failed
                showToast("sm2 encrypt data failed,code:" + len);
            }
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** SM2 decryption */
    private void nlkSm2Decrypt() {
        try {
            String pvtKeyIndexStr = this.<EditText>findViewById(R.id.edt_nlk_dec_pvt_key_index).getText().toString();
            int pvtKeyIndex = NumberUtil.parseInt(pvtKeyIndexStr);
            if (!KeyIndexUtil.checkNlkSm2KeyIndex(pvtKeyIndex)) {
                showToast("private key index should in [0,19]");
                return;
            }
            String dataInStr = this.<EditText>findViewById(R.id.edt_nlk_dec_data_in).getText().toString();
            if (TextUtils.isEmpty(dataInStr) || !Utility.checkHexValue(dataInStr)) {
                showToast("dataIn should not empty and should be hex string");
                return;
            }
            byte[] dataIn = ByteUtil.hexStr2Bytes(dataInStr);
            byte[] dataOut = new byte[1024];
            addStartTimeWithClear("nlk->sm2DecryptData()");
            int len = MyApplication.app.noLostKeyManagerV2.sm2DecryptData(pvtKeyIndex, dataIn, dataOut);
            addEndTime("nlk->sm2DecryptData()");
            LogUtil.e(TAG, "sm2 decrypt data code:" + len);
            if (len >= 0) {//success
                String validStr = ByteUtil.bytes2HexStr(Arrays.copyOf(dataOut, len));
                LogUtil.e(TAG, "sm2 decrypt data, out:" + validStr);
                TextView textView = findViewById(R.id.tv_nlk_decrypt_result);
                textView.setText("Decrypted data:" + validStr);
            } else {//failed
                showToast("sm2 decrypt data failed, code:" + len);
            }
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Calc SM3 hash with Z(ID) */
    private void nlkCalcSM3HashWithID() {
        try {
            String pukKeyIndexStr = this.<EditText>findViewById(R.id.edt_nlk_calc_hash_pub_key_index).getText().toString();
            int pukKeyIndex = NumberUtil.parseInt(pukKeyIndexStr);
            if (!KeyIndexUtil.checkNlkSm2KeyIndex(pukKeyIndex)) {
                showToast("private key index should in [0,19]");
                return;
            }
            String userIdStr = this.<EditText>findViewById(R.id.edt_nlk_calc_hash_user_id).getText().toString();
            if (TextUtils.isEmpty(userIdStr) || !Utility.checkHexValue(userIdStr)) {
                showToast("userId should not empty and should be hex string");
                return;
            }
            String dataInStr = this.<EditText>findViewById(R.id.edt_nlk_calc_hash_data_in).getText().toString();
            if (TextUtils.isEmpty(dataInStr) || !Utility.checkHexValue(dataInStr)) {
                showToast("dataIn should not empty and should be hex string");
                return;
            }
            byte[] userId = ByteUtil.hexStr2Bytes(userIdStr);
            byte[] dataIn = ByteUtil.hexStr2Bytes(dataInStr);
            byte[] buffer = new byte[1024];

            addStartTimeWithClear("nlk->calcSM3HashWithID()");
            int len = MyApplication.app.noLostKeyManagerV2.calcSM3HashWithID(pukKeyIndex, userId, dataIn, buffer);
            addEndTime("nlk->calcSM3HashWithID()");
            LogUtil.e(TAG, "calcSM3HashWithID->code:" + len);
            if (len >= 0) {
                String hashStr = ByteUtil.bytes2HexStr(Arrays.copyOf(buffer, len));
                LogUtil.e(TAG, "calcSM3HashWithID()->hash:" + hashStr);
                TextView textView = findViewById(R.id.tv_nlk_calc_sm3_hash_result);
                textView.setText("hash:" + hashStr);
            } else {
                showToast("calcSM3HashWithID()->failed, code:" + len);
            }
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * SM2 single sign
     */
    private void nlkSm2SingleSign() {
        try {
            String pvtIndexStr = this.<EditText>findViewById(R.id.edt_nlk_single_sign_pvt_key_index).getText().toString();
            int pvtKeyIndex = NumberUtil.parseInt(pvtIndexStr);
            if (!KeyIndexUtil.checkNlkSm2KeyIndex(pvtKeyIndex)) {
                showToast("key index should in [0,19]");
                return;
            }
            String hashStr = this.<EditText>findViewById(R.id.edt_nlk_single_sign_hash).getText().toString();
            if (TextUtils.isEmpty(hashStr)) {
                showToast("key data should not be empty");
                return;
            }
            if (!Utility.checkHexValue(hashStr)) {
                showToast("key data should be hex string");
                return;
            }
            if (hashStr.length() != 64) {
                showToast("hash data length should be 32B");
                return;
            }
            byte[] hash = ByteUtil.hexStr2Bytes(hashStr);
            byte[] buffer = new byte[1024];
            addStartTimeWithClear("nlk->sm2SingleSign()");
            int len = MyApplication.app.noLostKeyManagerV2.sm2SingleSign(pvtKeyIndex, hash, buffer);
            addEndTime("nlk->sm2SingleSign()");
            LogUtil.e(TAG, "SM2 single sign, code:" + len);
            if (len >= 0) {
                String signature = ByteUtil.bytes2HexStr(Arrays.copyOf(buffer, len));
                LogUtil.e(TAG, "sm2SingleSign()->signature:" + signature);
                TextView textView = findViewById(R.id.tv_nlk_single_sign_result);
                textView.setText("signature:" + signature);
            } else {
                showToast("sm2SingleSign()->failed, code:" + len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
