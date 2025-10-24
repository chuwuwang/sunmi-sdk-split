package com.sm.sdk.demo.security;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.NumberUtil;
import com.sm.sdk.demo.utils.Utility;
import com.sunmi.pay.hardware.aidl.AidlConstants.Security;

import java.util.Arrays;

public class NlkEccRecoverActivity extends BaseAppCompatActivity {
    private int signHash = Security.HASH_SHA_TYPE_1;
    private int verifyHash = Security.HASH_SHA_TYPE_1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_nlk_ecc_recover);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.security_nlk_ecc_recover);
        RadioGroup rgSign = findViewById(R.id.rg_nlk_sign_hash);
        rgSign.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.rb_nlk_sign_hash_type_sha1:
                    signHash = Security.HASH_SHA_TYPE_1;
                    break;
                case R.id.rb_nlk_sign_hash_type_sha224:
                    signHash = Security.HASH_SHA_TYPE_224;
                    break;
                case R.id.rb_nlk_sign_hash_type_sha256:
                    signHash = Security.HASH_SHA_TYPE_256;
                    break;
                case R.id.rb_nlk_sign_hash_type_sha384:
                    signHash = Security.HASH_SHA_TYPE_384;
                    break;
                case R.id.rb_nlk_sign_hash_type_sha512:
                    signHash = Security.HASH_SHA_TYPE_512;
                    break;
            }
        });
        rgSign.check(R.id.rb_nlk_sign_hash_type_sha1);
        RadioGroup rgVerify = findViewById(R.id.rg_nlk_verify_hash);
        rgVerify.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.rb_nlk_verify_hash_type_sha1:
                    verifyHash = Security.HASH_SHA_TYPE_1;
                    break;
                case R.id.rb_nlk_verify_hash_type_sha224:
                    verifyHash = Security.HASH_SHA_TYPE_224;
                    break;
                case R.id.rb_nlk_verify_hash_type_sha256:
                    verifyHash = Security.HASH_SHA_TYPE_256;
                    break;
                case R.id.rb_nlk_verify_hash_type_sha384:
                    verifyHash = Security.HASH_SHA_TYPE_384;
                    break;
                case R.id.rb_nlk_verify_hash_type_sha512:
                    verifyHash = Security.HASH_SHA_TYPE_512;
                    break;
            }
        });
        rgVerify.check(R.id.rb_nlk_verify_hash_type_sha1);
        findViewById(R.id.btn_nlk_recover_enc).setOnClickListener(this);
        findViewById(R.id.btn_nlk_recover_dec).setOnClickListener(this);
        findViewById(R.id.btn_nlk_sign).setOnClickListener(this);
        findViewById(R.id.btn_nlk_verify).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_nlk_recover_enc:
                nlkEccPukEncrypt();
                break;
            case R.id.btn_nlk_recover_dec:
                nlkEccPvkDecrypt();
                break;
            case R.id.btn_nlk_sign:
                nlkEccPvkSign();
                break;
            case R.id.btn_nlk_verify:
                nlkEccPukVerify();
                break;
        }
    }

    /** ECC public key encryption */
    private void nlkEccPukEncrypt() {
        try {
            String dataInStr = this.<EditText>findViewById(R.id.edt_nlk_recover_enc_data_in).getText().toString();
            String pukIndexStr = this.<EditText>findViewById(R.id.edt_nlk_recover_enc_puk_index).getText().toString();
            int pukIndex = NumberUtil.parseInt(pukIndexStr);
            if (!KeyIndexUtil.checkNlkEccKeyIndex(pukIndex)) {
                showToast(R.string.security_nlk_hint_ecc_range);
            }
            if (dataInStr.trim().isEmpty()) {
                showToast(R.string.security_source_data_hint);
                return;
            }
            if (dataInStr.length() % 2 != 0) {
                showToast(R.string.security_source_data_hint);
                return;
            }
            byte[] dataIn = ByteUtil.hexStr2Bytes(dataInStr);
            byte[] dataOut = new byte[2048];
            addStartTimeWithClear("nlk->eccRecover()");
            int len = MyApplication.app.noLostKeyManagerV2.eccRecover(pukIndex, dataIn, dataOut); //
            addEndTime("nlk->eccRecover()");
            showSpendTime();
            if (len < 0) {
                String msg = "eccRecover()->puk encrypt failed, code:" + len;
                showToast(msg);
                LogUtil.e(TAG, msg);
            } else {
                byte[] valid = Arrays.copyOf(dataOut, len);
                String hexStr = ByteUtil.bytes2HexStr(valid);
                LogUtil.e(TAG, "eccRecover()->puk encrypt, output:" + hexStr);
                TextView tvInfo = findViewById(R.id.tv_nlk_recover_enc_result);
                tvInfo.setText(hexStr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** ECC private key decryption */
    private void nlkEccPvkDecrypt() {
        try {
            String dataInStr = this.<EditText>findViewById(R.id.edt_nlk_recover_dec_data_in).getText().toString();
            String pvkIndexStr = this.<EditText>findViewById(R.id.edt_nlk_recover_dec_pvk_index).getText().toString();
            int pvkIndex = NumberUtil.parseInt(pvkIndexStr);
            if (!KeyIndexUtil.checkNlkEccKeyIndex(pvkIndex)) {
                showToast(R.string.security_nlk_hint_ecc_range);
            }
            if (dataInStr.trim().isEmpty()) {
                showToast(R.string.security_source_data_hint);
                return;
            }
            if (dataInStr.length() % 2 != 0) {
                showToast(R.string.security_source_data_hint);
                return;
            }
            byte[] dataIn = ByteUtil.hexStr2Bytes(dataInStr);
            byte[] dataOut = new byte[2048];
            addStartTimeWithClear("nlk->eccRecover()");
            int len = MyApplication.app.noLostKeyManagerV2.eccRecover(pvkIndex, dataIn, dataOut); //
            addEndTime("nlk->eccRecover()");
            showSpendTime();
            if (len < 0) {
                String msg = "eccRecover()->pvk decrypt failed, code:" + len;
                showToast(msg);
                LogUtil.e(TAG, msg);
            } else {
                byte[] valid = Arrays.copyOf(dataOut, len);
                String hexStr = ByteUtil.bytes2HexStr(valid);
                LogUtil.e(TAG, "eccRecover()->pvk decrypt output:" + hexStr);
                TextView tvInfo = findViewById(R.id.tv_nlk_recover_dec_result);
                tvInfo.setText(hexStr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** ECC private key signing */
    private void nlkEccPvkSign() {
        try {
            String dataInStr = this.<EditText>findViewById(R.id.edt_nlk_sign_data_in).getText().toString();
            String pvkIndexStr = this.<EditText>findViewById(R.id.edt_nlk_sign_pvt_key_index).getText().toString();
            int pvkIndex = NumberUtil.parseInt(pvkIndexStr);
            if (!KeyIndexUtil.checkNlkEccKeyIndex(pvkIndex)) {
                showToast(R.string.security_nlk_hint_ecc_range);
            }
            if (dataInStr.trim().isEmpty()) {
                showToast(R.string.security_source_data_hint);
                return;
            }
            if (dataInStr.length() % 2 != 0) {
                showToast(R.string.security_source_data_hint);
                return;
            }
            byte[] dataIn = ByteUtil.hexStr2Bytes(dataInStr);
            byte[] dataOut = new byte[2048];
            addStartTimeWithClear("nlk->eccSign()");
            int len = MyApplication.app.noLostKeyManagerV2.eccSign(pvkIndex, signHash, dataIn, dataOut);
            addEndTime("nlk->eccSign()");
            showSpendTime();
            if (len < 0) {
                String msg = "eccSign()->failed, code:" + len;
                showToast(msg);
                LogUtil.e(TAG, msg);
            } else {
                byte[] valid = Arrays.copyOf(dataOut, len);
                String hexStr = ByteUtil.bytes2HexStr(valid);
                LogUtil.e(TAG, "eccSign()->output:" + hexStr);
                TextView tvInfo = findViewById(R.id.tv_nlk_recover_sign_result);
                tvInfo.setText(hexStr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** ECC public key verification */
    private void nlkEccPukVerify() {
        try {
            String dataInStr = this.<EditText>findViewById(R.id.edt_nlk_verify_data_in).getText().toString();
            String signatureStr = this.<EditText>findViewById(R.id.edt_nlk_verify_sign_data).getText().toString();
            String pukIndexStr = this.<EditText>findViewById(R.id.edt_nlk_verify_puk_key_index).getText().toString();
            int pukIndex = NumberUtil.parseInt(pukIndexStr);
            if (!KeyIndexUtil.checkNlkEccKeyIndex(pukIndex)) {
                showToast(R.string.security_nlk_hint_ecc_range);
            }
            if (dataInStr.trim().isEmpty()) {
                showToast(R.string.security_source_data_hint);
                return;
            }
            if (dataInStr.length() % 2 != 0) {
                showToast(R.string.security_source_data_hint);
                return;
            }
            if (signatureStr.trim().isEmpty()) {
                showToast(R.string.security_signature_data_hint);
                return;
            }
            if (signatureStr.length() % 2 != 0) {
                showToast(R.string.security_signature_data_hint);
                return;
            }
            byte[] dataIn = ByteUtil.hexStr2Bytes(dataInStr);
            byte[] signData = ByteUtil.hexStr2Bytes(signatureStr);
            addStartTimeWithClear("nlk->eccVerify()");
            int code = MyApplication.app.noLostKeyManagerV2.eccVerify(pukIndex, verifyHash, dataIn, signData);
            addEndTime("nlk->eccVerify()");
            showSpendTime();
            String msg = "eccVerify()->" + Utility.getStateString(code);
            LogUtil.e(TAG, msg);
            TextView tvInfo = findViewById(R.id.tv_nlk_recover_verify_result);
            tvInfo.setText(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
