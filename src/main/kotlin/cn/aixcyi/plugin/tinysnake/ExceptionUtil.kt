package cn.aixcyi.plugin.tinysnake

import com.intellij.openapi.diagnostic.logger

val LOGGER = logger<ExceptionUtil>()

object ExceptionUtil

/**
 * 尝试调用对象的方法。
 *
 * @param block 对对象方法的调用。
 * @return 抛出异常时捕获之，并返回对象自身；否则返回 `null` 。
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
inline fun <T, R> T.runTry(block: T.() -> R): T? {
    try {
        block()
        return null
    } catch (e: Throwable) {
        LOGGER.debug(e)
        return this
    }
}