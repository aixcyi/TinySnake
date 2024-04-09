package cn.aixcyi.plugin.tinysnake

import com.intellij.icons.AllIcons
import com.intellij.ui.IconManager
import com.intellij.ui.LayeredIcon
import javax.swing.Icon

/**
 * Python 相关图标。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 * @see <a href="https://jetbrains.design/intellij/resources/icons_list/">Icons List</a>
 * @see <a href="https://www.jetbrains.com/help/pycharm/symbols.html#common-icons">Visibility modifiers</a>
 */
object AppIcons {

    private fun load(path: String, cacheKey: Int, flags: Int): Icon {
        return IconManager.getInstance().loadRasterizedIcon(
            path,
            AppIcons::class.java.classLoader, cacheKey, flags
        )
    }

    private fun loads(vararg icons: Icon): Icon {
        val icon = LayeredIcon(icons.size)
        for (i in icons.indices) {
            icon.setIcon(icons[i], i)
        }
        return icon
    }

    // 不是所有以双下划线开头的变量都是特殊变量，属性、函数同理。
    object Nodes {

        /**
         * 16x16
         */
        val CyanDot: Icon = load("icons/com/jetbrains/python/psi/nodes/cyan-dot.svg", 77524255, 0)

        /**
         * 16x16。特殊变量（它们都以双下划线开头）。
         */
        val Variable: Icon = loads(AllIcons.Nodes.Variable, CyanDot)
    }
}