package io.agora.flexmeetingcoredemo.ui

import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.lifecycle.ViewModelProvider
import io.agora.agoracore.core2.bean.FcrDeviceType
import io.agora.agoracore.core2.bean.FcrMediaSourceState
import io.agora.agoracore.core2.bean.FcrScreenCaptureParams
import io.agora.agoracore.core2.bean.FcrStreamEvent
import io.agora.agoracore.core2.bean.FcrVideoRenderConfig
import io.agora.agoracore.core2.bean.FcrVideoSourceType
import io.agora.agoracore.core2.bean.FcrVideoStreamType
import io.agora.agoracore.core2.control.observer.FcrStreamObserver
import io.agora.agoracore.rte2.pub.bean.AgoraVideoRenderMode
import io.agora.core.common.log.LogX
import io.agora.core.common.obs.FcrCallback
import io.agora.core.common.obs.FcrError
import io.agora.flexmeetingcoredemo.databinding.FcrFragmentRoomBinding
import io.agora.flexmeetingcoredemo.scene.FcrScreenShareManager
import io.agora.flexmeetingcoredemo.ui.base.BaseFragment
import io.agora.flexmeetingcoredemo.vm.FcrRoomViewModel

/**
 * @author chenbinhang@agora.io
 * @date 2024/11/19 16:44
 */
class FcrRoomFragment : BaseFragment() {
    companion object {
        const val TAG = "FcrRoomFragment"
    }

    private lateinit var binding: FcrFragmentRoomBinding
    private lateinit var shareManager: FcrScreenShareManager
    private val viewModel by lazy {
        ViewModelProvider(requireActivity())[FcrRoomViewModel::class.java]
    }
    private var rooId = ""

    private val streamObserver = object : FcrStreamObserver {
        override fun onStreamsAdded(roomId: String, event: List<FcrStreamEvent>) {
            super.onStreamsAdded(roomId, event)
            LogX.i(
                TAG,
                "roomId : $roomId  onStreamsAdded: ${event.size} 自己的id： ${
                    viewModel.coreEngine.getRoomControl()?.getUserControl()?.getLocalUser()?.userId
                }"
            )
            event.stream().forEach {
                LogX.i(
                    TAG,
                    "是否自己的流 : ${
                        it.modifiedStream.owner.userId == viewModel.coreEngine.getRoomControl()?.getUserControl()?.getLocalUser()?.userId
                    }"
                )
            }
            event.stream().filter {
                //过滤掉自己的流，还有屏幕共享的流
                (it.modifiedStream.owner.userId != viewModel.coreEngine.getRoomControl()?.getUserControl()?.getLocalUser()?.userId)
                        && it.modifiedStream.videoSourceType != FcrVideoSourceType.SCREEN
            }.findFirst().ifPresent {
                setRemoteVideo(it.modifiedStream.streamId, binding.remoteVideoView)
            }
            //fl_screen_view
            event.stream().filter {
                //过滤掉自己的流，并且是屏幕共享的流
                (it.modifiedStream.owner.userId != viewModel.coreEngine.getRoomControl()?.getUserControl()?.getLocalUser()?.userId)
                        && it.modifiedStream.videoSourceType == FcrVideoSourceType.SCREEN
            }.findFirst().ifPresent {
                binding.flScreenView.visibility = View.VISIBLE
                setRemoteVideo(it.modifiedStream.streamId, binding.flScreenView)
            }
        }

        override fun onStreamsRemoved(roomId: String, event: List<FcrStreamEvent>) {
            super.onStreamsRemoved(roomId, event)
            event.stream().filter {
                it.modifiedStream.owner.userId != viewModel.coreEngine.getRoomControl()?.getUserControl()?.getLocalUser()?.userId
            }.findFirst().ifPresent {
                LogX.i(TAG, "onStreamsRemoved streamId:${it.modifiedStream.videoSourceType}")
                if (it.modifiedStream.videoSourceType == FcrVideoSourceType.SCREEN) {
                    binding.flScreenView.visibility = View.GONE
                }
                viewModel.coreEngine.getRoomControl()?.getStreamControl()?.stopRenderRemoteVideoStream(it.modifiedStream.streamId)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FcrFragmentRoomBinding.inflate(LayoutInflater.from(context), container, false)
        binding.root.setOnClickListener {}
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        shareManager = FcrScreenShareManager(viewModel.coreEngine)
        viewModel.coreEngine.getMobileMediaControl().startCameraPreview(binding.selfVideoView, FcrVideoRenderConfig())
        binding.tvRoomId.text = viewModel.roomId
        rooId = viewModel.roomId
        viewModel.coreEngine.getRoomControl()?.getStreamControl()?.addObserver(streamObserver)
        viewModel.coreEngine.getMobileMediaControl().openDevice(FcrDeviceType.CAMERA)
        viewModel.coreEngine.getMobileMediaControl().openDevice(FcrDeviceType.MICROPHONE)
        viewModel.coreEngine.getRoomControl()?.getStreamControl()?.getStreamList()?.firstOrNull {
            //过滤掉自己的流，还有屏幕共享的流
            (it.owner.userId != viewModel.coreEngine.getRoomControl()?.getUserControl()?.getLocalUser()?.userId)
                    && it.videoSourceType != FcrVideoSourceType.SCREEN
        }?.let {
            setRemoteVideo(it.streamId, binding.remoteVideoView)
        }
        viewModel.coreEngine.getRoomControl()?.getStreamControl()?.getStreamList()?.firstOrNull {
            (it.owner.userId != viewModel.coreEngine.getRoomControl()?.getUserControl()?.getLocalUser()?.userId)
                    && it.videoSourceType == FcrVideoSourceType.SCREEN
        }?.let {
            LogX.i(TAG, "屏幕共享流：${it.streamId}")
            setRemoteVideo(it.streamId, binding.flScreenView)
        }
        binding.btnLeaveRoom.setOnClickListener {
            LogX.i(TAG, "btnLeaveRoom")
            leaveRoom()
        }

        binding.btnOpenLocalVideo.setOnClickListener {
            switchCameraState()
        }
        binding.btnOpenLocalAudio.setOnClickListener {
            switchMicState()
        }
        binding.btnShareScreen.setOnClickListener {
            if (hasScreenShareStream()) {
                stopShareScreen()
                return@setOnClickListener
            }
            val displayMetrics = DisplayMetrics()
            val wm = requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = wm.defaultDisplay
            display.getMetrics(displayMetrics)
            val size = Size(displayMetrics.widthPixels, displayMetrics.heightPixels)
            val params = FcrScreenCaptureParams(size, frameRate = 30, bitrate = 600, hasAudio = false)
            shareManager.startScreenShare(requireContext(), params, object : FcrCallback<Any> {
                override fun onSuccess(res: Any?) {
                    LogX.i(TAG, "succeed to start screen share")
                    binding.btnShareScreen.text = "停止共享"
                }

                override fun onFailure(error: FcrError) {
                    LogX.e(TAG, "failed to start screen share, error code: $error")
                }
            })
        }
        initDevice()
    }

    private fun initDevice() {
        viewModel.coreEngine.getMobileMediaControl().openDevice(FcrDeviceType.CAMERA)
        viewModel.coreEngine.getMobileMediaControl().openDevice(FcrDeviceType.MICROPHONE)
    }

    private fun setRemoteVideo(streamId: String, view: View) {
        val renderConfig = FcrVideoRenderConfig(AgoraVideoRenderMode.FIT)
        viewModel.coreEngine.getRoomControl()?.getStreamControl()?.startRenderRemoteVideoStream(
            streamId,
            view,
            renderConfig,
            FcrVideoStreamType.LOW
        )
    }

    private fun stopShareScreen() {
        viewModel.coreEngine.getRoomControl()?.getStreamControl()?.getStreamList()?.firstOrNull {
            it.videoSourceType == FcrVideoSourceType.SCREEN
        }?.let {
            LogX.i(TAG, "停止 屏幕共享流：${it.streamId}")
            shareManager.stopScreenShare(it.streamId, object : FcrCallback<Any> {
                override fun onSuccess(res: Any?) {
                    LogX.i(TAG, "succeed to stop screen share")
                    binding.btnShareScreen.text = "共享屏幕"
                }

                override fun onFailure(error: FcrError) {
                    LogX.e(TAG, "failed to stop screen share, error code: $error")
                }
            })
        }
    }

    private fun hasScreenShareStream(): Boolean {
        return viewModel.coreEngine.getRoomControl()?.getStreamControl()?.getStreamList()?.firstOrNull {
            it.videoSourceType == FcrVideoSourceType.SCREEN
        } != null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.coreEngine.getMobileMediaControl().stopCameraPreview()
        viewModel.coreEngine.getRoomControl()?.getStreamControl()?.removeObserver(streamObserver)
    }

    private fun switchCameraState() {
        if (viewModel.coreEngine.getMobileMediaControl().getDeviceState(FcrDeviceType.CAMERA) == FcrMediaSourceState.OPEN) {
            viewModel.coreEngine.getMobileMediaControl().closeDevice(FcrDeviceType.CAMERA)
            binding.btnOpenLocalVideo.text = "打开摄像头"
            binding.selfVideoView.visibility = View.INVISIBLE
        } else {
            viewModel.coreEngine.getMobileMediaControl().openDevice(FcrDeviceType.CAMERA)
            binding.btnOpenLocalVideo.text = "关闭摄像头"
            binding.selfVideoView.visibility = View.VISIBLE
        }
    }

    private fun switchMicState() {
        if (viewModel.coreEngine.getMobileMediaControl().getDeviceState(FcrDeviceType.MICROPHONE) == FcrMediaSourceState.OPEN) {
            viewModel.coreEngine.getMobileMediaControl().closeDevice(FcrDeviceType.MICROPHONE)
            binding.btnOpenLocalAudio.text = "打开麦克风"
        } else {
            viewModel.coreEngine.getMobileMediaControl().openDevice(FcrDeviceType.MICROPHONE)
            binding.btnOpenLocalAudio.text = "关闭麦克风"

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (viewModel.leaveRoom.value == false) {
            leaveRoom()
        }
    }

    private fun leaveRoom() {
        viewModel.coreEngine.getRoomControl()?.leave()
        viewModel.leaveRoom.value = true
    }
}