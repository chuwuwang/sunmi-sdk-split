package com.sm.sdk.demo.security;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.NumberUtil;
import com.sm.sdk.demo.utils.Utility;

public class NlkCertTestActivity extends BaseAppCompatActivity {
    private int certType = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_nlk_cert_test);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.security_nlk_cert_test);
        RadioGroup rgCertType = findViewById(R.id.rg_save_cert_type);
        rgCertType.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.rb_save_cert_type_rsa:
                    certType = 0;
                    break;
                case R.id.rb_save_cert_type_ecc:
                    certType = 1;
                    break;
            }
        });
        rgCertType.check(R.id.rb_save_cert_type_rsa);
        findViewById(R.id.btn_nlk_save_cert).setOnClickListener(this);
        findViewById(R.id.btn_nlk_get_cert).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_nlk_save_cert:
                nlkSaveCert();
                break;
            case R.id.btn_nlk_get_cert:
                nlkGetCert();
                break;
        }
    }

    /** Save noLost cert */
    private void nlkSaveCert() {
        try {
            String dataInStr = this.<EditText>findViewById(R.id.edt_nlk_save_cert_data).getText().toString();
            String certIndexStr = this.<EditText>findViewById(R.id.edt_nlk_save_cert_index).getText().toString();
            int certIndex = NumberUtil.parseInt(certIndexStr);
            if (!KeyIndexUtil.checkNlkCertKeyIndex(certIndex)) {
                showToast(R.string.security_nlk_hint_cert_range);
            }
            if (dataInStr.trim().isEmpty()) {
                showToast(R.string.security_source_data_hint);
                return;
            }
            if (dataInStr.length() % 2 != 0) {
                showToast(R.string.security_source_data_hint);
                return;
            }
            byte[] certData = ByteUtil.hexStr2Bytes(dataInStr);
            Bundle bundle = new Bundle();
            bundle.putInt("certType", certType);
            bundle.putInt("certIndex", certIndex);
            bundle.putByteArray("certData", certData);

            addStartTimeWithClear("nlk->saveCert()");
            int code = MyApplication.app.noLostKeyManagerV2.saveCert(bundle);
            addEndTime("nlk->saveCert()");
            showSpendTime();
            if (code != 0) {
                String msg = "saveCert()->failed, code:" + code;
                showToast(msg);
                LogUtil.e(TAG, msg);
            } else {
                String msg = "saveCert()->success";
                showToast(msg);
                LogUtil.e(TAG, msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /** Get noLost cert */
    private void nlkGetCert() {
        try {
            String certIndexStr = this.<EditText>findViewById(R.id.edt_nlk_get_cert_index).getText().toString();
            int certIndex = NumberUtil.parseInt(certIndexStr);
            if (!KeyIndexUtil.checkNlkCertKeyIndex(certIndex)) {
                showToast(R.string.security_nlk_hint_cert_range);
            }
            Bundle bundle = new Bundle();
            addStartTimeWithClear("nlk->getCert()");
            int code = MyApplication.app.noLostKeyManagerV2.getCert(certIndex, bundle);
            addEndTime("nlk->getCert()");
            showSpendTime();
            if (code != 0) {
                String msg = "getCert()->failed, code:" + code;
                showToast(msg);
                LogUtil.e(TAG, msg);
            } else {
                int certType = bundle.getInt("certType");
                byte[] certData = bundle.getByteArray("certData");
                String certTypeStr = getString(R.string.security_nlk_cert_rsa);
                if (certType == 1) {
                    certTypeStr = getString(R.string.security_nlk_cert_ecc);
                }
                String msg = Utility.formatStr("getCert()->success\ncertType:%s\ncertData:%s", certTypeStr, ByteUtil.bytes2HexStr(certData));
                showToast(msg);
                LogUtil.e(TAG, msg);
                TextView tvInfo = findViewById(R.id.tv_get_cert_result);
                tvInfo.setText(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
