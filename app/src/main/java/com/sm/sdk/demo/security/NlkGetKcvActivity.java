package com.sm.sdk.demo.security;

import android.os.Bundle;
import android.text.TextUtils;
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
import com.sunmi.pay.hardware.aidl.AidlConstants.Security;

public class NlkGetKcvActivity extends BaseAppCompatActivity {
    private EditText edtKeyIndex;
    private EditText edtTargetPkgName;
    private int keySystem = Security.SEC_MKSK_NOLOST;
    private int kcvMode = Security.KCV_MODE_CHK0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_nlk_get_kcv);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.security_nlk_get_kcv);
        edtTargetPkgName = findViewById(R.id.edt_target_pkg_name);
        edtKeyIndex = findViewById(R.id.key_index);
        RadioGroup group = findViewById(R.id.key_system_group);
        group.setOnCheckedChangeListener((group1, checkedId) -> {
            if (checkedId == R.id.rdo_nlk_sys_mksk) {
                keySystem = Security.SEC_MKSK_NOLOST;
            }
        });
        group.check(R.id.rdo_nlk_sys_mksk);
        group = findViewById(R.id.kcv_mode_group);
        group.setOnCheckedChangeListener((group1, checkedId) -> {
            switch (checkedId) {
                case R.id.kcv_mode_nochk:
                    kcvMode = Security.KCV_MODE_NOCHK;
                    break;
                case R.id.kcv_mode_chk0:
                    kcvMode = Security.KCV_MODE_CHK0;
                    break;
                case R.id.kcv_mode_chkfix:
                    kcvMode = Security.KCV_MODE_CHKFIX;
                    break;
                case R.id.kcv_mode_chkmac:
                    kcvMode = Security.KCV_MODE_CHKMAC;
                    break;
                case R.id.kcv_mode_chkcmac:
                    kcvMode = Security.KCV_MODE_CHKCMAC;
                    break;
                case R.id.kcv_mode_chkfix_16:
                    kcvMode = Security.KCV_MODE_CHKFIX_16;
                    break;
                case R.id.kcv_mode_chkbuf:
                    kcvMode = Security.KCV_MODE_CHK_BUF;
                    break;
                case R.id.kcv_mode_chkcmac_buf:
                    kcvMode = Security.KCV_MODE_CHKCMAC_BUF;
                    break;
            }
        });
        group.check(R.id.kcv_mode_chk0);
        findViewById(R.id.mb_get_kcv).setOnClickListener((v) -> nlkGetKcv());
    }

    private void nlkGetKcv() {
        try {
            String targetPkgName = edtTargetPkgName.getText().toString();
            String keyIndexStr = edtKeyIndex.getText().toString();
            int keyIndex = NumberUtil.parseInt(keyIndexStr);
            if (keySystem == Security.SEC_MKSK_NOLOST) {
                if (!KeyIndexUtil.checkNlkMkskKeyIndex(keyIndex)) {
                    showToast("Please input correct key index(range 0-99)");
                    return;
                }
            }
            byte[] dataOut = new byte[4];
            Bundle bundle = new Bundle();
            bundle.putInt("keySystem", keySystem);
            bundle.putInt("keyIndex", keyIndex);
            bundle.putInt("kcvMode", kcvMode);
            if (!TextUtils.isEmpty(targetPkgName)) {
                bundle.putString("targetAppPkgName", targetPkgName);
            }
            addStartTimeWithClear("nlk->getKeyCheckValue()");
            int code = MyApplication.app.noLostKeyManagerV2.getKeyCheckValue(bundle, dataOut);
            addEndTime("nlk->getKeyCheckValue()");
            if (code < 0) {
                String msg = "Get kcv error:" + code;
                LogUtil.e(TAG, msg);
                showToast(msg);
            } else {
                String hexKcv = ByteUtil.bytes2HexStr(dataOut);
                this.<TextView>findViewById(R.id.tv_info).setText("KCV:" + hexKcv);
            }
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
            showToast("key illegal key index");
        }
    }
}
