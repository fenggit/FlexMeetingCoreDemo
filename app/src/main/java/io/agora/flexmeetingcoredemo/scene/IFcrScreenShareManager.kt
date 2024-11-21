package io.agora.flexmeetingcoredemo.scene

import android.content.Context
import io.agora.agoracore.core2.bean.FcrMediaSourceState
import io.agora.agoracore.core2.bean.FcrScreenCaptureParams
import io.agora.core.common.obs.FcrCallback

/**
 * author : LF
 * date : 2024/8/9
 * description : screen share manager
 */
interface IFcrScreenShareManager {

    /**
     * start screen share
     * @param context application context
     * @param params FcrScreenCaptureParams
     * @param callback
     * https://confluence.agoralab.co/pages/viewpage.action?pageId=1301743211
     */
    fun startScreenShare(
        context: Context,
        params: FcrScreenCaptureParams,
        callback: FcrCallback<Any>? = null
    )

    /**
     * stop screen share
     * @param streamId share stream id
     * @param callback
     */
    fun stopScreenShare(streamId: String, callback: FcrCallback<Any>? = null)

    /**
     * start share audio
     * @param streamId share stream id
     * @param callback
     */
    fun startShareAudio(streamId: String, callback: FcrCallback<Any>?)

    /**
     * stop share audio
     * @param streamId share stream id
     * @param callback
     */
    fun stopShareAudio(streamId: String, callback: FcrCallback<Any>? = null)

    /**
     * get screen share state
     * @param streamId share stream id
     */
    fun getScreenShareState(streamId: String): FcrMediaSourceState

    /**
     * register screen share state change listener
     * @param listener
     */
    fun registerScreenShareStateListener(listener: OnScreenShareStateChangedListener)

    /**
     * unregister screen share state change listener
     * @param listener
     */
    fun unregisterScreenShareStateListener(listener: OnScreenShareStateChangedListener)
}

interface OnScreenShareStateChangedListener {
    /**
     * screen share state change callback
     * @param state screen share state change
     */
    fun onStateChanged(state: FcrMediaSourceState)
}