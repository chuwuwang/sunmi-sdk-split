package com.sunmi.emv.l2.split.demo;

import android.app.Application;
import android.util.Log;

import com.sunmi.emv.l2.utils.Utility;
import com.sunmi.pay.hardware.aidlv2.emv.EMVOptV2;
import com.sunmi.pay.hardware.aidlv2.etc.ETCOptV2;
import com.sunmi.pay.hardware.aidlv2.pinpad.PinPadOptV2;
import com.sunmi.pay.hardware.aidlv2.print.PrinterOptV2;
import com.sunmi.pay.hardware.aidlv2.readcard.ReadCardOptV2;
import com.sunmi.pay.hardware.aidlv2.rfid.RFIDOptV2;
import com.sunmi.pay.hardware.aidlv2.security.BiometricManagerV2;
import com.sunmi.pay.hardware.aidlv2.security.DevCertManagerV2;
import com.sunmi.pay.hardware.aidlv2.security.NoLostKeyManagerV2;
import com.sunmi.pay.hardware.aidlv2.security.SecurityOptV2;
import com.sunmi.pay.hardware.aidlv2.system.BasicOptV2;
import com.sunmi.pay.hardware.aidlv2.tax.TaxOptV2;
import com.sunmi.pay.hardware.aidlv2.test.TestOptV2;

import sunmi.paylib.SunmiPayKernel;

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";
    public static MyApplication app;

    public BasicOptV2 basicOptV2;                   // 获取基础操作模块
    public ReadCardOptV2 readCardOptV2;             // 获取读卡模块
    public PinPadOptV2 pinPadOptV2;                 // 获取PinPad操作模块
    public SecurityOptV2 securityOptV2;             // 获取安全操作模块
    public EMVOptV2 emvOptV2;                       // 获取EMV操作模块
    public TaxOptV2 taxOptV2;                       // 获取税控操作模块
    public ETCOptV2 etcOptV2;                       // 获取ETC操作模块
    public PrinterOptV2 printerOptV2;               // 获取打印操作模块
    public TestOptV2 testOptV2;                     // 获取测试操作模块
    public DevCertManagerV2 devCertManagerV2;       // 设备证书操作模块
    public NoLostKeyManagerV2 noLostKeyManagerV2;   // NoLostKey操作模块
    //    public HCEManagerV2 hceManagerV2;               // HCE操作模块
    public RFIDOptV2 rfidOptV2;                     // RFID操作模块
//    public BiometricManagerV2 mBiometricManagerV2;  // 生物特征模块

    private boolean connectPaySDK;//是否已连接PaySDK

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
    }

    public boolean isConnectPaySDK() {
        return connectPaySDK;
    }

    /**
     * bind PaySDK service
     */
    public void bindPaySDKService() {
        final SunmiPayKernel payKernel = SunmiPayKernel.getInstance();
        payKernel.setEmvL2Split(true);
        payKernel.initPaySDK(this, new SunmiPayKernel.ConnectCallback() {
            @Override
            public void onConnectPaySDK() {
                Log.e(TAG, "onConnectPaySDK...");
                emvOptV2 = payKernel.mEMVOptV2;
                basicOptV2 = payKernel.mBasicOptV2;
                pinPadOptV2 = payKernel.mPinPadOptV2;
                readCardOptV2 = payKernel.mReadCardOptV2;
                securityOptV2 = payKernel.mSecurityOptV2;
                taxOptV2 = payKernel.mTaxOptV2;
                etcOptV2 = payKernel.mETCOptV2;
                printerOptV2 = payKernel.mPrinterOptV2;
                testOptV2 = payKernel.mTestOptV2;
                devCertManagerV2 = payKernel.mDevCertManagerV2;
                noLostKeyManagerV2 = payKernel.mNoLostKeyManagerV2;
//                mBiometricManagerV2 = payKernel.mBiometricManagerV2;
//                hceManagerV2 = payKernel.mHCEManagerV2;
                rfidOptV2 = payKernel.mRFIDOptV2;
                connectPaySDK = true;
            }

            @Override
            public void onDisconnectPaySDK() {
                Log.e(TAG, "onDisconnectPaySDK...");
                connectPaySDK = false;
                emvOptV2 = null;
                basicOptV2 = null;
                pinPadOptV2 = null;
                readCardOptV2 = null;
                securityOptV2 = null;
                taxOptV2 = null;
                etcOptV2 = null;
                printerOptV2 = null;
                devCertManagerV2 = null;
                noLostKeyManagerV2 = null;
//                mBiometricManagerV2 = null;
//                hceManagerV2 = null;
                rfidOptV2 = null;
                Utility.showToast("connect to paySDK failed...");
            }
        });
    }

}
