package io.agora.flexmeetingcoredemo.utils

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.os.LocaleList
import io.agora.core.common.helper.SPreferenceManager
import io.agora.core.common.log.LogX
import java.util.Locale
import java.util.UUID

/**
 * author : felix
 * date : 2024/4/23
 * description :
 */
object FcrSceneUtils {
    /**
     * 获取设备id
     */
    fun getDeviceId(): String {
        var deviceId = SPreferenceManager.get(FcrSceneConstants.KEY_DEVICE_ID, "")
        if (deviceId.isNotEmpty()) {
            return deviceId
        }
        if (deviceId.isNullOrEmpty()) {
            deviceId = UUID.randomUUID().toString()
        }
        SPreferenceManager.put(FcrSceneConstants.KEY_DEVICE_ID, deviceId)
        return deviceId
    }
}