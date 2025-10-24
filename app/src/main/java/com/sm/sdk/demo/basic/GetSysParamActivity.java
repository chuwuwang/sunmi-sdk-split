package com.sm.sdk.demo.basic;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.DeviceUtil;
import com.sm.sdk.demo.utils.Utility;
import com.sunmi.pay.hardware.aidl.AidlConstants.SysParam;

import java.util.ArrayList;
import java.util.List;

public class GetSysParamActivity extends BaseAppCompatActivity {
    private TextView tvInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_get_sys_param);
        initView();
        initData();
    }

    private void initView() {
        initToolbarBringBack(R.string.basic_get_sys_param);
        tvInfo = findViewById(R.id.tv_info);
    }

    /** Get data fro SPHS */
    private void initData() {
        addStartTime("getSysParam() total");
        getDeviceTamperStatus();
        getSysParam();
        getDeviceCardStatus();
        getDeviceSAMCount();
        getDeviceNFCConfig();
        getEmvKernelVersion();
        getEmvKernelReleaseDate();
        addEndTime("getSysParam() total");
        showSpendTime();
    }

    /** Get device tamper statsu */
    private void getDeviceTamperStatus() {
        try {
            StringBuilder sb = new StringBuilder();
            if (DeviceUtil.isFinanceDevice() || DeviceUtil.isNPDevice()) {
                addStartTime("getSecStatus()");
                int status = MyApplication.app.securityOptV2.getSecStatus();
                addEndTime("getSecStatus()");
                sb.append(Utility.formatStr("SecStatus:%08X", status));
                sb.append("\n");
            } else {
                sb.append("SecStatus:null");
                sb.append("\n");
            }
            sb.deleteCharAt(sb.length() - 1);
            addTextViewText(sb, tvInfo, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Get system params */
    private void getSysParam() {
        List<String> keys = new ArrayList<>();
        keys.add(SysParam.BASE_VERSION);
        keys.add(SysParam.MSR2_FW_VER);
        keys.add(SysParam.TERM_STATUS);
        keys.add(SysParam.DEBUG_MODE);
        keys.add(SysParam.HARDWARE_VERSION);
        keys.add(SysParam.FIRMWARE_VERSION);
        keys.add(SysParam.SM_VERSION);
        keys.add(SysParam.SUPPORT_ETC);
        keys.add(SysParam.ETC_FIRM_VERSION);
        keys.add(SysParam.BootVersion);
        keys.add(SysParam.CFG_FILE_VERSION);
        keys.add(SysParam.FW_VERSION);
        keys.add(SysParam.SN);
        keys.add(SysParam.CSN);
        keys.add(SysParam.PN);
        keys.add(SysParam.TUSN);
        keys.add(SysParam.DEVICE_CODE);
        keys.add(SysParam.DEVICE_MODEL);
        keys.add(SysParam.RESERVED);
        keys.add(SysParam.PCD_PARAM_A);
        keys.add(SysParam.PCD_PARAM_B);
        keys.add(SysParam.PCD_PARAM_C);
        keys.add(SysParam.TUSN_KEY_KCV);
        keys.add(SysParam.PCD_IFM_VERSION);
        keys.add(SysParam.SAM_COUNT);
        keys.add(SysParam.SEC_MODE);
        keys.add(SysParam.SM_TYPE);
        keys.add(SysParam.PUSH_CFG_FILE);
        keys.add(SysParam.FLASH_SIZE);
        keys.add(SysParam.CARD_HW);
        keys.add(SysParam.NFC_FW_VER);
        keys.add(SysParam.IFM_LIB_VERSION);
        keys.add(SysParam.MSR_VERSION);
        keys.add(SysParam.POSAPI_VERSION);
        keys.add(SysParam.PCI_PTS_VERSION);
        keys.add(SysParam.RNIB_VERSION);
        keys.add(SysParam.RTC_BAT_VOL_DET);
        keys.add(SysParam.DEV_FINGER_STATUS);
        keys.add(SysParam.SELF_CHECK_STATUS);
        keys.add(SysParam.RAS_STANDARD_ALGORITHM);
        keys.add(SysParam.TAMPER_SWITCH);
        keys.add(SysParam.SRED);
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            sb.append(getDisplayKey(key));
            sb.append(":");
            sb.append(getSysParamInner(key));
            sb.append("\n");
        }
        sb.deleteCharAt(sb.length() - 1);
        addTextViewText(sb, tvInfo, false);
    }

    /** Get IC/NFC/MAG/SAM card status (Normal1 or Abnormal) */
    private void getDeviceCardStatus() {
        try {
            //Bit0-IC card functionality,  0-Functionality normal, 1-Functionality abnormal
            //Bit1-SAM card functionality, 0-Functionality normal, 1-Functionality abnormal
            //Bit2-NFC card functionality, 0-Functionality normal, 1-Functionality abnormal
            //Bit3-MagStripe card functionality, 0-Functionality normal, 1-Functionality abnormal
            //bit4~7-Reserved, value is 0
            //(Only supported on toss device)
            StringBuilder sb = new StringBuilder();
            String value = MyApplication.app.basicOptV2.getSysParam(SysParam.CARD_HW);
            if (TextUtils.isEmpty(value) || !TextUtils.isDigitsOnly(value)) {
                String unknown = getString(R.string.other_version_known);
                sb.append(getString(R.string.basic_ic_status)).append(unknown).append("\n");
                sb.append(getString(R.string.basic_sam_status)).append(unknown).append("\n");
                sb.append(getString(R.string.basic_nfc_status)).append(unknown).append("\n");
                sb.append(getString(R.string.basic_mag_status)).append(unknown).append("\n");
            } else {
                int intValue = Integer.parseInt(value);
                //Bit0：IC card status, 0-normal, 1-error
                sb.append(getString(R.string.basic_ic_status));
                sb.append((intValue & 0x01) == 0 ? getString(R.string.basic_card_status_normal) : getString(R.string.basic_card_status_error));
                sb.append("\n");
                //Bit1：SAM card status, 0-normal, 1-error
                sb.append(getString(R.string.basic_sam_status));
                sb.append((intValue & 0x02) == 0 ? getString(R.string.basic_card_status_normal) : getString(R.string.basic_card_status_error));
                sb.append("\n");
                //Bit2：NFC card status, 0-normal, 1-error
                sb.append(getString(R.string.basic_nfc_status));
                sb.append((intValue & 0x04) == 0 ? getString(R.string.basic_card_status_normal) : getString(R.string.basic_card_status_error));
                sb.append("\n");
                //Bit3：MagStripe card status, 0-normal, 1-error
                sb.append(getString(R.string.basic_mag_status));
                sb.append((intValue & 0x08) == 0 ? getString(R.string.basic_card_status_normal) : getString(R.string.basic_card_status_error));
                sb.append("\n");
            }
            sb.deleteCharAt(sb.length() - 1);
            addTextViewText(sb, tvInfo, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Get the count of device supported SAM card slots */
    private void getDeviceSAMCount() {
        try {
            //“00”: not support SAM card slot
            //“01”: support 1 SAM card slots
            //“02”: support 2 SAM cards slots
            //“03”: support 3 SAM card slots
            //(使用1902安全芯片的机型不支持)
            StringBuilder sb = new StringBuilder();
            String value = MyApplication.app.basicOptV2.getSysParam(SysParam.SAM_COUNT);
            if (TextUtils.isEmpty(value) || !TextUtils.isDigitsOnly(value)) {
                sb.append(getString(R.string.basic_sam_count)).append(getString(R.string.other_version_known)).append("\n");
            } else {
                int intValue = Integer.parseInt(value);
                sb.append(getString(R.string.basic_sam_count));
                sb.append(intValue);
                sb.append("\n");
            }
            sb.deleteCharAt(sb.length() - 1);
            addTextViewText(sb, tvInfo, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Get device nfc chip type */
    private void getDeviceNFCConfig() {
        try {
            //nfc chip type:
            //“00”-no nfc chip
            //“01”-nfc chip type is RC531
            //“02”-nfc chip type is PN512
            //“03”-nfc chip type is RC663
            //“04”-nfc chip type is AS3911
            //“06”-nfc chip type is MH1608C
            //“07”-nfc chip type is PN5190
            //“08”-nfc chip type is ST3916
            //“09”-nfc chip type is ST3917
            //“10”-nfc chip type is FM17660
            //“11”-nfc chip type is FM17660K
            //(支持所有机型)
            StringBuilder sb = new StringBuilder();
            String value = MyApplication.app.basicOptV2.getSysParam(SysParam.NFC_CONFIG);
            if (TextUtils.isEmpty(value) || !TextUtils.isDigitsOnly(value)) {
                sb.append(getString(R.string.basic_nfc_config)).append(getString(R.string.other_version_known)).append("\n");
            } else {
                int intValue = Integer.parseInt(value);
                sb.append(getString(R.string.basic_nfc_config));
                switch (intValue) {
                    case 0:
                        sb.append("--");
                        break;
                    case 1:
                        sb.append("RC531");
                        break;
                    case 2:
                        sb.append("PN512");
                        break;
                    case 3:
                        sb.append("RC663");
                        break;
                    case 4:
                        sb.append("AS3911");
                        break;
                    case 6:
                        sb.append("MH1608C");
                        break;
                    case 7:
                        sb.append("PN5190");
                        break;
                    case 8:
                        sb.append("ST3916");
                        break;
                    case 9:
                        sb.append("ST3917");
                        break;
                    case 10:
                        sb.append("FM17660");
                        break;
                    case 11:
                        sb.append("FM17660K");
                        break;
                }
                sb.append("\n");
                sb.deleteCharAt(sb.length() - 1);
                addTextViewText(sb, tvInfo, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Get EMV kernel version */
    private void getEmvKernelVersion() {
        List<String> keys = new ArrayList<>();
        keys.add(SysParam.APEMV_VERSION);
        keys.add(SysParam.EMV_VERSION);
        keys.add(SysParam.PAYPASS_VERSION);
        keys.add(SysParam.PAYWAVE_VERSION);
        keys.add(SysParam.QPBOC_VERSION);
        keys.add(SysParam.ENTRY_VERSION);
        keys.add(SysParam.MIR_VERSION);
        keys.add(SysParam.JCB_VERSION);
        keys.add(SysParam.PAGO_VERSION);
        keys.add(SysParam.PURE_VERSION);
        keys.add(SysParam.AE_VERSION);
        keys.add(SysParam.FLASH_VERSION);
        keys.add(SysParam.DPAS_VERSION);
        keys.add(SysParam.EMVBASE_VERSION);
        keys.add(SysParam.KD_VERSION);
        keys.add(SysParam.EFTPOS_VERSION);
        keys.add(SysParam.RUPAY_VERSION);
        keys.add(SysParam.SAMSUNGPAY_VERSION);
        keys.add(SysParam.CPACE_VERSION);
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            String value = getSysParamInner(key);
            if (!TextUtils.isEmpty(value)) {
                sb.append(value);
                sb.append("\n");
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        addTextViewText(sb, tvInfo, false);
    }

    /** Get emv kernel release date */
    private void getEmvKernelReleaseDate() {
        List<String> keys = new ArrayList<>();
        keys.add(SysParam.EMV_RELEASE_DATE);
        keys.add(SysParam.PAYPASS_RELEASE_DATE);
        keys.add(SysParam.PAYWAVE_RELEASE_DATE);
        keys.add(SysParam.QPBOC_RELEASE_DATE);
        keys.add(SysParam.ENTRY_RELEASE_DATE);
        keys.add(SysParam.MIR_RELEASE_DATE);
        keys.add(SysParam.JCB_RELEASE_DATE);
        keys.add(SysParam.PAGO_RELEASE_DATE);
        keys.add(SysParam.PURE_RELEASE_DATE);
        keys.add(SysParam.AE_RELEASE_DATE);
        keys.add(SysParam.FLASH_RELEASE_DATE);
        keys.add(SysParam.DPAS_RELEASE_DATE);
        keys.add(SysParam.EMVBASE_RELEASE_DATE);
        keys.add(SysParam.KD_RELEASE_DATE);
        keys.add(SysParam.EFTPOS_RELEASE_DATE);
        keys.add(SysParam.RUPAY_RELEASE_DATE);
        keys.add(SysParam.SAMSUNGPAY_RELEASE_DATE);
        keys.add(SysParam.CPACE_RELEASE_DATE);
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            String value = getSysParamInner(key);
            if (!TextUtils.isEmpty(value)) {
                sb.append(value);
                sb.append("\n");
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        addTextViewText(sb, tvInfo, false);
    }

    /** 获取显示的key */
    private String getDisplayKey(String key) {
        if (SysParam.SAM_COUNT.equals(key)) {
            return getString(R.string.basic_sam_count);
        }
        return key;
    }

    /** Get system param for SPHS */
    private String getSysParamInner(String key) {
        String value = null;
        try {
            addStartTime("getSysParam() key=" + key);
            value = MyApplication.app.basicOptV2.getSysParam(key);
            addEndTime("getSysParam() key=" + key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

}
