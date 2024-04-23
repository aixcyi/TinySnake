package cn.aixcyi.plugin.tinysnake

import com.intellij.DynamicBundle
import org.jetbrains.annotations.PropertyKey
import java.util.*

object Zoo {

    // 杂项
    const val PLUGIN_DISPLAY_NAME = "Tiny Snake"
    const val PLUGIN_LEVEL_STORAGE = "aixcyi.TinySnake.xml"

    // <component name="NAME"></component>
    const val PLUGIN_SETTINGS_NAME = "TinySnake.Settings"
    const val DJANGO_APP_CREATION_NAME = "TinySnake.DjangoAppCreation"
    const val DUNDER_ALL_OPTIMIZATION_NAME = "TinySnake.DunderAllOptimization"

    /** 国际化文本资源包。 */
    private val BUNDLE: ResourceBundle = ResourceBundle.getBundle(
        "messages.TinySnakeBundle", DynamicBundle.getLocale()
    )

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
    @JvmStatic
    fun resource(path: String) = Zoo::class.java.getResource(path)!!
}