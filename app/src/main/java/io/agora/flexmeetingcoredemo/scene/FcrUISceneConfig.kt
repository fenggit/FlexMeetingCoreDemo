package cn.shengwang.scene

import io.agora.agoracore.core2.bean.FcrUserRole
import java.io.Serializable

/**
 * author : felix
 * date : 2024/1/23
 * description :
 */
data class FcrUISceneConfig(
    var roomId: String,
    var roomToken: String,
    var userName: String,
    var userRole: FcrUserRole,
    var inviteLink: String // 分享链接
) : Serializable {
    /**
     * 资源文件；可修改该对象中的字段来修改资源文件链接
     */
    var resource: FcrUIResource? = null

    /**
     * 自定义用户属性，可基于需求带键值对；可作用于用户标签
     */
    var userProperties: Map<String, String>? = null

    //var region: String = FcrRegion.cn // 区域
    //var dualCameraVideoStreamConfig: FcrDualVideoStreamConfig? = null // 摄像头视频多流配置
    //var streamLatency = FcrStreamLatencyLevel.ULTRALOW
}

class FcrUIResource(
    var virtualBackgroundImages: Array<String>?,
    var virtualBackgroundVideos: Array<String>?
) : Serializable

