package cn.aixcyi.plugin.tinysnake.util

import cn.aixcyi.plugin.tinysnake.Zoo
import com.intellij.DynamicBundle
import com.intellij.psi.util.QualifiedName
import org.jetbrains.annotations.PropertyKey
import java.nio.file.Path
import java.util.*
import kotlin.io.path.div

/**
 * I/O 相关工具。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
object IOUtil {

    /** 国际化文本资源包。 */
    private val BUNDLE = ResourceBundle.getBundle("messages.TinySnakeBundle", DynamicBundle.getLocale())

    /**
     * 获取本地化翻译。
     *
     * Retrieve translated text via keys on "properties" file.
     *
     * @param key properties 文件中的键。
     * @return properties 文件中键对应的值。
     */
    @JvmStatic
    fun message(@PropertyKey(resourceBundle = "messages.TinySnakeBundle") key: String): String = BUNDLE.getString(key)

    /**
     * 获取资源文件。
     *
     * 比如获取 ./src/main/resources/index.html 则应调用 `resource("/index.html")` 。
     *
     * @param path 资源文件路径。
     * @return 资源文件。
     */
    fun resource(path: String) = Zoo::class.java.getResource(path)!!
}

/** 允许 [Path] 拼接一个 [QualifiedName]。 */
operator fun Path.div(module: QualifiedName): Path {
    var path = this
    for (component in module.components)
        path /= component
    return path
}