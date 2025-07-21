package com.sm.sdk.demo.other;

import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.SystemPropertiesUtil;
import com.sunmi.pay.hardware.aidl.AidlConstants.SysParam;

public class VersionActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_version);
        initToolbarBringBack(R.string.version);
        initView();
    }

    private void initView() {
        TextView tvInfo = findViewById(R.id.tv_info);
        try {
            addStartTimeWithClear("getSysParam() total");
            StringBuilder sb = new StringBuilder();
            sb.append(getString(R.string.other_version_device)).append(getSysParam(SysParam.DEVICE_MODEL)).append("\n");
            sb.append(getString(R.string.other_version_rom)).append(getRomVersion()).append("\n");
            sb.append(getString(R.string.other_version_sn)).append(getSysParam(SysParam.SN)).append("\n");
            sb.append(getString(R.string.other_version_demo)).append(getApkVersion("com.sm.sdk.demo")).append("\n");
            sb.append(getString(R.string.other_version_service)).append(getApkVersion("com.sunmi.pay.hardware_v3")).append("\n");
            sb.append(getString(R.string.other_version_rnib)).append(getSysParam(SysParam.RNIB_VERSION)).append("\n");
            addEndTime("getSysParam() total");
            tvInfo.setText(sb);
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getSysParam(String key) {
        String result = null;
        String unknown = getString(R.string.other_version_known);
        try {
            result = MyApplication.app.basicOptV2.getSysParam(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return TextUtils.isEmpty(result) ? unknown : result;
    }

    private String getApkVersion(String pkgName) {
        String result = null;
        String unknown = getString(R.string.other_version_known);
        try {
            PackageInfo pkgInfo = getPackageManager().getPackageInfo(pkgName, 0);
            result = pkgInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return TextUtils.isEmpty(result) ? unknown : result;
    }

    private String getRomVersion() {
        String result = SystemPropertiesUtil.get("ro.version.sunmi_versionname");
        String unknown = getString(R.string.other_version_known);
        return TextUtils.isEmpty(result) ? unknown : result;
    }

}
