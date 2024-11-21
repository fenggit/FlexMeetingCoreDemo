package cn.shengwang.scene.helper

import cn.shengwang.scene.FcrUIExitReason
import io.agora.core.common.bus.FcrMessage
import io.agora.core.common.bus.FcrMessageBus
import io.agora.flexmeetingcoredemo.utils.FcrSceneConstants

/**
 * author : felix
 * date : 2024/9/23
 * description :
 */
object FcrUISceneHelper {
    fun exit(reason: FcrUIExitReason = FcrUIExitReason.LEAVEROOM) {
        FcrMessageBus.getDefault().post(FcrMessage(FcrSceneConstants.MSG_ID_ROOM_FINISH).apply {
            dataObj = reason
        })
    }
}