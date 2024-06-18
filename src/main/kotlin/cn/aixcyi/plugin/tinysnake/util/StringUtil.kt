package cn.aixcyi.plugin.tinysnake.util

import java.net.MalformedURLException
import java.net.URL

/**
 * 字符串相关工具方法。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
object StringUtil {

    /**
     * `"snake_to_upper_camel"`　→　`"SnakeToUpperCamel"`
     */
    @JvmStatic
    fun snakeToUpperCamel(s: String) =
        s.split('_').joinToString("") { it.replaceFirstChar { c -> c.uppercase() } }

    /**
     * 将（以 [delimiter] 分隔）每个单词的开头的字母转换为大写。
     */
    @JvmStatic
    fun capitalize(s: String, delimiter: Char) =
        s.split(delimiter).joinToString(delimiter.toString(), transform = String::capitalize)
}

/**
 * 将首个字符转换为大写。
 */
fun String.capitalize() =
    if (this.isEmpty()) this else (this.substring(0, 1).uppercase() + this.substring(1))

/**
 * 去除字符串的结尾（如果有的话）。
 */
fun String.tailless(tail: String, ignoreCase: Boolean = false) =
    if (tail.isNotEmpty() && this.endsWith(tail, ignoreCase = ignoreCase))
        this.substring(0, this.length - tail.length)
    else
        this

/**
 * 判断字符串是不是 [URL]，并且使用了给定的协议（如果指定了的话）。
 */
fun String.isURL(vararg protocol: String) = try {
    if (protocol.isNotEmpty()) {
        protocol.contains(URL(this).protocol)
    } else {
        URL(this)
        true
    }
} catch (_: MalformedURLException) {
    false
}