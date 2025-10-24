package com.sm.sdk.demo.basic;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.NumberUtil;

public class CardWaterMarkActivity extends BaseAppCompatActivity {
    private int orientation = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_card_watermark);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.basic_watermark_test);
        RadioGroup group = findViewById(R.id.rg_rotation);
        group.setOnCheckedChangeListener((group1, checkedId) -> {
            switch (checkedId) {
                case R.id.rb_current:
                    orientation = -1;
                    break;
                case R.id.rb_orientation_0:
                    orientation = 0;
                    break;
                case R.id.rb_orientation_90:
                    orientation = 1;
                    break;
                case R.id.rb_orientation_180:
                    orientation = 2;
                    break;
                case R.id.rb_orientation_270:
                    orientation = 3;
                    break;
            }
        });
        findViewById(R.id.btn_set_alpha).setOnClickListener(this);
        findViewById(R.id.btn_get_alpha).setOnClickListener(this);
        findViewById(R.id.btn_get_location).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_set_alpha:
                setWatermarkAlpha();
                break;
            case R.id.btn_get_alpha:
                getWatermarkAlpha();
                break;
            case R.id.btn_get_location:
                getWatermarkLocation();
                break;
        }
    }

    private void setWatermarkAlpha() {
        try {
            String indexStr = this.<EditText>findViewById(R.id.edt_get_alpha_index).getText().toString();
            String alphaStr = this.<EditText>findViewById(R.id.edt_set_alpha_value).getText().toString();
            int index = NumberUtil.parseInt(indexStr, -1);
            if (index < 0) {
                showToast("watermark index should be greater than 0");
                return;
            }
            float alpha = NumberUtil.parseFloat(alphaStr, -1.0f);
            if (alpha < 0.0f || alpha > 1.0f) {
                showToast("watermark alpha should be in [0.0f,1.0f]");
                return;
            }
            int code = MyApplication.app.basicOptV2.setCardWaterMarkAlpha(index, alpha);
            String msg = "setCardWaterMarkAlpha()->code:" + code;
            LogUtil.e(TAG, msg);
            showToast(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getWatermarkAlpha() {
        try {
            String indexStr = this.<EditText>findViewById(R.id.edt_get_alpha_index).getText().toString();
            int index = NumberUtil.parseInt(indexStr, -1);
            if (index < 0) {
                showToast("watermark index should be greater than 0");
                return;
            }
            float alpha = MyApplication.app.basicOptV2.getCardWaterMarkAlpha(index);
            LogUtil.e(TAG, "setCardWaterMarkAlpha()->value:" + alpha);
            if (alpha >= 0) {
                TextView tv = findViewById(R.id.tv_get_alpha_result);
                String msg = "alpha:" + alpha;
                tv.setText(msg);
                LogUtil.e(TAG, "getCardWaterMarkAlpha()->" + msg);
            } else {
                showToast("getCardWaterMarkAlpha() failed, code:" + (int) alpha);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getWatermarkLocation() {
        try {
            String indexStr = this.<EditText>findViewById(R.id.edt_get_location_index).getText().toString();
            int index = NumberUtil.parseInt(indexStr, -1);
            if (index < 0) {
                showToast("watermark index should be greater than 0");
                return;
            }
            Bundle bundle = new Bundle();
            int code = MyApplication.app.basicOptV2.getCardWaterMarkLocation(index, orientation, bundle);
            LogUtil.e(TAG, "getCardWaterMarkLocation()->code:" + code);
            if (code == 0) {
                int posX = bundle.getInt("posX");
                int posY = bundle.getInt("posY");
                int width = bundle.getInt("width");
                int height = bundle.getInt("height");
                TextView tv = findViewById(R.id.tv_get_location_result);
                String msg = "posX:" + posX + ", posY:" + posY + ", width:" + width + ", height:" + height;
                tv.setText(msg);
                LogUtil.e(TAG, "getCardWaterMarkLocation()->" + msg);
            } else {
                showToast("getCardWaterMarkLocation() failed, code:" + code);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
