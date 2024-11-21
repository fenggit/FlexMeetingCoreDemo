package cn.shengwang.http.bean

/**
 * author : felix
 * date : 2024/4/15
 * description :
 */
data class FcrSceneCreateRoomReq(
    var roomName: String,
    var startTime: Long,
    var endTime: Long,
    var creatorUuid: String? = null, // 创建人id , 也和进入房间是同一个人
    var roomProperties: Map<String, String>? = null,
    var sceneType: Int = 4
)

