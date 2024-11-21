package io.agora.flexmeetingcoredemo.data

/**
 * @author chenbinhang@agora.io
 * @date 2024/11/21 10:18
 */
class ResponseThrowable(val code:Int, override val message:String?): Throwable()