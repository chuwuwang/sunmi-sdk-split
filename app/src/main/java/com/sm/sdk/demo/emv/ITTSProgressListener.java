package com.sm.sdk.demo.emv;

import android.speech.tts.TextToSpeech;

public interface ITTSProgressListener {

    /**
     * Called when an utterance "starts" as perceived by the caller. This will
     * be soon before audio is played back in the case of a {@link TextToSpeech#speak}
     * or before the first bytes of a file are written to the file system in the case
     * of {@link TextToSpeech#synthesizeToFile}.
     *
     * @param utteranceId The utterance ID of the utterance.
     */
    void onStart(String utteranceId);

    /**
     * Called when an utterance has successfully completed processing.
     * All audio will have been played back by this point for audible output, and all
     * output will have been written to disk for file synthesis requests.
     * <p>
     * This request is guaranteed to be called after {@link #onStart(String)}.
     *
     * @param utteranceId The utterance ID of the utterance.
     */
    void onDone(String utteranceId);

    /**
     * Called when an error has occurred during processing. This can be called
     * at any point in the synthesis process. Note that there might be calls
     * to {@link #onStart(String)} for specified utteranceId but there will never
     * be a call to both {@link #onDone(String)} and {@link #onError(String)} for
     * the same utterance.
     *
     * @param utteranceId The utterance ID of the utterance.
     * @deprecated Use {@link #onError(String, int)} instead
     */

    void onError(String utteranceId);

    /**
     * Called when an utterance has been stopped while in progress or flushed from the
     * synthesis queue. This can happen if a client calls {@link TextToSpeech#stop()}
     * or uses {@link TextToSpeech#QUEUE_FLUSH} as an argument with the
     * {@link TextToSpeech#speak} or {@link TextToSpeech#synthesizeToFile} methods.
     *
     * @param utteranceId The utterance ID of the utterance.
     * @param interrupted If true, then the utterance was interrupted while being synthesized
     *                    and its output is incomplete. If false, then the utterance was flushed
     *                    before the synthesis started.
     */
    void onStop(String utteranceId, boolean interrupted);
}
