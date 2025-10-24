package com.sm.sdk.demo.emv;

import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;
import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.Utility;
import com.sunmi.pay.hardware.aidl.AidlConstants;
import com.sunmi.pay.hardware.aidl.AidlConstants.EMV;
import com.sunmi.pay.hardware.aidl.AidlConstants.Security;

public class SredActivity extends BaseAppCompatActivity {
    private TextInputLayout input1Lay;
    private TextInputLayout input2Lay;
    private EditText edtInput1;
    private EditText edtInput2;
    private TextView tvSred;
    private TextView tvAccSecData;
    private int keySystem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emv_sred);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.emv_sred_test);
        findViewById(R.id.mb_get_sred).setOnClickListener(this);
        findViewById(R.id.mb_disable_sred_by_sys_param).setOnClickListener(this);
        findViewById(R.id.mb_disable_sred_by_set_account_sec_param).setOnClickListener(this);
        input1Lay = findViewById(R.id.edt_key_input_1_lay);
        edtInput1 = findViewById(R.id.edt_key_input_1);
        input2Lay = findViewById(R.id.edt_key_input_2_lay);
        edtInput2 = findViewById(R.id.edt_key_input_2);

        RadioGroup rdgSet = findViewById(R.id.rdg_adsp);
        rdgSet.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.rdo_mksk:
                    input1Lay.setHint("keyValue");
                    edtInput1.setHint("keyValue");
                    edtInput1.setText("A8164307793B0B1CC27F68FEBCF2DF5B");
                    input2Lay.setVisibility(View.GONE);
                    keySystem = Security.SEC_MKSK;
                    break;
                case R.id.rdo_dukpt:
                    input1Lay.setHint("keyValue");
                    edtInput1.setHint("keyValue");
                    input2Lay.setHint("ksn");
                    edtInput2.setHint("ksn");
                    edtInput1.setText("6AC292FAA1315B4D858AB3A3D7D5933A");
                    edtInput2.setText("FFFF9876543210E00000");
                    input2Lay.setVisibility(View.VISIBLE);
                    keySystem = Security.SEC_DUKPT;
                    break;
                case R.id.rdo_rsa:
                    input1Lay.setHint("pubKey modulus");
                    edtInput1.setHint("pubKey modulus");
                    input2Lay.setHint("pubKey exponent");
                    edtInput2.setHint("pubKey exponent");
                    edtInput1.setText("B859D678065F2A6B7575FF174158083F50F6ED8993297B26161C19E881A8B3D209731385D29CD98D960C274DF8A4CC7BFE96A170395B1136CDB8E53CCEFED5A5590A7ED9E26CBC6C9E8DE656BC90F6E83CE49A5DC565C24C8800E1A034973B5EDDCF5A40C029871DA32B4E5AAA58A8DEDA18CAB3416E3BE91C77C5E864BAC2E7E28CED41CE6DFBC2538688B69FA1E757038A8E1948234E172EA800DB900AC7D4B1425E88CBF44B6B06B826C9CE3A07F856715130222D91C81D7AFB357E0A5A404529D2CBE288532FDCE3784BC31F5BE71DC21286B2C476353617E5FF1BF96A860020EFE0E7D0F74776C3348F21D6F037EED5927BCC9D91FB0480E172F65E908D");
                    edtInput2.setText("010001");
                    input2Lay.setVisibility(View.VISIBLE);
                    keySystem = Security.SEC_RSA_KEY;
                    break;
            }
        });
        rdgSet.check(R.id.rdo_mksk);
        findViewById(R.id.mb_set_adsp).setOnClickListener(this);
        findViewById(R.id.mb_get_asd).setOnClickListener(this);
        tvSred = findViewById(R.id.tv_get_sred);
        tvAccSecData = findViewById(R.id.tv_get_asd);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mb_get_sred:
                getSred();
                break;
            case R.id.mb_disable_sred_by_sys_param:
                disableSredBySysParam();
                break;
            case R.id.mb_disable_sred_by_set_account_sec_param:
                disableSredBySetAccountDataSecParam();
                break;
            case R.id.mb_set_adsp:
                if (keySystem == Security.SEC_MKSK) {
                    setAccountDataSecParamWithMksk();
                } else if (keySystem == Security.SEC_DUKPT) {
                    setAccountDataSecParamWithDukpt();
                } else if (keySystem == Security.SEC_RSA_KEY) {
                    setAccountDataSecParamWithRsa();
                }
                break;
            case R.id.mb_get_asd:
                getAccountSecParam();
                break;
        }
    }

    private void getSred() {
        try {
            String status = MyApplication.app.basicOptV2.getSysParam(AidlConstants.SysParam.SRED);
            LogUtil.e(TAG, "get sred, value:" + status);
            tvSred.setText("sred value:" + status);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** Disable sred by set sys param */
    private void disableSredBySysParam() {
        try {
            int code = MyApplication.app.basicOptV2.setSysParam(AidlConstants.SysParam.SRED, "0");
            LogUtil.e(TAG, "close sred, code:" + code);
            showToast("close sred " + Utility.getStateString(code));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** Disable sred by set account sec param */
    private void disableSredBySetAccountDataSecParam() {
        try {
            Bundle bundle = new Bundle();
            bundle.putBoolean("sred", false);
            int code = MyApplication.app.emvOptV2.setAccountDataSecParam(bundle);
            LogUtil.e(TAG, "close sred by setAccountDataSecParam(), code:" + code);
            showToast("close sred " + Utility.getStateString(code));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set account sec param with MKSK key
     * <li>Call this method with not set key "sred" or set value of key "sred" as true will enable sred</li>
     * <li>Call this method with set value of key "sred" as false will disable sred</li>
     * <li>If sred is disabled, call method getAccountSecParam() will return -50035: sred not enabled</li>
     */
    private void setAccountDataSecParamWithMksk() {
        try {
            String keyStr = edtInput1.getText().toString();
            if (TextUtils.isEmpty(keyStr)) {
                showToast("keyValue should not be empty");
                edtInput1.requestFocus();
                return;
            }
            if (!Utility.checkHexValue(keyStr)) {
                showToast("keyValue should be Hex string");
                edtInput1.requestFocus();
                return;
            }
            final int KEY_INDEX_MKSK = 1;
            //1.save mksk key
            byte[] mkskKey = ByteUtil.hexStr2Bytes(keyStr);
            int code = MyApplication.app.securityOptV2.savePlaintextKey(Security.KEY_TYPE_TDK, mkskKey, null, Security.KEY_ALG_TYPE_3DES, KEY_INDEX_MKSK);
            Log.e(TAG, "savePlaintextKey(), keyIndex:" + KEY_INDEX_MKSK + ",code:" + code);
            //2.set account sec data with mksk
            Bundle pIn = new Bundle();
            pIn.putInt("encKeySystem", Security.SEC_MKSK);
            pIn.putInt("encKeyIndex", KEY_INDEX_MKSK);
            pIn.putInt("encMode", Security.DATA_MODE_CBC);
            pIn.putByteArray("encIv", new byte[16]);
            pIn.putString("panAppendContent", "01696069");
            pIn.putInt("panAppendMode", 0);
            code = MyApplication.app.emvOptV2.setAccountDataSecParam(pIn);
            String msg = "setAccountDataSecParam() with mksk, code:" + code;
            showToast(msg);
            Log.e(TAG, msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Set account sec param with dukpt key
     * <li>Call this method with not set key "sred" or set value of key "sred" as true will enable sred</li>
     * <li>Call this method with set value of key "sred" as false will disable sred</li>
     * <li>If sred is disabled, call method getAccountSecParam() will return -50035: sred not enabled</li>
     */
    private void setAccountDataSecParamWithDukpt() {
        try {
            String keyStr = edtInput1.getText().toString();
            if (TextUtils.isEmpty(keyStr)) {
                showToast("keyValue should not be empty");
                edtInput1.requestFocus();
                return;
            }
            if (!Utility.checkHexValue(keyStr)) {
                showToast("keyValue should be Hex string");
                edtInput1.requestFocus();
                return;
            }
            String ksnStr = edtInput2.getText().toString();
            if (TextUtils.isEmpty(ksnStr)) {
                showToast("ksn should not be empty");
                edtInput2.requestFocus();
                return;
            }
            if (!Utility.checkHexValue(ksnStr)) {
                showToast("ksn should be Hex string");
                edtInput2.requestFocus();
                return;
            }
            final int KEY_INDEX_DUKPT = 2;
            //1.save dukpt key
            byte[] dukptKey = ByteUtil.hexStr2Bytes(keyStr);
            byte[] ksn = ByteUtil.hexStr2Bytes(ksnStr);
            int code = MyApplication.app.securityOptV2.saveKeyDukpt(Security.KEY_TYPE_DUPKT_IPEK, dukptKey, null, ksn, Security.KEY_ALG_TYPE_3DES, KEY_INDEX_DUKPT);
            Log.e(TAG, "saveKeyDukpt(), keyIndex:" + KEY_INDEX_DUKPT + ",code:" + code);
            //2.set account sec data with dukpt
            Bundle pIn = new Bundle();
            pIn.putInt("encKeySystem", Security.SEC_DUKPT);
            pIn.putInt("encKeyIndex", KEY_INDEX_DUKPT);
            pIn.putInt("encMode", Security.DATA_MODE_CBC);
            pIn.putByteArray("encIv", new byte[16]);
            pIn.putString("panAppendContent", "01696069");
            pIn.putInt("panAppendMode", 0);
            code = MyApplication.app.emvOptV2.setAccountDataSecParam(pIn);
            String msg = "setAccountDataSecParam() with dukpt,code:" + code;
            showToast(msg);
            Log.e(TAG, msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Set account sec param with RSA key
     * <li>Call this method with not set key "sred" or set value of key "sred" as true will enable sred</li>
     * <li>Call this method with set value of key "sred" as false will disable sred</li>
     * <li>If sred is disabled, call method getAccountSecParam() will return -50035: sred not enabled</li>
     */
    private void setAccountDataSecParamWithRsa() {
        try {
            String modulusStr = edtInput1.getText().toString();
            if (TextUtils.isEmpty(modulusStr)) {
                showToast("modulus should not be empty");
                edtInput1.requestFocus();
                return;
            }
            if (!Utility.checkHexValue(modulusStr)) {
                showToast("modulus should be Hex string");
                edtInput1.requestFocus();
                return;
            }
            String exponentStr = edtInput2.getText().toString();
            if (TextUtils.isEmpty(exponentStr)) {
                showToast("exponent should not be empty");
                edtInput2.requestFocus();
                return;
            }
            if (!Utility.checkHexValue(exponentStr)) {
                showToast("exponent should be Hex string");
                edtInput2.requestFocus();
                return;
            }
            final int KEY_INDEX_RSA = 3;
            //1. save RSA key
            final String modulus = modulusStr;
            final String exponent = exponentStr;
            final int KEY_SIZE = 2048;
            int code = MyApplication.app.securityOptV2.injectRSAKey(KEY_INDEX_RSA, KEY_SIZE, modulus, exponent);
            Log.e(TAG, "injectRSAKey(), keyIndex:" + KEY_INDEX_RSA + ",code:" + code);

            //2.set account sec data with rsa
            Bundle pIn = new Bundle();
            pIn.putInt("encKeySystem", Security.SEC_RSA_KEY);
            pIn.putInt("encKeyIndex", KEY_INDEX_RSA);
            pIn.putInt("encMode", Security.DATA_MODE_CBC);
            pIn.putByteArray("encIv", new byte[16]);
            pIn.putByte("encPaddingMode", (byte) Security.PADDING_OAEP_SHA1);
            pIn.putString("panAppendContent", "01696069");
            pIn.putInt("panAppendMode", 0);
            code = MyApplication.app.emvOptV2.setAccountDataSecParam(pIn);
            String msg = "setAccountDataSecParam() with rsa,code:" + code;
            showToast(msg);
            Log.e(TAG, msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** After enable sred, call this method to get ciphertext account sensitive data */
    private void getAccountSecParam() {
        try {
            tvAccSecData.setText(null);
            //2.get account sec data with dukpt
            Bundle pOut = new Bundle();
            String[] tags = {"57", "5A", "5F24", "5F34"};
            int code = MyApplication.app.emvOptV2.getAccountSecData(EMV.TLVOpCode.OP_NORMAL, tags, pOut);
            String msg = "getAccountSecData(), code:" + code + ", out:" + Utility.bundle2String(pOut);
            addTextViewText(msg, tvAccSecData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
