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
import com.sm.sdk.demo.utils.NumberUtil;
import com.sunmi.pay.hardware.aidl.AidlConstants.Security;

public class NlkDeleteKeyActivity extends BaseAppCompatActivity {
    private EditText edtTargetPkgName;
    private EditText edtIndex;
    private int keySystem = Security.SEC_MKSK_NOLOST;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_nlk_delete_key);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.security_nlk_delete_key);
        RadioGroup rdoGroup = findViewById(R.id.key_system);
        rdoGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String keyIndexHint = getString(R.string.security_key_index);
            switch (checkedId) {
                case R.id.rb_sec_mksk:
                    keySystem = Security.SEC_MKSK_NOLOST;
                    edtIndex.setHint(keyIndexHint + "[0,99]");
                    break;
                case R.id.rb_sec_rsa_key:
                    keySystem = Security.SEC_RSA_KEY_NOLOST;
                    edtIndex.setHint(keyIndexHint + "[0,19]");
                    break;
                case R.id.rb_sec_ecc_key:
                    keySystem = Security.SEC_ECC_KEY_NOLOST;
                    edtIndex.setHint(keyIndexHint + "[0,19]");
                    break;
                case R.id.rb_sec_cert:
                    keySystem = Security.SEC_CERT_NOLOST;
                    edtIndex.setHint(keyIndexHint + "[0,9]");
                    break;
                case R.id.rb_sec_sm2_key:
                    keySystem = Security.SEC_SM2_KEY_NOLOST;
                    edtIndex.setHint(keyIndexHint + "[0,19]");
                    break;
            }
        });
        edtTargetPkgName = findViewById(R.id.edt_target_pkg_name);
        edtIndex = findViewById(R.id.key_index);
        findViewById(R.id.mb_ok).setOnClickListener(this);
        rdoGroup.check(R.id.rb_sec_mksk);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mb_ok:
                nlkDeleteKey();
                break;
        }
    }

    private void nlkDeleteKey() {
        try {
            String targetPkgName = edtTargetPkgName.getText().toString();
            String keyIndexStr = edtIndex.getText().toString().trim();
            int keyIndex = NumberUtil.parseInt(keyIndexStr);
            if (keySystem == Security.SEC_MKSK_NOLOST) {
                if (!KeyIndexUtil.checkNlkMkskKeyIndex(keyIndex)) {
                    showToast("Please input correct key index(range 0-99)");
                    return;
                }
            } else if (keySystem == Security.SEC_RSA_KEY_NOLOST) {
                if (!KeyIndexUtil.checkNlkRsaKeyIndex(keyIndex)) {
                    showToast("Please input correct key index(range 0-19)");
                    return;
                }
            } else if (keySystem == Security.SEC_ECC_KEY_NOLOST) {
                if (!KeyIndexUtil.checkNlkEccKeyIndex(keyIndex)) {
                    showToast("Please input correct key index(range 0-19)");
                    return;
                }
            } else if (keySystem == Security.SEC_CERT_NOLOST) {
                if (!KeyIndexUtil.checkNlkCertKeyIndex(keyIndex)) {
                    showToast("Please input correct key index(range 0-9)");
                    return;
                }
            } else if (keySystem == Security.SEC_SM2_KEY_NOLOST) {
                if (!KeyIndexUtil.checkNlkSm2KeyIndex(keyIndex)) {
                    showToast("Please input correct key index(range 0-19)");
                    return;
                }
            }
            Bundle bundle = new Bundle();
            bundle.putInt("keySystem", keySystem);
            bundle.putInt("keyIndex", keyIndex);
            if (!TextUtils.isEmpty(targetPkgName)) {
                bundle.putString("targetAppPkgName", targetPkgName);
            }
            addStartTimeWithClear("nlk->deleteKey()");
            int result = MyApplication.app.noLostKeyManagerV2.deleteKey(bundle);
            addEndTime("nlk->deleteKey()");
            toastHint(result);
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
