package cn.shengwang.scene

import io.agora.core.common.http.bean.FcrRegion
import java.io.Serializable

data class FcrUISceneCreatorConfig(
    val appId: String,
    val userId: String,
    var parameters: Map<String, Any>? = null
) : Serializable {
    var region = FcrRegion.CN
}