package cn.shengwang.scene.helper

import io.agora.core.common.http.core.FcrDomainManager
import io.agora.core.common.http.interceptor.FcrHttpHeaderManager
import cn.shengwang.scene.FcrUISceneCreatorConfig
import io.agora.core.common.http.utils.FcrHttpEnvUtils
import io.agora.core.common.utils.CommonConstants

/**
 * author : felix
 * date : 2024/3/5
 * description :
 */
object FcrHttpHelper {

    fun initHttp(config: FcrUISceneCreatorConfig, roomToken: String) {
        setEnv(config)
        FcrDomainManager.initDomain(config.region)
        setHttpHeader(roomToken)
    }

    private fun setEnv(config: FcrUISceneCreatorConfig) {
        //切环境
        val core = config.parameters?.containsKey(CommonConstants.KEY_CORE) as? Map<String, Any>
        if (core != null && core.containsKey(CommonConstants.KEY_ENVIRONMENT)) {
            val httpEnv = CommonConstants.environmentMap[core[CommonConstants.KEY_ENVIRONMENT] as? String]
            FcrHttpEnvUtils.setEnv(httpEnv)
        }
    }

    private fun setHttpHeader(roomToken: String) {
        val map = mutableMapOf<String, String>()
        map[FcrHttpHeaderManager.HEADER_TOKEN007] = FcrHttpHeaderManager.getToken(roomToken)
        FcrHttpHeaderManager.setAllHeader(map)
    }
}