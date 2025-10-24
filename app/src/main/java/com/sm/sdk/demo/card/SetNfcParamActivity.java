package com.sm.sdk.demo.card;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputLayout;
import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.NumberUtil;
import com.sunmi.pay.hardware.aidl.AidlConstants;
import com.sunmi.pay.hardware.aidl.AidlConstants.SysParam;

import java.util.ArrayList;
import java.util.List;

/** Set device nfc param */
public class SetNfcParamActivity extends BaseAppCompatActivity {
    private TextView tvDeviceInfo;
    private EditText edtMode;
    private TextInputLayout layout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_set_nfc_param);
        initView();
        getDeviceInfo();
        getSupportedNfcParamList();
    }

    private void initView() {
        initToolbarBringBack(R.string.card_set_nfc_param);
        tvDeviceInfo = findViewById(R.id.tv_device_info);
        layout = findViewById(R.id.edt_mode_layout);
        edtMode = findViewById(R.id.edt_mode);
        findViewById(R.id.btn_ok).setOnClickListener((v) -> setNfcParam());
    }

    private void getDeviceInfo() {
        String deviceMode = getDeviceModel();
        String nfcChip = getNfcChip();
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.basic_nfc_config)).append(nfcChip).append("\n")
                .append(getString(R.string.other_version_device)).append(deviceMode);
        tvDeviceInfo.setText(sb);
    }

    /** Get device model */
    private String getDeviceModel() {
        try {
            return MyApplication.app.basicOptV2.getSysParam(SysParam.DEVICE_MODEL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "unknown";
    }

    /**
     * Get nfc chip type
     * <br/>非接卡配置
     * <li>“00”: 表示没有非接模块</li>
     * <li>“01”: 表示有非接模块为RC531</li>
     * <li>“02”: 表示非接模块为PN512</li>
     * <li>“03”: 表示有非接模块为RC663</li>
     * <li>“04”: 表示非接模块为AS3911</li>
     * <li>“06”: 表示非接模块为MH1608C</li>
     * <li>“07”:表示非接模块为PN5190</li>
     * <li>“08”:表示非接模块为ST3916</li>
     * <li>“09”:表示非接模块为ST3917</li>
     * <li>“10”:表示非接模块为FM17660</li>
     * <li>(支持所有机型)</li>
     */
    private String getNfcChip() {
        try {
            String value = MyApplication.app.basicOptV2.getSysParam(AidlConstants.SysParam.NFC_CONFIG);
            if (!TextUtils.isEmpty(value) && TextUtils.isDigitsOnly(value)) {
                int intValue = Integer.parseInt(value);
                switch (intValue) {
                    case 0:
                        return "unknown";
                    case 1:
                        return "RC531";
                    case 2:
                        return "PN512";
                    case 3:
                        return "RC663";
                    case 4:
                        return "AS3911";
                    case 6:
                        return "MH1608C";
                    case 7:
                        return "PN5190";
                    case 8:
                        return "ST3916";
                    case 9:
                        return "ST3917";
                    case 10:
                        return "FM17660";
                    case 11:
                        return "FM17660K";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "unknown";
    }

    /** Get supported nfc param list */
    private void getSupportedNfcParamList() {
        try {
            Bundle current = new Bundle();
            List<Bundle> supported = new ArrayList<>();
            int code = MyApplication.app.readCardOptV2.getNfcParam(current, supported);
            Log.e(TAG, "getNfcParam()->code:" + code);
            if (code != 0) {
                return;
            }
            final String KEY_MODE = "mode";
            StringBuilder sb = new StringBuilder();
            sb.append(KEY_MODE).append("[");
            for (Bundle item : supported) {
                sb.append(item.getInt(KEY_MODE));
                sb.append(",");
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            sb.append("]");
            layout.setHint(sb);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Get Nfc Param */
    private void setNfcParam() {
        try {
            String modeStr = edtMode.getText().toString();
            if (TextUtils.isEmpty(modeStr)) {
                showToast("param mode shouldn't be empty");
                edtMode.requestFocus();
                return;
            }
            int mode = NumberUtil.parseInt(modeStr);
            Bundle bundle = new Bundle();
            bundle.putInt("mode", mode);
            int code = MyApplication.app.readCardOptV2.setNfcParam(bundle);
            String msg = "setNfcParam()->code:" + code;
            Log.e(TAG, msg);
            showToast(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
