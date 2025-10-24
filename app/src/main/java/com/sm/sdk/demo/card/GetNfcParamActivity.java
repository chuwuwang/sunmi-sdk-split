package com.sm.sdk.demo.card;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sunmi.pay.hardware.aidl.AidlConstants.SysParam;

import java.util.ArrayList;
import java.util.List;

/** Get device nfc param */
public class GetNfcParamActivity extends BaseAppCompatActivity {
    private TextView tvDeviceInfo;
    private TextView tvNfcParam;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_get_nfc_param);
        intView();
        getDeviceInfo();
    }

    private void intView() {
        initToolbarBringBack(R.string.card_get_nfc_param);
        tvDeviceInfo = findViewById(R.id.tv_device_info);
        tvNfcParam = findViewById(R.id.tv_nfc_param);
        findViewById(R.id.btn_ok).setOnClickListener((v) -> getNfcParam());
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
            String value = MyApplication.app.basicOptV2.getSysParam(SysParam.NFC_CONFIG);
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

    /** Get Nfc Param */
    private void getNfcParam() {
        try {
            Bundle current = new Bundle();
            List<Bundle> supported = new ArrayList<>();
            int code = MyApplication.app.readCardOptV2.getNfcParam(current, supported);
            Log.e(TAG, "getNfcParam()->code:" + code);
            if (code != 0) {
                return;
            }
            final String KEY_MODE = "mode";
            final String KEY_CARD_A_REG = "card_A_reg";
            final String KEY_CARD_B_REG = "card_B_reg";
            int mode = current.getInt(KEY_MODE);
            String card_A_reg = current.getString(KEY_CARD_A_REG);
            String card_B_reg = current.getString(KEY_CARD_B_REG);
            StringBuilder sb = new StringBuilder();
            sb.append("current nfc param:\n")
                    .append(KEY_MODE).append(":").append(mode).append("\n")
                    .append(KEY_CARD_A_REG).append(":").append(card_A_reg).append("\n")
                    .append(KEY_CARD_B_REG).append(":").append(card_B_reg).append("\n");
            sb.append("\nsupported nfc params:\n");
            for (Bundle item : supported) {
                mode = item.getInt(KEY_MODE);
                card_A_reg = item.getString(KEY_CARD_A_REG);
                card_B_reg = item.getString(KEY_CARD_B_REG);
                sb.append(KEY_MODE).append(":").append(mode).append("\n")
                        .append(KEY_CARD_A_REG).append(":").append(card_A_reg).append("\n")
                        .append(KEY_CARD_B_REG).append(":").append(card_B_reg).append("\n");
            }
            tvNfcParam.setText(sb);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
