package com.sm.sdk.demo.print;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.LogUtil;

public class PrintDotSpeedLevelActivity extends BaseAppCompatActivity {
    private int speedLevel = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_dot_speed);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.print_dot_speed_level);
        RadioGroup group = findViewById(R.id.rdg_dot_speed_level);
        group.setOnCheckedChangeListener((group1, checkedId) -> {
            switch (checkedId) {
                case R.id.rdo_low:
                    speedLevel = 1;
                    break;
                case R.id.rdo_middle:
                    speedLevel = 2;
                    break;
                case R.id.rdo_high:
                    speedLevel = 3;
                    break;
            }
        });
        group.check(R.id.rdo_low);
        findViewById(R.id.btn_set_dot_speed_level).setOnClickListener(this);
        findViewById(R.id.btn_get_dot_speed_level).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_set_dot_speed_level:
                setPrintDotSpeedLevel();
                break;
            case R.id.btn_get_dot_speed_level:
                getPrintDotSpeedLevel();
                break;
        }
    }

    private void setPrintDotSpeedLevel() {
        try {
            int code = MyApplication.app.printerOptV2.setPrintDotSpeedLevel(speedLevel);
            String msg = "setPrintDotSpeedLevel()->code:" + code;
            LogUtil.e(TAG, msg);
            showToast(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getPrintDotSpeedLevel() {
        try {
            int level = MyApplication.app.printerOptV2.getPrintDotSpeedLevel();
            LogUtil.e(TAG, "getPrintDotSpeedLevel()->code:" + level);
            String msg = null;
            if (level < 0) {
                msg = "getPrintDotSpeedLevel()->failed, code:" + level;
            } else {
                msg = "getPrintDotSpeedLevel()->success, level:" + getLevelString(level);
            }
            LogUtil.e(TAG, msg);
            TextView tvInfo = findViewById(R.id.tv_info);
            tvInfo.setText(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getLevelString(int level) {
        switch (level) {
            case 1:
                return "Low(1)";
            case 2:
                return "Middle(2)";
            case 3:
                return "High(3)";
            default:
                return "unknown";
        }
    }
}
