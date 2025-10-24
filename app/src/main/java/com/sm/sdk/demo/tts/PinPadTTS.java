package com.sm.sdk.demo.tts;

import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.utils.LogUtil;

import java.util.Locale;

public class PinPadTTS extends UtteranceProgressListener {
    private static final String TAG = "PinPadTTS";
    private TextToSpeech textToSpeech;
    private boolean supportTTS;
    private final SparseArray<String> ttsMap = new SparseArray<>();
    private volatile int playTextId = Integer.MIN_VALUE;

    private PinPadTTS() {

    }

    public static PinPadTTS getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void setTtsLanguage(String language) {
        if (!TextUtils.isEmpty(language)) {
            updateTtsLanguage(language);
        }
    }

    public void init() {
        //初始化TTS对象
        destroy();
        textToSpeech = new TextToSpeech(MyApplication.app, this::onTTSInit);
        textToSpeech.setOnUtteranceProgressListener(this);
    }

    public void play(int textId) {
        if (!supportTTS) {
            Log.e(TAG, "PinPadTTS: play TTS failed, TTS not support...");
            return;
        }
        if (textToSpeech == null) {
            Log.e(TAG, "PinPadTTS: play TTS slipped, textToSpeech not init..");
            return;
        }
        Log.e(TAG, "textId: " + textId + ",playTextId:" + playTextId);
        if (textId != playTextId) {
            playInner(ttsMap.get(textId));
            playTextId = textId;
        }
    }

    private void playInner(String text) {
        Log.e(TAG, "play() text: [" + text + "]");
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "0");
    }

    @Override
    public void onStart(String utteranceId) {
        Log.e(TAG, "播放开始,utteranceId:" + utteranceId);
    }

    @Override
    public void onDone(String utteranceId) {
        Log.e(TAG, "播放结束,utteranceId:" + utteranceId);
        resetPrevTextId();
    }

    @Override
    public void onError(String utteranceId) {
        Log.e(TAG, "播放出错,utteranceId:" + utteranceId);
    }

    public void resetPrevTextId() {
        playTextId = Integer.MIN_VALUE;
    }

    public int getPlayTextId() {
        return playTextId;
    }

    public void stop() {
        if (textToSpeech != null) {
            int code = textToSpeech.stop();
            Log.e(TAG, "tts stop() code:" + code);
            resetPrevTextId();
        }
    }

    public boolean isSpeaking() {
        if (textToSpeech != null) {
            return textToSpeech.isSpeaking();
        }
        return false;
    }

    public void destroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
    }

    /** TTS初始化回调 */
    private void onTTSInit(int status) {
        if (status != TextToSpeech.SUCCESS) {
            LogUtil.e(TAG, "PinPadTTS: init TTS failed, status:" + status);
            supportTTS = false;
            return;
        }
        updateTtsLanguage(null);
        if (supportTTS) {
            textToSpeech.setPitch(1.0f);
            textToSpeech.setSpeechRate(1.0f);
            playInner("");
            LogUtil.e(TAG, "onTTSInit() success,locale:" + textToSpeech.getVoice().getLocale());
        }
    }

    /** 更新TTS语言 */
    private void updateTtsLanguage(String language) {
        Locale locale = Locale.ENGLISH;
        if (!TextUtils.isEmpty(language)) {
            locale = new Locale(language);
        }
        int result = textToSpeech.setLanguage(locale);
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            supportTTS = false; //系统不支持当前Locale对应的语音播报
            LogUtil.e(TAG, "updateTtsLanguage() failed, TTS not support in locale:" + locale);
        } else {
            supportTTS = true;
            LogUtil.e(TAG, "updateTtsLanguage() success, TTS locale:" + locale);
        }
        loadTTSText();
    }

    /** load tts text */
    private void loadTTSText() {
        ttsMap.put(0, "All digits cleared, please re-enter pin");
        ttsMap.put(1, "First digit entered");
        ttsMap.put(2, "Second digit entered");
        ttsMap.put(3, "Third digit entered");
        ttsMap.put(4, "Fourth digit entered");
        ttsMap.put(5, "Fifth digit entered");
        ttsMap.put(6, "Sixth digit entered");
        ttsMap.put(7, "Seventh digit entered");
        ttsMap.put(8, "Eighth digit entered");
        ttsMap.put(9, "Ninth digit entered");
        ttsMap.put(10, "Tenth digit entered");
        ttsMap.put(11, "Eleventh digit entered");
        ttsMap.put(12, "Twelfth digit entered");

        ttsMap.put(13, "Cancel button");
        ttsMap.put(14, "Clear all digits button");
        ttsMap.put(15, "Enter button");

        ttsMap.put(16, "pin pad top");
        ttsMap.put(17, "pin pad below");
        ttsMap.put(18, "pin pad left");
        ttsMap.put(19, "pin pad right");
        ttsMap.put(20, "Maximum input");

        ttsMap.put(100, "Please enter pin. The keypad is standard telephone layout with 1, 2, 3 towards the bottom half of the screen and cancel, clear and OK at the bottom. If you are too high on the screen, the device will tell you pin pad below. Please enter your pin. Find the desired key using the beeps, then double tap anywhere on the screen to confirm. When finished select Enter at the bottom right and double tap. Please press Enter Button if all the digits have been entered. Or press Clear Button to start the pin entry again. Or press Cancel Button to cancel the transaction.");

    }

    private static final class SingletonHolder {
        private static final PinPadTTS INSTANCE = new PinPadTTS();
    }
}
