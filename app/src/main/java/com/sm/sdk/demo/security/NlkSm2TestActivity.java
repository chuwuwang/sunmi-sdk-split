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

public class NlkSm2TestActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_nlk_sm2_test);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.security_nlk_sm2_test);
        findViewById(R.id.btn_nlk_gen_sm2_key_pair).setOnClickListener(this);
        findViewById(R.id.btn_nlk_inject_sm2_key).setOnClickListener(this);
        findViewById(R.id.btn_nlk_sm2_read_key).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_nlk_gen_sm2_key_pair:
                nlkGenerateSm2KeyPair();
                break;
            case R.id.btn_nlk_inject_sm2_key:
                nlkInjectSm2Key();
                break;
            case R.id.btn_nlk_sm2_read_key:
                nlkReadSm2key();
                break;
        }
    }

    /** Generate SM2 keypair */
    private void nlkGenerateSm2KeyPair() {
        try {
            String pvtKeyIndexStr = this.<EditText>findViewById(R.id.edt_nlk_gen_pvt_key_index).getText().toString();
            int pvtKeyIndex = NumberUtil.parseInt(pvtKeyIndexStr);
            if (!KeyIndexUtil.checkNlkSm2KeyIndex(pvtKeyIndex)) {
                showToast("private key index should in [0,19]");
                return;
            }
            Bundle bundle = new Bundle();
            addStartTimeWithClear("nlk->generateSM2Keypair()");
            int code = MyApplication.app.noLostKeyManagerV2.generateSM2Keypair(pvtKeyIndex, bundle);
            addEndTime("nlk->generateSM2Keypair()");
            LogUtil.e(TAG, "nlk->generate SM2 keypair code:" + code);
            if (code == 0) {
                byte[] data = bundle.getByteArray("data");
                byte[] kcv = bundle.getByteArray("kcv");
                byte[] rfu = bundle.getByteArray("rfu");
                LogUtil.e(TAG, "generate SM2 keypair success,publicKey={"
                        + "\ndata:" + ByteUtil.bytes2HexStr(data)
                        + "\nkcv:" + ByteUtil.bytes2HexStr(kcv)
                        + "\nrfu:" + ByteUtil.bytes2HexStr(rfu)
                        + "\n}");
                showToast("generate SM2 keypair success");
            } else {
                showToast("generate SM2 keypair failed");
            }
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Inject a SM2 key
     * <br/> SM2 public key 64B
     * <br/> MS2 private key 32B
     */
    private void nlkInjectSm2Key() {
        try {
            String keyIndexStr = this.<EditText>findViewById(R.id.edt_nlk_inject_key_index).getText().toString();
            int keyIndex = NumberUtil.parseInt(keyIndexStr);
            if (!KeyIndexUtil.checkNlkSm2KeyIndex(keyIndex)) {
                showToast("private key index should in [0,19]");
                return;
            }
            String keyDataStr = this.<EditText>findViewById(R.id.edt_nlk_inject_key_data).getText().toString();
            if (TextUtils.isEmpty(keyDataStr)) {
                showToast("key data should not be empty");
                return;
            }
            if (!Utility.checkHexValue(keyDataStr)) {
                showToast("key data should be hex string");
                return;
            }
            if (keyDataStr.length() != 64 && keyDataStr.length() != 128) {
                showToast("key data length should be 32B(privateKey) or 64B(PublicKey)");
                return;
            }
            String kcvStr = this.<EditText>findViewById(R.id.edt_nlk_inject_kcv).getText().toString();
            if (!TextUtils.isEmpty(kcvStr) && kcvStr.length() != 10) {
                showToast("kcv length should be 5B");
                return;
            }
            String rfuStr = this.<EditText>findViewById(R.id.edt_nlk_inject_rfu).getText().toString();
            if (!TextUtils.isEmpty(rfuStr) && rfuStr.length() != 20) {
                showToast("kcv length should be 10B");
                return;
            }
            Bundle bundle = new Bundle();
            bundle.putByteArray("data", ByteUtil.hexStr2Bytes(keyDataStr));
            bundle.putByteArray("kcv", ByteUtil.hexStr2Bytes(kcvStr));
            bundle.putByteArray("rfu", ByteUtil.hexStr2Bytes(rfuStr));
            addStartTimeWithClear("nlk->injectSM2Key()");
            int code = MyApplication.app.noLostKeyManagerV2.injectSM2Key(keyIndex, bundle);
            addEndTime("nlk->injectSM2Key()");
            LogUtil.e(TAG, "nlk->inject SM2 key code:" + code);
            String msg = "inject SM2 key " + (code == 0 ? "success" : "failed");
            LogUtil.e(TAG, msg);
            showToast(msg);
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Read SM2 public key */
    private void nlkReadSm2key() {
        try {
            String pubKeyIndexStr = this.<EditText>findViewById(R.id.edt_nlk_read_key_index).getText().toString();
            int pubKeyIndex = NumberUtil.parseInt(pubKeyIndexStr);
            if (!KeyIndexUtil.checkNlkSm2KeyIndex(pubKeyIndex)) {
                showToast("key index should in [0,19]");
                return;
            }
            Bundle bundle = new Bundle();
            addStartTimeWithClear("nlk->readSM2Key()");
            int code = MyApplication.app.noLostKeyManagerV2.readSM2Key(pubKeyIndex, bundle);
            addEndTime("nlk->readSM2Key()");
            LogUtil.e(TAG, "nlk->read sm2 key, code:" + code);
            if (code == 0) {//success
                String keyDataStr = ByteUtil.bytes2HexStr(bundle.getByteArray("keyData"));
                LogUtil.e(TAG, "readSM2Key()->keyData:" + keyDataStr);
                TextView textView = findViewById(R.id.txt_nlk_read_key_result);
                textView.setText("keyData:" + keyDataStr);
            } else {//failed
                showToast("readSM2Key()->failed, code:" + code);
            }
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
