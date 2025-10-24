package com.sm.sdk.demo.security;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.NumberUtil;
import com.sm.sdk.demo.utils.Utility;

import java.util.Arrays;

public class NlkRsaTestActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_nlk_rsa_test);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.security_nlk_rsa_test);
        findViewById(R.id.btn_nlk_gen_rsa_key_pair).setOnClickListener(this);
        findViewById(R.id.btn_nlk_inject_pub_key).setOnClickListener(this);
        findViewById(R.id.btn_nlk_inject_pvt_key).setOnClickListener(this);
        findViewById(R.id.btn_nlk_read_rsa_key).setOnClickListener(this);

        //test data
//        EditText edtPubSize = findViewById(R.id.edt_inject_pub_key_size);
//        EditText edtPubModule = findViewById(R.id.edt_inject_pub_key_module);
//        EditText edtPubKeyExp = findViewById(R.id.edt_inject_pub_key_exp);
//        EditText edtPvtSize = findViewById(R.id.edt_inject_pvt_key_size);
//        EditText edtPvtModule = findViewById(R.id.edt_inject_pvt_key_module);
//        EditText edtPvtKeyExp = findViewById(R.id.edt_inject_pvt_key_exp);
//        edtPubSize.setText("2048");
//        edtPubModule.setText("AE424600AA134385C6E061627C5C2D7B67E314120D4C31C5AC5102BC26BA7A02FD95835C3690095B9407DDCCE32AB33A35A3A8F168EED8967369C151FA0E81163BA280869B637A0A1D0ABA6D4F765AE4C80A4BADDC0FBE524B032C7235C4DE744CFAD7B830C6EAC21A5164AF75DDB80A861725D9DA7B7201F7D921855C9973F25F9177E92154EAC7AA5BF0C548B81E9328DA8E84B84D21DDBEE9CD8FADC9634DF0885EBBC3830D7A417887B1D0ABC83CA47C54E232B0347D3D0DB679D381FCB931DF81F6BA4916E969FF3C68B0AF7CCD6240DCDD3CF6A040B6ADF373F67BC36A19F0B59B3D0CA6AD8EFA000A6B01BA0F32E2AA35483E7236F9FBBB5310D9434D");
//        edtPubKeyExp.setText("010001");
//        edtPvtSize.setText("2048");
//        edtPvtModule.setText("AE424600AA134385C6E061627C5C2D7B67E314120D4C31C5AC5102BC26BA7A02FD95835C3690095B9407DDCCE32AB33A35A3A8F168EED8967369C151FA0E81163BA280869B637A0A1D0ABA6D4F765AE4C80A4BADDC0FBE524B032C7235C4DE744CFAD7B830C6EAC21A5164AF75DDB80A861725D9DA7B7201F7D921855C9973F25F9177E92154EAC7AA5BF0C548B81E9328DA8E84B84D21DDBEE9CD8FADC9634DF0885EBBC3830D7A417887B1D0ABC83CA47C54E232B0347D3D0DB679D381FCB931DF81F6BA4916E969FF3C68B0AF7CCD6240DCDD3CF6A040B6ADF373F67BC36A19F0B59B3D0CA6AD8EFA000A6B01BA0F32E2AA35483E7236F9FBBB5310D9434D");
//        edtPvtKeyExp.setText("06B1F92A915E481BDDB64547996B993BC29410F3589B72D61B76C95A1D4AD0E14888F41118EF93CC76F58E6A0ED857268765105AA6237722E0B051832ACE5FDB50D33F88EC7377FEE77AC00AEF20A7015F4635FE2A1458C5A4A82C8EFECDDF962C56FEEECEC0F5C81B66C12D94A3BE2C79566E57DE731BC0439B8E1427A8A5B1BBF88AE5F6340B990F9CDE20AA09A35F92F75F2E52EE98C54124690BE1BD3D3DEFB1B452DCD662F55A0223AA85F74DBCE9BEA8E37227881A2AEB3DD569B0C68A141C9130D055B18FCCDB18ECA4C768E2D79B5D864977FD779CAA39ECA4731EEB2F1863B40BC11959D8A9C83BD52D11476FE9EBABCC57CE1879F6ABCD3E0BF229");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_nlk_gen_rsa_key_pair:
                nlkGenerateRsaKeyPair();
                break;
            case R.id.btn_nlk_inject_pub_key:
                nlkInjectRsaPublicKey();
                break;
            case R.id.btn_nlk_inject_pvt_key:
                nlkInjectRsaPrivateKey();
                break;
            case R.id.btn_nlk_read_rsa_key:
                nlkReadRsaKey();
                break;
        }
    }

    /** Generate RSA keypair */
    private void nlkGenerateRsaKeyPair() {
        try {
            EditText edt = findViewById(R.id.edt_nlk_gen_pvt_key_index);
            String pvtKeyIndexStr = edt.getText().toString();
            if (TextUtils.isEmpty(pvtKeyIndexStr)) {
                showToast("private key index should not be empty");
                edt.requestFocus();
                return;
            }
            edt = findViewById(R.id.edt_nlk_gen_pvt_key_size);
            String pvtKeySizeStr = edt.getText().toString();
            if (TextUtils.isEmpty(pvtKeySizeStr)) {
                showToast("private key size should not be empty");
                edt.requestFocus();
                return;
            }
            edt = findViewById(R.id.edt_nlk_gen_rsa_key_exp);
            String rsaKeyExpStr = edt.getText().toString();
            if (TextUtils.isEmpty(rsaKeyExpStr)) {
                showToast("rsa Key Exponent should not be empty");
                edt.requestFocus();
                return;
            }
            int pvtKeyIndex = NumberUtil.parseInt(pvtKeyIndexStr);
            int pvtKeySize = NumberUtil.parseInt(pvtKeySizeStr);

            byte[] dataOut = new byte[1024];
            Bundle bundle = new Bundle();
            bundle.putInt("keyType", 0);
            bundle.putInt("pvkIndex", pvtKeyIndex);
            bundle.putInt("keySize", pvtKeySize);
            bundle.putString("pubExponent", rsaKeyExpStr);
            addStartTimeWithClear("nlk->generateRSAKeypair()");
            int len = MyApplication.app.noLostKeyManagerV2.generateRSAKeypair(bundle, dataOut);
            addEndTime("nlk->generateRSAKeypair()");
            LogUtil.e(TAG, "generateRSAKeypair len:" + len);
            showSpendTime();
            if (len >= 0) {
                String module = ByteUtil.bytes2HexStr(Arrays.copyOf(dataOut, len));
                LogUtil.e(TAG, "module = " + module);
                showToast("nlk generate RSA keypair success");
            } else {
                showToast("nlk generate RSA keypair failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Inject a RSA public key */
    private void nlkInjectRsaPublicKey() {
        try {
            EditText edt = findViewById(R.id.edt_nlk_inject_pub_key_index);
            String keyIndexStr = edt.getText().toString();
            if (TextUtils.isEmpty(keyIndexStr)) {
                showToast("public key index should not be empty");
                edt.requestFocus();
                return;
            }
            edt = findViewById(R.id.edt_nlk_inject_pub_key_size);
            String keySizeStr = edt.getText().toString();
            if (TextUtils.isEmpty(keySizeStr)) {
                showToast("public key size should not be empty");
                edt.requestFocus();
                return;
            }
            edt = findViewById(R.id.edt_nlk_inject_pub_key_module);
            String keyModuleStr = edt.getText().toString();
            if (TextUtils.isEmpty(keyModuleStr)) {
                showToast("public key module should not be empty");
                edt.requestFocus();
                return;
            }
            edt = findViewById(R.id.edt_nlk_inject_pub_key_exp);
            String keyPubExpStr = edt.getText().toString();
            if (TextUtils.isEmpty(keyPubExpStr)) {
                showToast("public key exponent should not be empty");
                edt.requestFocus();
                return;
            }
            int keyIndex = NumberUtil.parseInt(keyIndexStr);
            int keySize = NumberUtil.parseInt(keySizeStr);
            Bundle bundle = new Bundle();
            bundle.putInt("keyIndex", keyIndex);
            bundle.putInt("keySize", keySize);
            bundle.putString("module", keyModuleStr);
            bundle.putString("exponent", keyPubExpStr);
            addStartTimeWithClear("nlk->injectRSAKey()");
            int code = MyApplication.app.noLostKeyManagerV2.injectRSAKey(bundle);
            addEndTime("nlk->injectRSAKey()");
            LogUtil.e(TAG, "injectRSAKey()->inject puk, code:" + code);
            if (code == 0) {//success
                showToast("nlk inject RSA public key success");
            } else {//failed
                showToast("nlk inject RSA public key failed,code:" + code);
            }
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Inject a RSA private key */
    private void nlkInjectRsaPrivateKey() {
        try {
            EditText edt = findViewById(R.id.edt_nlk_inject_pvt_key_index);
            String keyIndexStr = edt.getText().toString();
            if (TextUtils.isEmpty(keyIndexStr)) {
                showToast("private key index should not be empty");
                edt.requestFocus();
                return;
            }
            edt = findViewById(R.id.edt_nlk_inject_pvt_key_size);
            String keySizeStr = edt.getText().toString();
            if (TextUtils.isEmpty(keySizeStr)) {
                showToast("private key size should not be empty");
                edt.requestFocus();
                return;
            }
            edt = findViewById(R.id.edt_nlk_inject_pvt_key_module);
            String keyModuleStr = edt.getText().toString();
            if (TextUtils.isEmpty(keyModuleStr)) {
                showToast("private key module should not be empty");
                return;
            }
            edt = findViewById(R.id.edt_nlk_inject_pvt_key_exp);
            String keyPvtExpStr = edt.getText().toString();
            if (TextUtils.isEmpty(keyPvtExpStr)) {
                showToast("private key exponent should not be empty");
                return;
            }
            int keyIndex = NumberUtil.parseInt(keyIndexStr);
            int keySize = NumberUtil.parseInt(keySizeStr);
            Bundle bundle = new Bundle();
            bundle.putInt("keyIndex", keyIndex);
            bundle.putInt("keySize", keySize);
            bundle.putString("module", keyModuleStr);
            bundle.putString("exponent", keyPvtExpStr);
            addStartTimeWithClear("nlk->injectRSAKey()");
            int code = MyApplication.app.noLostKeyManagerV2.injectRSAKey(bundle);
            addEndTime("nlk->injectRSAKey()");
            LogUtil.e(TAG, "injectRSAKey()->inject pvk, code:" + code);
            if (code == 0) {//success
                showToast("nlk inject RSA private key success");
            } else {//failed
                showToast("nlk inject RSA private key failed,code:" + code);
            }
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Read RSA key */
    private void nlkReadRsaKey() {
        try {
            String keyIndexStr = this.<EditText>findViewById(R.id.edt_nlk_read_key_index).getText().toString();
            if (TextUtils.isEmpty(keyIndexStr)) {
                showToast("key index should not be empty");
                return;
            }
            TextView tvKeyInfo = findViewById(R.id.tv_nlk_rsa_key_info);
            int keyIndex = NumberUtil.parseInt(keyIndexStr);
            Bundle bundle = new Bundle();
            int code = MyApplication.app.noLostKeyManagerV2.getRsaPubKey(keyIndex, bundle);
            if (code < 0) {
                showToast("Read RSA key failed, code:" + code);
                return;
            }
            byte[] modulus = bundle.getByteArray("modulus");
            byte[] exponent = bundle.getByteArray("exponent");
            String msg = Utility.formatStr("modulus:%s\nexponent:%s", ByteUtil.bytes2HexStr(modulus), ByteUtil.bytes2HexStr(exponent));
            tvKeyInfo.setText(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
