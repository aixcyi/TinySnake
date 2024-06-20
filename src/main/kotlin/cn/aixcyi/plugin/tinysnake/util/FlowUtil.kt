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
 * [apply] 的变体。
 *
 * - 捕获 [Throwable] 后返回对象自身，以便链式调用继续执行；
 *   未捕获任何异常时返回 `null`，终止链式调用。
 * - 该函数侧重于上下文（也就是对象自身）的传递，无法返回 [block] 的返回值。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
inline fun <T> T.exec(block: T.() -> Unit): T? {
    try {
        block()
        return null
    } catch (e: Throwable) {
        FlowUtil.LOGGER.debug(e)
        return this
    }
}

/**
 * 无接收器的 [run] 的变体。
 *
 * - 捕获 [Throwable] 后返回 `null`，未捕获任何异常时返回 [block] 的返回值。
 * - 该函数侧重于评估 [block] 的值，而为了将异常传递出去，[block] 只能够返回非空值。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
inline fun <R> eval(block: () -> R): R? {
    try {
        return block()
    } catch (e: Throwable) {
        FlowUtil.LOGGER.debug(e)
        return null
    }
}