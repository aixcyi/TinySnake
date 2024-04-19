package cn.aixcyi.plugin.tinysnake

/**
 * 字符串相关工具。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
object StringUtil {

    /**
     * `"snake_to_upper_camel"`　→　`"SnakeToUpperCamel"`
     */
    @JvmStatic
    fun snakeToUpperCamel(s: String?): String {
        if (s.isNullOrEmpty()) return ""
        return s.split('_').joinToString("") { it.replaceFirstChar { c -> c.uppercase() } }
    }
}

/**
 * 去除字符串的结尾（如果有的话）。
 *
 * @param tail 结尾字符串。
 * @param ignoreCase 判断结尾时是否忽略大小写。
 * @return 去除结尾后的新的字符串，或者原字符串自身。
 */
fun String.tailless(tail: String, ignoreCase: Boolean = false): String {
    return if (tail.isEmpty())
        this
    else if (this.endsWith(tail, ignoreCase = ignoreCase))
    // Unresolved reference: ..<
        this.substring(0..this.length - tail.length - 1)
    else
        this
}