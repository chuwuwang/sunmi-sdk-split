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

public class EccTestActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_ecc_test);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.security_ecc_test);
        findViewById(R.id.btn_gen_ecc_key_pair).setOnClickListener(this);
        findViewById(R.id.btn_inject_pub_key).setOnClickListener(this);
        findViewById(R.id.btn_inject_pvt_key).setOnClickListener(this);
        findViewById(R.id.btn_read_key).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_gen_ecc_key_pair:
                generateEccKeyPair();
                break;
            case R.id.btn_inject_pub_key:
                injectEccPublicKey();
                break;
            case R.id.btn_inject_pvt_key:
                injectEccPrivateKey();
                break;
            case R.id.btn_read_key:
                readEccKey();
                break;
        }
    }

    /** Generate ECC keypair */
    private void generateEccKeyPair() {
        try {
            EditText edt = findViewById(R.id.edt_gen_pvt_key_index);
            String pvtKeyIndexStr = edt.getText().toString();
            if (TextUtils.isEmpty(pvtKeyIndexStr)) {
                showToast("private key index should not be empty");
                edt.requestFocus();
                return;
            }
            edt = findViewById(R.id.edt_gen_pvt_key_size);
            String pvtKeySizeStr = edt.getText().toString();
            if (TextUtils.isEmpty(pvtKeySizeStr)) {
                showToast("private key size should not be empty");
                edt.requestFocus();
                return;
            }
            int pvtKeyIndex = NumberUtil.parseInt(pvtKeyIndexStr);
            int pvtKeySize = NumberUtil.parseInt(pvtKeySizeStr);
            byte[] dataOut = new byte[1024];
            addStartTimeWithClear("generateEccKeypair()");
            int len = MyApplication.app.securityOptV2.generateEccKeypair(pvtKeyIndex, pvtKeySize, dataOut);
            addEndTime("generateEccKeypair()");
            LogUtil.e(TAG, "generateEccKeypair len:" + len);
            showSpendTime();
            if (len >= 0) {
                String puk = ByteUtil.bytes2HexStr(Arrays.copyOf(dataOut, len));
                LogUtil.e(TAG, "puk = " + puk);
                showToast("generate ecc keypair success");
            } else {
                showToast("generate ecc keypair failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Inject a ECC public key */
    private void injectEccPublicKey() {
        try {
            EditText edt = findViewById(R.id.edt_inject_pub_key_index);
            String keyIndexStr = edt.getText().toString();
            if (TextUtils.isEmpty(keyIndexStr)) {
                showToast("public key index should not be empty");
                edt.requestFocus();
                return;
            }
            edt = findViewById(R.id.edt_inject_pub_key_size);
            String keySizeStr = edt.getText().toString();
            if (TextUtils.isEmpty(keySizeStr)) {
                showToast("public key size should not be empty");
                edt.requestFocus();
                return;
            }
            edt = findViewById(R.id.edt_inject_pub_key_data);
            String keyDataStr = edt.getText().toString();
            if (TextUtils.isEmpty(keyDataStr)) {
                showToast("public key data should not be empty");
                edt.requestFocus();
                return;
            }
            int keyIndex = NumberUtil.parseInt(keyIndexStr);
            int keySize = NumberUtil.parseInt(keySizeStr);
            byte[] keyData = ByteUtil.hexStr2Bytes(keyDataStr);
            addStartTimeWithClear("injectEccPubKey()");
            int code = MyApplication.app.securityOptV2.injectEccPubKey(keyIndex, keySize, keyData);
            addEndTime("injectEccPubKey()");
            LogUtil.e(TAG, "injectEccPubKey()->inject puk, code:" + code);
            if (code == 0) {//success
                showToast("inject ECC public key success");
            } else {//failed
                showToast("inject ECC public key failed,code:" + code);
            }
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Inject a ECC private key */
    private void injectEccPrivateKey() {
        try {
            EditText edt = findViewById(R.id.edt_inject_pvt_key_index);
            String keyIndexStr = edt.getText().toString();
            if (TextUtils.isEmpty(keyIndexStr)) {
                showToast("private key index should not be empty");
                edt.requestFocus();
                return;
            }
            edt = findViewById(R.id.edt_inject_pvt_key_size);
            String keySizeStr = edt.getText().toString();
            if (TextUtils.isEmpty(keySizeStr)) {
                showToast("private key size should not be empty");
                edt.requestFocus();
                return;
            }
            edt = findViewById(R.id.edt_inject_pvt_key_data);
            String keyDataStr = edt.getText().toString();
            if (TextUtils.isEmpty(keyDataStr)) {
                showToast("private key data should not be empty");
                return;
            }
            int keyIndex = NumberUtil.parseInt(keyIndexStr);
            int keySize = NumberUtil.parseInt(keySizeStr);
            byte[] keyData = ByteUtil.hexStr2Bytes(keyDataStr);
            addStartTimeWithClear("injectEccPvtKey()");
            int code = MyApplication.app.securityOptV2.injectEccPvtKey(keyIndex, keySize, keyData);
            addEndTime("injectEccPvtKey()");
            LogUtil.e(TAG, "injectEccPvtKey()->inject pvk, code:" + code);
            if (code == 0) {//success
                showToast("inject ECC private key success");
            } else {//failed
                showToast("inject ECC private key failed,code:" + code);
            }
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Read ECC key */
    private void readEccKey() {
        try {
            String keyIndexStr = this.<EditText>findViewById(R.id.edt_read_key_index).getText().toString();
            if (TextUtils.isEmpty(keyIndexStr)) {
                showToast("key index should not be empty");
                return;
            }
            TextView tvKeyInfo = findViewById(R.id.tv_key_info);
            int keyIndex = NumberUtil.parseInt(keyIndexStr);
            Bundle bundle = new Bundle();
            int code = MyApplication.app.securityOptV2.getEccPubKey(keyIndex, bundle);
            if (code < 0) {
                showToast("Read ECC key failed, code:" + code);
                return;
            }
            byte[] puk = bundle.getByteArray("publicKey");
            String msg = Utility.formatStr("publicKey:%s", ByteUtil.bytes2HexStr(puk));
            tvKeyInfo.setText(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
