package com.sm.sdk.demo.security;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.R;

public class NlkTestActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_nlk_test);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.security_nlk_test);
        View view = findViewById(R.id.nlk_save_mksk_key);
        TextView leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.security_nlk_save_mksk);

        view = findViewById(R.id.nlk_get_kcv);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.security_nlk_get_kcv);

        view = findViewById(R.id.nlk_mksk_data_encrypt);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.security_nlk_mksk_data_enc);

        view = findViewById(R.id.nlk_mksk_data_decrypt);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.security_nlk_mksk_data_dec);

        view = findViewById(R.id.nlk_delete_key);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.security_nlk_delete_key);

        view = findViewById(R.id.nlk_rsa_test);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.security_nlk_rsa_test);

        view = findViewById(R.id.nlk_rsa_recover);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.security_nlk_rsa_recover);

        view = findViewById(R.id.nlk_ecc_test);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.security_nlk_ecc_test);

        view = findViewById(R.id.nlk_ecc_recover);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.security_nlk_ecc_recover);

        view = findViewById(R.id.nlk_cert_test);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.security_nlk_cert_test);

        view = findViewById(R.id.nlk_sm2_test);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.security_nlk_sm2_test);

        view = findViewById(R.id.nlk_sm2_recover);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.security_nlk_sm2_recover);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.nlk_save_mksk_key:
                openActivity(NlkSaveKeyActivity.class);
                break;
            case R.id.nlk_get_kcv:
                openActivity(NlkGetKcvActivity.class);
                break;
            case R.id.nlk_mksk_data_encrypt:
                openActivity(NlkDataEncryptActivity.class);
                break;
            case R.id.nlk_mksk_data_decrypt:
                openActivity(NlkDataDecryptActivity.class);
                break;
            case R.id.nlk_delete_key:
                openActivity(NlkDeleteKeyActivity.class);
                break;
            case R.id.nlk_rsa_test:
                openActivity(NlkRsaTestActivity.class);
                break;
            case R.id.nlk_rsa_recover:
                openActivity(NlkRsaRecoverActivity.class);
                break;
            case R.id.nlk_ecc_test:
                openActivity(NlkEccTestActivity.class);
                break;
            case R.id.nlk_ecc_recover:
                openActivity(NlkEccRecoverActivity.class);
                break;
            case R.id.nlk_cert_test:
                openActivity(NlkCertTestActivity.class);
                break;
            case R.id.nlk_sm2_test:
                openActivity(NlkSm2TestActivity.class);
                break;
            case R.id.nlk_sm2_recover:
                openActivity(NlkSm2RecoverActivity.class);
                break;
        }
    }
}
