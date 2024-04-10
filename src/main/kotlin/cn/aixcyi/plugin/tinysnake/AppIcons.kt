package cn.aixcyi.plugin.tinysnake

import com.intellij.icons.AllIcons
import com.intellij.openapi.util.IconLoader
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

    /**
     * 载入包内的图标。
     */
    private fun load(path: String): Icon {
        return IconLoader.getIcon(path, AppIcons::class.java.classLoader)
    }

    /**
     * 将多个图标层叠成一个图标。
     */
    private fun loads(vararg icons: Icon): Icon {
        val icon = LayeredIcon(icons.size)
        for (i in icons.indices) {
            icon.setIcon(icons[i], i)
        }
        return icon
    }

    // 不是所有以双下划线开头的变量都是特殊变量，属性、函数同理。

    /**
     * 16x16
     */
    val CyanDot: Icon = load("/icons/cyan-dot.svg")

    /**
     * 16x16。特殊变量（它们都以双下划线开头）。
     */
    val Variable: Icon = loads(AllIcons.Nodes.Variable, CyanDot)
}