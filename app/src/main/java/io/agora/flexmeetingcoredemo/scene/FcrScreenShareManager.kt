package io.agora.flexmeetingcoredemo.scene

import android.content.Context
import io.agora.agoracore.core2.FcrCoreEngine
import io.agora.agoracore.core2.bean.FcrCapability
import io.agora.agoracore.core2.bean.FcrMediaSourceState
import io.agora.agoracore.core2.bean.FcrScreenCaptureParams
import io.agora.agoracore.core2.bean.FcrLocalStreamCreateConfig
import io.agora.agoracore.core2.bean.FcrStreamEvent
import io.agora.agoracore.core2.bean.FcrUpdateStreamPrivilege
import io.agora.agoracore.core2.control.observer.FcrMobileMediaObserver
import io.agora.agoracore.core2.control.observer.FcrStreamObserver
import io.agora.agoracore.core2.control.privilege.FcrPermissionAction
import io.agora.agoracore.rte2.code.ErrorCode
import io.agora.agoracore.rte2.code.ErrorCodeManager
import io.agora.agoracore.rte2.code.ErrorCodeModule
import io.agora.agoracore.rte2.code.ErrorCodeService
import io.agora.agoracore.rte2.pub.bean.AgoraRteAudioSourceType
import io.agora.agoracore.rte2.pub.bean.AgoraRteMediaSourceState
import io.agora.agoracore.rte2.pub.bean.AgoraRteStreamPrivilegeOperation
import io.agora.agoracore.rte2.pub.bean.AgoraRteStreamType
import io.agora.agoracore.rte2.pub.bean.AgoraRteVideoSourceType
import io.agora.core.common.log.LogX
import io.agora.core.common.obs.FcrCallback
import io.agora.core.common.obs.FcrError
import java.util.concurrent.CopyOnWriteArrayList

/**
 * author : LF
 * date : 2024/7/26
 * description :manager to unified manage of screen sharing function
 */
class FcrScreenShareManager(
    val core: FcrCoreEngine
) : IFcrScreenShareManager {
    private var screenShareState = FcrMediaSourceState.CLOSE
    private val stateChangedListeners = CopyOnWriteArrayList<OnScreenShareStateChangedListener>()
    private var screenCaptureParameters: FcrScreenCaptureParams? = null
    private val streamObserver = object : FcrStreamObserver {
        override fun onStreamsAdded(roomId: String, event: List<FcrStreamEvent>) {
            for (streamEvent in event) {
                onStreamAdded(roomId, streamEvent)
            }
        }

        override fun onStreamsRemoved(roomId: String, event: List<FcrStreamEvent>) {
            for (streamEvent in event) {
                onStreamRemoved(roomId, streamEvent)
            }
        }
    }
    private val screenCaptureObserver = object : FcrMobileMediaObserver() {
        override fun onScreenCaptureStateUpdated(state: FcrMediaSourceState) {
            super.onScreenCaptureStateUpdated(state)
            onScreenStateChanged(state)
        }
    }

    override fun startScreenShare(
        context: Context,
        params: FcrScreenCaptureParams,
        callback: FcrCallback<Any>?
    ) {
        val noAuthCode = ErrorCodeManager.getCode(
            ErrorCodeService.Rte_Client,
            ErrorCodeModule.Rte_Screen,
            ErrorCode.NOT_AUTHORIZED.value
        )
        val noAuthMsg = ErrorCodeManager.getMessage(
            ErrorCodeService.Rte_Client,
            "failed to start screen share, no permission"
        )
        val permissionInfo = core.getRoomControl()?.getPrivilegeControl()
            ?.getLocalUserPermissionInfo(FcrPermissionAction.STREAM_START_SCREEN_SHARING) ?: run {
            LogX.e(TAG, "failed to start screen share, no permission")
            callback?.onFailure(FcrError(noAuthCode, noAuthMsg))
            return
        }
        if (!permissionInfo.enable) {
            LogX.e(TAG, "failed to start screen share, no permission")
            callback?.onFailure(FcrError(noAuthCode, noAuthMsg))
            return
        }
        val ret = core.getMobileMediaControl().isCapabilitySupported(
            context.applicationContext,
            FcrCapability.SCREEN_CAPTURE
        )
        if (!ret) {
            LogX.e(TAG, "failed to start screen share, device not support")
            val noSupportCode = ErrorCodeManager.getCode(
                ErrorCodeService.Rte_Client,
                ErrorCodeModule.Rte_Screen,
                ErrorCode.Undefined.value
            )
            val noSupportMsg = ErrorCodeManager.getMessage(
                ErrorCodeService.Rte_Client,
                "failed to start screen share, no permission"
            )
            callback?.onFailure(FcrError(noSupportCode, noSupportMsg))
            return
        }
        val localUid = core.getRoomControl()?.getUserControl()?.getLocalUser()?.userId
        if (localUid == null) {
            LogX.e(TAG, "failed to start screen share, local user is null")
            val noUserCode = ErrorCodeManager.getCode(
                ErrorCodeService.Rte_Client,
                ErrorCodeModule.Rte_Screen,
                ErrorCode.Undefined.value
            )
            val noUserMsg = ErrorCodeManager.getMessage(
                ErrorCodeService.Rte_Client,
                "failed to start screen share, local user is null"
            )
            callback?.onFailure(FcrError(noUserCode, noUserMsg))
            return
        }
        this.screenCaptureParameters = params
        this.addObserver()
        val config = FcrLocalStreamCreateConfig(
            streamType = if (params.hasAudio) {
                AgoraRteStreamType.BOTH
            } else {
                AgoraRteStreamType.VIDEO
            },
            videoSourceType = AgoraRteVideoSourceType.SCREEN,
            audioSourceType = AgoraRteAudioSourceType.LOOPBACK
        )
        core.getRoomControl()
            ?.getStreamControl()
            ?.addLocalScreenStream(config, object : FcrCallback<Any> {
                override fun onSuccess(res: Any?) {
                    callback?.onSuccess(res)
                }

                override fun onFailure(error: FcrError) {
                    super.onFailure(error)
                    callback?.onFailure(error)
                }
            })
    }

    override fun stopScreenShare(streamId: String, callback: FcrCallback<Any>?) {
        val noAuthCode = ErrorCodeManager.getCode(
            ErrorCodeService.Rte_Client,
            ErrorCodeModule.Rte_Screen,
            ErrorCode.NOT_AUTHORIZED.value
        )
        val noAuthMsg = ErrorCodeManager.getMessage(
            ErrorCodeService.Rte_Client,
            "failed to start screen share, no permission"
        )
        //以下代码保留，停止的时候不做权限判断，后期如果改由服务端关闭屏幕共享，这里可以再打开
//        val permissionInfo = core.getRoomControl()?.getPrivilegeControl()
//            ?.getLocalUserPermissionInfo(FcrPermissionAction.STREAM_STOP_SCREEN_SHARING) ?: run {
//            LogX.e(TAG, "failed to stop screen share, no permission")
//            callback?.onFailure(FcrError(noAuthCode, noAuthMsg))
//            return
//        }
//        if (!permissionInfo.enable) {
//            LogX.e(TAG, "failed to stop screen share, no permission")
//            callback?.onFailure(FcrError(noAuthCode, noAuthMsg))
//            return
//        }
        core.getRoomControl()?.getStreamControl()?.removeScreenStream(object : FcrCallback<Any> {
            override fun onSuccess(res: Any?) {
                callback?.onSuccess(res)
            }

            override fun onFailure(error: FcrError) {
                super.onFailure(error)
                callback?.onFailure(error)
            }
        })
    }


    override fun startShareAudio(streamId: String, callback: FcrCallback<Any>?) {
        val shareMap = core.getRoomControl()?.getUserControl()?.getLocalUser()?.screenShare ?: run {
            LogX.e(TAG, "failed to start share audio, share stream is null")
            return
        }
        if (shareMap.isEmpty()) {
            LogX.e(TAG, "failed to start share audio, share stream is empty")
            return
        }
        val param = mapOf(
            streamId to FcrUpdateStreamPrivilege(
                videoPrivilege = AgoraRteStreamPrivilegeOperation.NO_OPERATION,
                audioPrivilege = AgoraRteStreamPrivilegeOperation.HAS_PRIVILEGE
            )
        )
        core.getRoomControl()
            ?.getStreamControl()
            ?.updatePublishPrivilegeOfStreams(param, object : FcrCallback<Any> {
                override fun onSuccess(res: Any?) {
                    callback?.onSuccess(res)
                }

                override fun onFailure(error: FcrError) {
                    super.onFailure(error)
                    callback?.onFailure(error)
                }
            })
    }

    override fun stopShareAudio(streamId: String, callback: FcrCallback<Any>?) {
        val shareMap = core.getRoomControl()?.getUserControl()?.getLocalUser()?.screenShare ?: run {
            LogX.e(TAG, "failed to stop share audio, share stream is null")
            return
        }
        if (shareMap.isEmpty()) {
            LogX.e(TAG, "failed to stop share audio, share stream is empty")
            return
        }
        val param = mapOf(
            streamId to FcrUpdateStreamPrivilege(
                videoPrivilege = AgoraRteStreamPrivilegeOperation.NO_OPERATION,
                audioPrivilege = AgoraRteStreamPrivilegeOperation.NO_PRIVILEGE
            )
        )
        core.getRoomControl()
            ?.getStreamControl()
            ?.updatePublishPrivilegeOfStreams(param, object : FcrCallback<Any> {
                override fun onSuccess(res: Any?) {
                    callback?.onSuccess(res)
                }

                override fun onFailure(error: FcrError) {
                    super.onFailure(error)
                    callback?.onFailure(error)
                }
            })
    }

    override fun getScreenShareState(streamId: String): FcrMediaSourceState {
        return this.screenShareState
    }

    override fun registerScreenShareStateListener(listener: OnScreenShareStateChangedListener) {
        stateChangedListeners.add(listener)
    }

    override fun unregisterScreenShareStateListener(listener: OnScreenShareStateChangedListener) {
        stateChangedListeners.remove(listener)
    }

    /**
     * on screen share state changed
     * @param state
     */
    private fun onScreenStateChanged(state: FcrMediaSourceState) {
        LogX.i(TAG, "onScreenCaptureStateUpdated: $state")
        this.screenShareState = state

        if (state == FcrMediaSourceState.ERROR) {
            stopCurrentScreenShare()
        }

        for (listener in stateChangedListeners) {
            listener.onStateChanged(state)
        }
    }

    /**
     * stop current screen share
     */
    private fun stopCurrentScreenShare() {
        val shareMap = core.getRoomControl()?.getUserControl()?.getLocalUser()?.screenShare ?: run {
            LogX.e(TAG, "failed to stop current share audio, share stream is null")
            return
        }
        if (shareMap.isEmpty()) {
            LogX.e(TAG, "failed to stop current share audio, share stream is empty")
            return
        }
        val shareItem = shareMap.entries.last()
        stopScreenShare(shareItem.key)
    }

    /**
     * on stream added
     * @param roomId
     * @param event
     */
    private fun onStreamAdded(roomId: String, event: FcrStreamEvent) {
        val stream = event.modifiedStream
        LogX.i(TAG, "onShareStreamAdded roomId:$roomId event:$event")
        if (stream.videoSourceType == AgoraRteVideoSourceType.SCREEN) {
            onShareStreamAdded(roomId, event)
        }
    }

    /**
     * on share stream added
     * @param roomId
     * @param event
     */
    private fun onShareStreamAdded(roomId: String, event: FcrStreamEvent) {
        val stream = event.modifiedStream
        val localUid = core.getRoomControl()?.getUserControl()?.getLocalUser()?.userId
        if (stream.owner.userId != localUid) {
            return
        }
        if (stream.videoSourceState != AgoraRteMediaSourceState.OPEN) {
            LogX.e(TAG, "failed to open screen share, video state is not open")
            return
        }
        val params = screenCaptureParameters
        if (params == null) {
            LogX.e(TAG, "failed to start screen share, params not found")
            return
        }
        val ret = core.getMobileMediaControl().startScreenCapture(params)
        ret?.let {
            onScreenStateChanged(FcrMediaSourceState.ERROR)
        }
    }

    /**
     * on stream removed
     * @param roomId
     * @param event
     */
    private fun onStreamRemoved(roomId: String, event: FcrStreamEvent) {
        val stream = event.modifiedStream

        if (stream.videoSourceType == AgoraRteVideoSourceType.SCREEN) {
            onShareStreamRemoved(roomId, event)
        }
    }

    /**
     * on share stream removed
     * @param roomId
     * @param event
     */
    private fun onShareStreamRemoved(roomId: String, event: FcrStreamEvent) {
        val stream = event.modifiedStream
        val localUid = core.getRoomControl()?.getUserControl()?.getLocalUser()?.userId
        if (stream.owner.userId == localUid) {
            val ret = core.getMobileMediaControl().stopScreenCapture()
            ret?.let {
                onScreenStateChanged(FcrMediaSourceState.ERROR)
            }
            removeObserver()
        }
    }

    /**
     * add observer
     */
    private fun addObserver() {
        core.getRoomControl()?.getStreamControl()?.addObserver(streamObserver)
        core.getMobileMediaControl().addObserver(screenCaptureObserver)
    }

    /**
     * remove observer
     */
    private fun removeObserver() {
        core.getRoomControl()?.getStreamControl()?.removeObserver(streamObserver)
        core.getMobileMediaControl().removeObserver(screenCaptureObserver)
    }

    companion object {
        private const val TAG = "FcrScreenShareManager"
        private var CODE_OK = 0
    }
}
