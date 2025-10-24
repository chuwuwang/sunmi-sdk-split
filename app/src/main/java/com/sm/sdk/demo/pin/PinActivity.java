package com.sm.sdk.demo.pin;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.R;

public class PinActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((R.layout.activity_pin));
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.pin_pad);
        View view = findViewById(R.id.pinpad_whole);
        TextView leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.pin_pad_whole);

        view = findViewById(R.id.customized_pinpad);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.pin_pad_customized_keyboard);

        view = findViewById(R.id.customized_pinpad_for_banjul);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.pin_pad_customized_keyboard_for_banjul);

        view = findViewById(R.id.customized_pinpad_for_qicard);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.pin_pad_customized_keyboard_for_qicard);

        view = findViewById(R.id.customized_pinpad_for_clip);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.pin_pad_customized_keyboard_for_clip);

        view = findViewById(R.id.customized_pinpad_for_hobex);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.pin_pad_customized_keyboard_for_hobex);

        view = findViewById(R.id.customized_vi_pinpad);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.pin_pad_customized_vi_keyboard);

        view = findViewById(R.id.customized_vi_pinpad_for_softbank);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.pin_pad_customized_vi_keyboard_for_softbank);

        view = findViewById(R.id.start_input_pin);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.pin_pad_start_input_pin);

        view = findViewById(R.id.vi_pinpad);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.pin_pad_vi_keybaoard);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.pinpad_whole:
                openActivity(PinPadActivity.class);
                break;
            case R.id.customized_pinpad:
                openActivity(CustomizedPinPadActivity.class);
                break;
            case R.id.customized_pinpad_for_banjul:
                openActivity(CustomizedPinPadActivityForBanjul.class);
                break;
            case R.id.customized_pinpad_for_qicard:
                openActivity(CustomizedPinPadActivityForQiCard.class);
                break;
            case R.id.customized_pinpad_for_clip:
                openActivity(CustomizedPinPadActivityForClip.class);
                break;
            case R.id.customized_pinpad_for_hobex:
                openActivity(CustomizedPinPadActivityForHobex.class);
                break;
            case R.id.customized_vi_pinpad:
                openActivity(CustomizedVisualImpairmentPinActivity.class);
                break;
            case R.id.customized_vi_pinpad_for_softbank:
                openActivity(CustomizedVisualImpairmentPinActivityForBankSoft.class);
                break;
            case R.id.start_input_pin:
                openActivity(StartInputPinActivity.class);
                break;
            case R.id.vi_pinpad:
                openActivity(VisualImpairmentPinActivity.class);
                break;
        }
    }
}
