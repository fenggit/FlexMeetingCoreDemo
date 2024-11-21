package io.agora.flexmeetingcoredemo.vm

import android.content.Context
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.shengwang.http.FcrSceneRoomService
import cn.shengwang.http.bean.FcrSceneCreateRoomReq
import cn.shengwang.http.bean.FcrSceneCreateRoomRes
import cn.shengwang.http.bean.FcrSceneJoinRoomReq
import cn.shengwang.http.bean.FcrSceneJoinRoomRes
import cn.shengwang.scene.FcrUISceneCreatorConfig
import cn.shengwang.scene.helper.FcrHttpHelper
import io.agora.agoracore.core2.FcrCoreEngine
import io.agora.agoracore.core2.FcrCoreEngineImpl
import io.agora.agoracore.core2.bean.FcrCoreEngineConfig
import io.agora.agoracore.core2.bean.FcrRoomControlCreateConfig
import io.agora.agoracore.core2.bean.FcrRoomJoinOptions
import io.agora.agoracore.core2.bean.FcrRoomType
import io.agora.agoracore.core2.bean.FcrUserRole
import io.agora.agoracore.core2.utils.FcrHashUtil
import io.agora.core.common.helper.SPreferenceManager
import io.agora.core.common.http.app.AppRetrofitManager
import io.agora.core.common.http.app.HttpCallback
import io.agora.core.common.http.bean.HttpBaseRes
import io.agora.core.common.log.LogX
import io.agora.core.common.obs.FcrCallback
import io.agora.core.common.obs.FcrError
import io.agora.flexmeetingcoredemo.data.FcrUISceneConfig
import io.agora.flexmeetingcoredemo.data.ResponseThrowable
import io.agora.flexmeetingcoredemo.utils.FcrSceneUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @author chenbinhang@agora.io
 * @date 2024/11/19 18:48
 */
class FcrRoomViewModel : ViewModel() {
    companion object {
        private const val TAG = "FcrRoomViewModel"
    }

    private var coreEngineInit = false
    lateinit var coreEngine: FcrCoreEngine
    private val _joinRoomStatus = MutableLiveData<Result<Unit>>()
    val joinRoomStatus: LiveData<Result<Unit>> = _joinRoomStatus
    val leaveRoom = MutableLiveData<Boolean>()
    val waiting = MutableLiveData<Boolean>(false)
    var roomId: String = ""
    private fun createCoreEngine(context: Context, sceneConfig: FcrUISceneConfig): FcrCoreEngine {
        val config = FcrCoreEngineConfig(
            sceneConfig.appId,
            sceneConfig.token,
            sceneConfig.region,
            sceneConfig.userId,
            sceneConfig.dualCameraVideoStreamConfig,
            sceneConfig.parameters,
        )
        return FcrCoreEngineImpl(context.applicationContext, config)
    }

    /**
     * 免登录：创建房间并进入房间
     */
    fun createJoinRoom(context: Context, userName: String, roomName: String, userRole: FcrUserRole) {
        waiting.postValue(true)
        val startTime = System.currentTimeMillis()
        val endTime = startTime + 60 * 1000 * 60 * 24                // 24 hour
        val userId = genUserId(userName)
        val req = FcrSceneCreateRoomReq(roomName, startTime, endTime, userId)

        val call = AppRetrofitManager.getService(FcrSceneRoomService::class.java).createRoom(req)
        AppRetrofitManager.Companion.exc(call, object : HttpCallback<HttpBaseRes<FcrSceneCreateRoomRes>>() {
            override fun onSuccess(result: HttpBaseRes<FcrSceneCreateRoomRes>?) {
                waiting.postValue(false)
                joinRoom(context, userId, result?.data?.roomId ?: "", userRole, userName)
            }

            override fun onError(httpCode: Int, code: Int, message: String?) {
                super.onError(httpCode, code, message)
                waiting.postValue(false)
                // code == 1101021 房间号不存在
                val throwable = ResponseThrowable(code, message)
                _joinRoomStatus.value = Result.failure(throwable)
            }
        })
    }

    private fun genUserId(userName: String): String {
        return FcrHashUtil.md5(userName + FcrSceneUtils.getDeviceId()).lowercase()
    }

    fun joinRoom(
        context: Context, userId: String? = null, roomId: String, userRole: FcrUserRole, userName: String
    ) {
        waiting.postValue(true)
        val userUuid = if (TextUtils.isEmpty(userId)) {
            genUserId(userName)
        } else {
            userId!!
        }
        // 1、get token & appid
        val req = FcrSceneJoinRoomReq(roomId, userRole.value, userUuid, userName)
        val call = AppRetrofitManager.getService(FcrSceneRoomService::class.java).joinRoom(req)
        AppRetrofitManager.Companion.exc(call, object : HttpCallback<HttpBaseRes<FcrSceneJoinRoomRes>>() {
            override fun onSuccess(result: HttpBaseRes<FcrSceneJoinRoomRes>?) {
                result?.data?.let {
                    SPreferenceManager.put("fcr_meeting_room_id", roomId)
                    SPreferenceManager.put("fcr_meeting_user_name", userName)
                    val config = FcrUISceneConfig(
                        it.appId, it.token, roomId, userUuid, userName, userRole
                    )
                    val creatorConfig = FcrUISceneCreatorConfig(it.appId, userUuid)
                    FcrHttpHelper.initHttp(creatorConfig, it.token)
                    coreEngine = createCoreEngine(context, config)
                    coreEngine.login(object : FcrCallback<Any> {
                        override fun onSuccess(res: Any?) {
                            viewModelScope.launch(Dispatchers.Main) {
                                LogX.i(TAG, "coreEngine login success ${Thread.currentThread()}")
                                val createConfig = FcrRoomControlCreateConfig(roomId, FcrRoomType.MAIN_ROOM)
                                val roomControl = coreEngine.createRoomControl(createConfig)
                                if (roomControl != null) {
                                    val options = FcrRoomJoinOptions(userName, userRole, null, it.token)
                                    roomControl.join(options, object : FcrCallback<Unit> {
                                        override fun onSuccess(res: Unit?) {
                                            waiting.postValue(false)
                                            coreEngineInit = true
                                            this@FcrRoomViewModel.roomId = roomId
                                            _joinRoomStatus.value = Result.success(Unit)
                                            LogX.i(TAG, "createRoomControl success   ${Thread.currentThread()}")
                                        }

                                        override fun onFailure(error: FcrError) {
                                            super.onFailure(error)
                                            waiting.postValue(false)
                                            LogX.i(TAG, "createRoomControl onFailure   ${Thread.currentThread()}")
                                        }
                                    })

                                } else {
                                    LogX.i(TAG, "createRoomControl onFailure   ${Thread.currentThread()}")
                                }
                            }
                        }

                        override fun onFailure(error: FcrError) {
                            super.onFailure(error)
                            waiting.postValue(false)
                            LogX.i(TAG, "coreEngine login  onFailure $error")
                        }
                    })
                }
            }

            override fun onError(httpCode: Int, code: Int, message: String?) {
                super.onError(httpCode, code, message)
                waiting.postValue(false)
                // code == 1101021 房间号不存在
                val throwable = ResponseThrowable(code, message)
                _joinRoomStatus.value = Result.failure(throwable)
            }
        })
    }


    override fun onCleared() {
        super.onCleared()
        if (coreEngineInit) {
            coreEngine.release()
        }
    }
}
