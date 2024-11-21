package io.agora.flexmeetingcoredemo.utils

object FcrSceneConstants {


    const val KEY_DEVICE_ID: String = "key_device_id"

    /**
     * 视窗fragment的tag名称
     */
    const val TAG_ROOM_WINDOW: String = "tag_room_window"

    /**
     * 新增屏幕共享消息通知
     */
    const val MSG_ID_NEW_SHARING: String = "msg_new_sharing"

    /**
     * 屏幕共享关闭通知
     */
    const val MSG_ID_SHARING_CLOSED: String = "msg_sharing_closed"

    /**
     * 滑动到大小窗的消息
     */
    const val MSG_ID_SCROLL_SPEAKER_PAGE: String = "msg_id_scroll_speaker_page"

    /**
     * 关闭屏幕分享提示
     */
    const val MSG_ID_SHARE_SNACK_CLOSE: String = "msg_id_share_snack_close"

    /**
     * 关闭房间通知
     */
    const val MSG_ID_ROOM_FINISH: String = "msg_id_room_finish"

    /**
     * 唤起聊天页面
     */
    const val MSG_ID_ROOM_CHAT_CLICK: String = "msg_id_room_chat_click"

    /**
     * 会议悬浮布局控制
     */
    const val MSG_ID_FLOAT_LAYOUT_OPT: String = "msg_id_float_layout_opt"

    /**
     * 会议悬浮布局控制
     */
    const val MSG_ID_WINDOW_RELOAD: String = "msg_id_window_reload"

    /**
     * 开启白板通知
     */
    const val MSG_ID_BOARD_OPEN: String = "msg_id_board_open"
    const val CHAT_PRIVATE_SEND_ALL = "All"
    const val ROOM_BIG_VIDEO_CHANGED = "room_big_video_changed"

    /**
     * 耳机或者外接设备变更
     */
    const val MSG_ID_AUDIO_ROUTE_CHANGED: String = "msg_id_audio_route_changed"

    /**
     * 会议中的用户数目变更
     */
    const val MSG_ROOM_USER_COUNT_CHANGED: String = "msg_room_user_count_changed"

    const val REFRESH_INTERVAL = 1000L

    /**
     * 语言设置
     */
    const val LOCALE_LANGUAGE = "locale_language"

    /**
     * 地区设置
     */
    const val LOCALE_AREA = "locale_country"

    /**
     * 主持人锁定会议，其他参会者加入时后台返回的code
     */
    const val CODE_LOCKED_MEETING = 732403100

    /**
     * 关闭麦克风设备
     */
    const val MSG_ID_CLOSE_MIC_DEVICE: String = "msg_id_close_mic_device"


    /**
     * 关闭前台服务
     */
    const val MSG_ID_CLOSE_FOREGROUND_SERVICE: String = "msg_id_close_foreground_service"
}