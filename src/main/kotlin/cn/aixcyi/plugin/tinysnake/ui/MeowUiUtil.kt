package cn.aixcyi.plugin.tinysnake.ui

import com.intellij.ui.IdeBorderFactory
import org.jetbrains.annotations.ApiStatus
import javax.swing.BoxLayout
import javax.swing.JPanel

/**
 * 图形界面相关的工具函数。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
object MeowUiUtil {

    /**
     * 检测是否启用了 [NewUI](https://www.jetbrains.com/help/idea/new-ui.html) 。
     *
     * 之前唯一的检测接口 [com.intellij.ui.ExperimentalUI.isNewUI] 属于 [ApiStatus.Internal]，
     * idea/232.5150.116 开始才有二次封装的公开接口 [com.intellij.ui.NewUI.isEnabled]，所以不得不再次封装来避免兼容性问题。
     */
    fun isUsingNewUI(): Boolean {
        try {
            // since: idea/232.5150.116
            // https://github.com/JetBrains/intellij-community/commit/ba6df8d944aa1f080d555223917c4b0aa7f43a26
            val clazz = Class.forName("com.intellij.ui.NewUI")
            val method = clazz.getMethod("isEnabled")
            return method.invoke(null) as Boolean
            // return com.intellij.ui.NewUI.isEnabled();
        } catch (ignored: ReflectiveOperationException) {
            // 喵
        }
        try {
            val clazz = Class.forName("com.intellij.ui.ExperimentalUI")
            val method = clazz.getMethod("isNewUI")
            return method.invoke(null) as Boolean
            // return com.intellij.ui.ExperimentalUI.isNewUI();
        } catch (ignored: ReflectiveOperationException) {
            // 喵
        }
        return false
    }

    /**
     * 创建一个带有标题和分割线的 [JPanel] ，常用于设置界面创建分组。
     *
     * @param title 标题。提供 `null` 的话会绘制一条从左到右的分割线，提供字符串则从字符串末尾开始绘制。
     * @return 被修饰过的 [JPanel] ，放置在里面的组件会自动添加左侧缩进。
     */
    fun createTitledPanel(title: String?): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.border = IdeBorderFactory.createTitledBorder(title)
        return panel
    }
}