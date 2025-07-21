package com.sm.sdk.demo.emv;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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
    private TextView tvSred;
    private TextView tvAccSecDataMksk;
    private TextView tvAccSecDataDukpt;
    private TextView tvAccSecDataRsa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sred);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.emv_sred_test);
        findViewById(R.id.mb_get_sred).setOnClickListener(this);
        findViewById(R.id.mb_close_sred_by_sys_param).setOnClickListener(this);
        findViewById(R.id.mb_close_sred_by_setAccountDataSecParam).setOnClickListener(this);
        findViewById(R.id.mb_account_sec_data_mksk).setOnClickListener(this);
        findViewById(R.id.mb_account_sec_data_dukpt).setOnClickListener(this);
        findViewById(R.id.mb_account_sec_data_rsa).setOnClickListener(this);
        tvSred = findViewById(R.id.tv_get_sred);
        tvAccSecDataMksk = findViewById(R.id.tv_account_sec_data_mksk);
        tvAccSecDataDukpt = findViewById(R.id.tv_account_sec_data_dukpt);
        tvAccSecDataRsa = findViewById(R.id.tv_account_sec_data_rsa);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mb_get_sred:
                getSred();
                break;
            case R.id.mb_close_sred_by_sys_param:
                closeSredBySysParam();
                break;
            case R.id.mb_close_sred_by_setAccountDataSecParam:
                closeSredBySetAccountDataSecParam();
                break;
            case R.id.mb_account_sec_data_mksk:
                testAccountSecDataParamWithMksk();
                break;
            case R.id.mb_account_sec_data_dukpt:
                testAccountSecDataParamWithDukpt();
                break;
            case R.id.mb_account_sec_data_rsa:
                testAccountSecDataParamWithRSA();
                break;
        }
    }

    private void getSred() {
        try {
            String status = MyApplication.app.basicOptV2.getSysParam("sred");
            LogUtil.e(TAG, "get sred, value:" + status);
            tvSred.setText("sred value:" + status);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void closeSredBySysParam() {
        try {
            int code = MyApplication.app.basicOptV2.setSysParam("sred", "0");
            LogUtil.e(TAG, "close sred, code:" + code);
            showToast("close sred " + Utility.getStateString(code));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void closeSredBySetAccountDataSecParam() {
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

    private void testAccountSecDataParamWithMksk() {
        try {
            tvAccSecDataMksk.setText(null);
            final int KEY_INDEX_MKSK = 1;

            //1.save mksk key
            byte[] mkskKey = ByteUtil.hexStr2Bytes("A8164307793B0B1CC27F68FEBCF2DF5B");
            int code = MyApplication.app.securityOptV2.savePlaintextKey(Security.KEY_TYPE_TDK, mkskKey, null, Security.KEY_ALG_TYPE_3DES, KEY_INDEX_MKSK);
            Log.e(TAG, "savePlaintextKey(), keyIndex:" + KEY_INDEX_MKSK + ",code:" + code);

            //2.get account sec data with mksk
            Bundle pIn = new Bundle();
            Bundle pOut = new Bundle();
            String[] tags = {"57", "5A", "5F24", "5F34"};
            pIn.putInt("encKeySystem", Security.SEC_MKSK);
            pIn.putInt("encKeyIndex", KEY_INDEX_MKSK);
            pIn.putInt("encMode", Security.DATA_MODE_CBC);
            pIn.putByteArray("encIv", new byte[16]);
            pIn.putString("panAppendContent", "01696069");
            pIn.putString("panAppendMode", "TID+PAN");
            code = MyApplication.app.emvOptV2.setAccountDataSecParam(pIn);
            String msg = "setAccountDataSecParam() with mksk, code:" + code;
            addTextViewText(tvAccSecDataMksk, msg);
            Log.e(TAG, msg);
            code = MyApplication.app.emvOptV2.getAccountSecData(EMV.TLVOpCode.OP_NORMAL, tags, pOut);
            msg = "getAccountSecData() with mksk Key, code:" + code + ", out:" + Utility.bundle2String(pOut);
            addTextViewText(tvAccSecDataMksk, msg);
            Log.e(TAG, msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void testAccountSecDataParamWithDukpt() {
        try {
            tvAccSecDataDukpt.setText(null);
            final int KEY_INDEX_DUKPT = 2;

            //1.save dukpt key
            byte[] dukptKey = ByteUtil.hexStr2Bytes("6AC292FAA1315B4D858AB3A3D7D5933A");
            byte[] ksn = ByteUtil.hexStr2Bytes("FFFF9876543210E00000");
            int code = MyApplication.app.securityOptV2.saveKeyDukpt(Security.KEY_TYPE_DUPKT_IPEK, dukptKey, null, ksn, Security.KEY_ALG_TYPE_3DES, KEY_INDEX_DUKPT);
            Log.e(TAG, "saveKeyDukpt(), keyIndex:" + KEY_INDEX_DUKPT + ",code:" + code);

            //2.get account sec data with dukpt
            Bundle pIn = new Bundle();
            Bundle pOut = new Bundle();
            String[] tags = {"57", "5A", "5F24", "5F34"};
            pIn.putInt("encKeySystem", Security.SEC_DUKPT);
            pIn.putInt("encKeyIndex", KEY_INDEX_DUKPT);
            pIn.putInt("encMode", Security.DATA_MODE_CBC);
            pIn.putByteArray("encIv", new byte[16]);
            pIn.putString("panAppendContent", "01696069");
            pIn.putString("panAppendMode", "TID+PAN");
            code = MyApplication.app.emvOptV2.setAccountDataSecParam(pIn);
            String msg = "setAccountDataSecParam() with dukpt,code:" + code;
            addTextViewText(tvAccSecDataDukpt, msg);
            Log.e(TAG, msg);
            code = MyApplication.app.emvOptV2.getAccountSecData(AidlConstants.EMV.TLVOpCode.OP_NORMAL, tags, pOut);
            msg = "getAccountSecData() with dukpt Key,code:" + code + ", out:" + Utility.bundle2String(pOut);
            addTextViewText(tvAccSecDataDukpt, msg);
            Log.e(TAG, msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void testAccountSecDataParamWithRSA() {
        try {
            tvAccSecDataRsa.setText(null);
            final int KEY_INDEX_RSA = 3;

            //1. save RSA key
            String modulus = "B859D678065F2A6B7575FF174158083F50F6ED8993297B26161C19E881A8B3D209731385D29CD98D960C274DF8A4CC7BFE96A170395B1136CDB8E53CCEFED5A5590A7ED9E26CBC6C9E8DE656BC90F6E83CE49A5DC565C24C8800E1A034973B5EDDCF5A40C029871DA32B4E5AAA58A8DEDA18CAB3416E3BE91C77C5E864BAC2E7E28CED41CE6DFBC2538688B69FA1E757038A8E1948234E172EA800DB900AC7D4B1425E88CBF44B6B06B826C9CE3A07F856715130222D91C81D7AFB357E0A5A404529D2CBE288532FDCE3784BC31F5BE71DC21286B2C476353617E5FF1BF96A860020EFE0E7D0F74776C3348F21D6F037EED5927BCC9D91FB0480E172F65E908D";
            String exponent = "010001";
            final int KEY_SIZE = 2048;
            int code = MyApplication.app.securityOptV2.injectRSAKey(KEY_INDEX_RSA, KEY_SIZE, modulus, exponent);
            Log.e(TAG, "injectRSAKey(), keyIndex:" + KEY_INDEX_RSA + ",code:" + code);

            //2.get account sec data with rsa
            Bundle pIn = new Bundle();
            Bundle pOut = new Bundle();
            String[] tags = {"57", "5A", "5F24", "5F34"};
            pIn.putInt("encKeySystem", Security.SEC_RSA_KEY);
            pIn.putInt("encKeyIndex", KEY_INDEX_RSA);
            pIn.putInt("encMode", Security.DATA_MODE_CBC);
            pIn.putByteArray("encIv", new byte[16]);
            pIn.putByte("encPaddingMode", (byte) Security.PADDING_OAEP_SHA1);
            pIn.putString("panAppendContent", "01696069");
            pIn.putInt("panAppendMode", 0);
            code = MyApplication.app.emvOptV2.setAccountDataSecParam(pIn);
            String msg = "setAccountDataSecParam() with rsa,code:" + code;
            addTextViewText(tvAccSecDataRsa, msg);
            Log.e(TAG, msg);
            code = MyApplication.app.emvOptV2.getAccountSecData(AidlConstants.EMV.TLVOpCode.OP_NORMAL, tags, pOut);
            msg = "getAccountSecData() with rsa Key,code:" + code + ", out:" + Utility.bundle2String(pOut);
            addTextViewText(tvAccSecDataRsa, msg);
            Log.e(TAG, msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
