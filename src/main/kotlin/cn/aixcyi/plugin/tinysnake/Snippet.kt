package cn.aixcyi.plugin.tinysnake

import cn.aixcyi.plugin.tinysnake.storage.DunderAllOptimization

/**
 * Python 代码片段构造工具。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
object Snippet {

    /**
     * 构造 Python list 的字面值。
     *
     * @param items list 的元素。不用也不能预先添加引号。
     * @param state 格式化状态。
     * @return 字面值所对应的字符串。
     */
    fun makeStringList(
        items: List<String>,
        state: DunderAllOptimization.State = DunderAllOptimization.getInstance().state
    ): String {
        val ends = if (state.isEndsWithComma) "," else ""
        val head = if (state.isLineByLine) "[\n" else "["
        val tail = if (state.isLineByLine) "\n]" else "]"
        val body = items.joinToString(
            separator = if (state.isLineByLine) ",\n" else ", ",
            postfix = if (state.isUseSingleQuote) "'" else "\"",
            prefix = if (state.isUseSingleQuote) "'" else "\"",
        )
        //      ["meow", "doge", "woof",     ]
        // (head)(--------body--------)(ends)(tail)
        return head + body + ends + tail
    }
}