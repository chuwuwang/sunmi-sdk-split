package com.sm.sdk.demo.wrapper;

import com.sm.sdk.demo.emv.ITTSProgressListener;
import com.sm.sdk.demo.utils.LogUtil;

public class TTSProgressListenerWrapper implements ITTSProgressListener {
    private static final String TAG = "TTSProgressListenerWrap";

    @Override
    public void onStart(String utteranceId) {
        LogUtil.e(TAG, "onStart()");
    }

    @Override
    public void onDone(String utteranceId) {
        LogUtil.e(TAG, "onDone()");
    }

    @Override
    public void onError(String utteranceId) {
        LogUtil.e(TAG, "onError()");
    }

    @Override
    public void onStop(String utteranceId, boolean interrupted) {
        LogUtil.e(TAG, "onStop()");
    }
}
