package com.sm.sdk.demo.security;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.NumberUtil;
import com.sm.sdk.demo.utils.Utility;
import com.sunmi.pay.hardware.aidl.AidlConstants.Security;

public class NlkSaveKeyActivity extends BaseAppCompatActivity {
    private EditText mEditKeyValue;
    private EditText mEditCheckValue;
    private EditText mEditKeyVariant;
    private EditText mEditKeyIndex;
    private EditText mDependKeyIndex;
    private boolean isEncrypt = false;

    private int mKeyType = Security.KEY_TYPE_KEK;
    private int mKeyAlgType = Security.KEY_ALG_TYPE_3DES;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_nlk_save_mksk_key);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.security_nlk_save_mksk);
        RadioGroup rgKeyType = findViewById(R.id.rg_key_type);
        rgKeyType.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.rb_kek:
                    mKeyType = Security.KEY_TYPE_KEK;
                    break;
                case R.id.rb_tmk:
                    mKeyType = Security.KEY_TYPE_TMK;
                    break;
                case R.id.rb_pik:
                    mKeyType = Security.KEY_TYPE_PIK;
                    break;
                case R.id.rb_tdk:
                    mKeyType = Security.KEY_TYPE_TDK;
                    break;
                case R.id.rb_mak:
                    mKeyType = Security.KEY_TYPE_MAK;
                    break;
                case R.id.rb_rec_key:
                    mKeyType = Security.KEY_TYPE_REC;
                    break;
            }
        });
        RadioGroup rgKeyAlgType = findViewById(R.id.rg_key_alg_type);
        rgKeyAlgType.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.rb_3des:
                    mKeyAlgType = Security.KEY_ALG_TYPE_3DES;
                    break;
                case R.id.rb_sm4:
                    mKeyAlgType = Security.KEY_ALG_TYPE_SM4;
                    break;
                case R.id.rb_aes:
                    mKeyAlgType = Security.KEY_ALG_TYPE_AES;
                    break;
            }
        });
        RadioGroup rgCipherKey = findViewById(R.id.rg_ciphertext);
        rgCipherKey.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.rb_yes:
                    isEncrypt = true;
                    findViewById(R.id.depend_key_lay).setVisibility(View.VISIBLE);
                    break;
                case R.id.rb_no:
                    isEncrypt = false;
                    findViewById(R.id.depend_key_lay).setVisibility(View.GONE);
                    break;
            }
        });
        rgCipherKey.check(R.id.rb_no);
        mEditKeyValue = findViewById(R.id.key_value);
        mEditCheckValue = findViewById(R.id.check_value);
        mEditKeyVariant = findViewById(R.id.key_variant);
        mEditKeyIndex = findViewById(R.id.key_index);
        mDependKeyIndex = findViewById(R.id.depend_key_index);
        findViewById(R.id.mb_ok).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mb_ok:
                saveNoLostMkskKey();
                break;
        }
    }

    private void saveNoLostMkskKey() {
        try {
            String keyValueStr = mEditKeyValue.getText().toString().trim();
            String checkValueStr = mEditCheckValue.getText().toString().trim();
            String keyVariantStr = mEditKeyVariant.getText().toString().trim();
            String keyIndexStr = mEditKeyIndex.getText().toString().trim();
            String dependKeyIndexStr = mDependKeyIndex.getText().toString().trim();

            if (keyValueStr.isEmpty() || keyValueStr.length() % 8 != 0) {
                showToast(R.string.security_key_value_hint);
                return;
            }
            if (!checkValueStr.isEmpty()) {
                if (mKeyAlgType == Security.KEY_ALG_TYPE_SM4) {
                    if (checkValueStr.length() > 32 || checkValueStr.length() % 4 != 0) {
                        showToast(R.string.security_check_value_hint);
                        return;
                    }
                } else {
                    if (checkValueStr.length() > 16 || checkValueStr.length() % 4 != 0) {
                        showToast(R.string.security_check_value_hint);
                        return;
                    }
                }
            }
            if (!TextUtils.isEmpty(keyVariantStr) && !Utility.checkHexValue(keyVariantStr)) {
                showToast("key variant should be hex string");
                return;
            }
            int keyIndex = NumberUtil.parseInt(keyIndexStr);
            if (!KeyIndexUtil.checkNlkMkskKeyIndex(keyIndex)) {
                showToast("Please input correct key index(range 0-99)");
                return;
            }
            int dependKeyIndex = 0;
            if (isEncrypt) {
                dependKeyIndex = NumberUtil.parseInt(dependKeyIndexStr);
                if (!KeyIndexUtil.checkNlkMkskKeyIndex(dependKeyIndex)) {
                    showToast("Please input correct key index(range 0-99)");
                    return;
                }
            }
            byte[] keyValue = ByteUtil.hexStr2Bytes(keyValueStr);
            byte[] checkValue = ByteUtil.hexStr2Bytes(checkValueStr);
            byte[] keyVariant = ByteUtil.hexStr2Bytes(keyVariantStr);

            Bundle bundle = new Bundle();
            bundle.putInt("keyType", mKeyType);
            bundle.putByteArray("keyValue", keyValue);
            bundle.putByteArray("checkValue", checkValue);
            bundle.putInt("encryptIndex", 0);
            bundle.putInt("keyAlgType", mKeyAlgType);
            bundle.putInt("keyIndex", keyIndex);
            bundle.putInt("encryptIndex", dependKeyIndex);
            bundle.putBoolean("isEncrypt", isEncrypt);
            bundle.putInt("variantUsage", Security.KEY_VARIANT_XOR);
            bundle.putByteArray("keyVariant", keyVariant);
            addStartTimeWithClear("nlk->saveKey()");
            int result = MyApplication.app.noLostKeyManagerV2.saveKey(bundle);
            addEndTime("nlk->saveKey()");
            toastHint(result);
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
