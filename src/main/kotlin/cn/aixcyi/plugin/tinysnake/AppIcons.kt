package cn.aixcyi.plugin.tinysnake

import com.intellij.icons.AllIcons
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.LayeredIcon
import javax.swing.Icon

// 必须 @JvmField 和 @JvmStatic，
// 否则 plugin.xml／idea-plugin／actions／group.icon 无法读取，
// 会报
// java.lang.IllegalAccessException: member is private: cn.aixcyi.plugin.tinysnake.AppIcons.String/javax.swing.Icon/getStatic, from class com.intellij.openapi.util.IconLoader (……)
// com.intellij.diagnostic.PluginException: Icon cannot be found in 'cn.aixcyi.plugin.tinysnake.AppIcons.String', action 'cn.aixcyi.plugin.tinysnake.action.DocstringGroup' (……))

/**
 * Python 相关图标。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 * @see <a href="https://jetbrains.design/intellij/resources/icons_list/">Icons List</a>
 * @see <a href="https://www.jetbrains.com/help/pycharm/symbols.html#common-icons">Visibility modifiers</a>
 */
object AppIcons {

    // 不是所有以双下划线开头的变量都是特殊变量，属性、函数同理。

    /** 16x16，PythonPsiApiIcons／icons／com／jetbrains／python／nodes／cyan dot */
    @JvmField
    val CyanDot: Icon = load("/icons/cyan-dot.svg")

    /** 16x16。特殊变量（它们都以双下划线开头）。*/
    @JvmField
    val Variable: Icon = loads(AllIcons.Nodes.Variable, CyanDot)

    /** 16x16，DatabaseIcons／icons／string */
    @JvmField
    val Docstring: Icon = load("/icons/string.svg")

    /** 16x16，MarkdownIcons／icons／editor_actions／Link */
    @JvmField
    val Hyperlink: Icon = load("/icons/Link.svg")

    /** 16x16，MarkdownIcons／icons／editor_actions／Bold */
    @JvmField
    val Bold: Icon = load("/icons/Bold.svg")

    /** 16x16，MarkdownIcons／icons／editor_actions／Italic */
    @JvmField
    val Italic: Icon = load("/icons/Italic.svg")

    /**
     * 载入包内的图标。
     */
    @JvmStatic
    private fun load(path: String) = IconLoader.getIcon(path, AppIcons::class.java.classLoader)

    /**
     * 将多个图标层叠成一个图标。
     */
    @JvmStatic
    private fun loads(vararg icons: Icon) = LayeredIcon(icons.size).also {
        icons.forEachIndexed { layout, icon -> it.setIcon(icon, layout) }
    }
}