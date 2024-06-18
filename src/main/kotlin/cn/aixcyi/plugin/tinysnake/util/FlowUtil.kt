package cn.aixcyi.plugin.tinysnake.util

import com.intellij.openapi.diagnostic.thisLogger

/**
 * 流程控制相关工具。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
object FlowUtil {
    val LOGGER = thisLogger()
}

/**
 * `try` 的链式执行。
 *
 * 抛出异常时返回对象自身，否则返回 `null`。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
inline fun <T> T.chainTry(block: T.() -> Unit): T? {
    try {
        block()
        return null
    } catch (e: Throwable) {
        FlowUtil.LOGGER.debug(e)
        return this
    }
}