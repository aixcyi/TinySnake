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
    const val DUNDER_ALL_OPTIMIZATION_NAME = "TinySnake.OptimizationDunderAll"

    /**
     * 国际化文本资源包。
     */
    private val BUNDLE: ResourceBundle = ResourceBundle.getBundle(
        "messages.TinySnakeBundle", DynamicBundle.getLocale()
    )

    /**
     * 获取本地化翻译。
     *
     * A utility function that provides translated message/text.
     *
     * @param key properties 文件中的键。
     * @return properties 文件中键对应的值。
     */
    @JvmStatic
    fun message(
        @PropertyKey(resourceBundle = "messages.TinySnakeBundle") key: String
    ): String {
        return BUNDLE.getString(key)
    }
}