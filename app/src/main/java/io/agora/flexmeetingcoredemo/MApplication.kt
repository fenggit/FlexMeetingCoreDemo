package io.agora.flexmeetingcoredemo

import android.app.Application
import io.agora.agoracore.core2.utils.FcrSDKInitUtils
import io.agora.core.common.helper.SPreferenceManager

/**
 * @author chenbinhang@agora.io
 * @date 2024/11/21 10:38
 */
class MApplication :Application() {
    override fun onCreate() {
        super.onCreate()
        SPreferenceManager.init(this)
        FcrSDKInitUtils.initSDK(this)
    }
}