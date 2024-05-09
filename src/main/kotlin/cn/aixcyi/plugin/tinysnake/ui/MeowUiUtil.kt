package cn.aixcyi.plugin.tinysnake.ui

import cn.aixcyi.plugin.tinysnake.chainTry
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.validation.DialogValidation
import com.intellij.openapi.util.text.TextWithMnemonic
import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.dsl.builder.Cell
import org.jetbrains.annotations.ApiStatus
import javax.swing.*

/**
 * 图形界面相关的工具函数。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
object MeowUiUtil {

    /**
     * 检测是否启用了 [NewUI](https://www.jetbrains.com/help/idea/new-ui.html) 。
     *
     * - 本方法适用于任意版本的 IntelliJ 平台。
     * - 232.5150 开始有 `com.intellij.ui.NewUI.isEnabled`。
     * - 213.2094 开始有 `com.intellij.ui.ExperimentalUI.isNewUI`，属于 [ApiStatus.Internal]。
     */
    fun isUsingNewUI(): Boolean {
        try {
            // @since 232.5150.116
            // https://github.com/JetBrains/intellij-community/commit/ba6df8d944aa1f080d555223917c4b0aa7f43a26
            // return com.intellij.ui.NewUI.isEnabled()
            val ret = Class.forName("com.intellij.ui.NewUI").getMethod("isEnabled").invoke(null)
            return ret as Boolean
        } catch (e: Throwable) {
            // 喵
        }
        try {
            // @since 213.2094
            // https://github.com/JetBrains/intellij-community/blob/213.2094/platform/platform-api/src/com/intellij/ui/ExperimentalUI.java
            // return com.intellij.ui.ExperimentalUI.isNewUI()
            val ret = Class.forName("com.intellij.ui.ExperimentalUI").getMethod("isNewUI").invoke(null)
            return ret as Boolean
        } catch (e: Throwable) {
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

/**
 * 自动解析 Kotlin UI DSL [Cell] 包装的组件的文本，并设置助记键。
 */
fun <T : JComponent> Cell<T>.mnemonic(): Cell<T> {
    when (val component = this.component) {
        // 按钮、单选框、复选框
        is AbstractButton -> {
            val text = TextWithMnemonic.parse(component.text)
            if (text.hasMnemonic()) {
                component.mnemonic = text.mnemonicCode
                component.text = text.text
            }
        }
        // 标签
        is JLabel -> {
            val text = TextWithMnemonic.parse(component.text)
            if (text.hasMnemonic()) {
                component.setDisplayedMnemonic(text.mnemonicChar)
                component.text = text.text
            }
        }
        // 其它组件
        else -> {}
    }
    return this
}

/**
 * 组件内部条件 `condition()` 判定为 `true` 则请求获取焦点。
 */
fun <T : JComponent> Cell<T>.focusIf(condition: T.() -> Boolean): Cell<T> {
    if (condition.invoke(this.component)) this.focused()
    return this
}

/**
 * 外部条件 `condition` 判定为 `true` 则请求获取焦点。
 */
fun <T : JComponent> Cell<T>.focusIf(condition: Boolean): Cell<T> {
    if (condition) this.focused()
    return this
}

/**
 * 让 [Cell] 填满当前的布局单元格（如果不经设置，会默认为左对齐）。
 */
fun <T : JComponent> Cell<T>.hFill(): Cell<T> {
    // 兼容以下两种写法：
    // align(com.intellij.ui.dsl.builder.AlignX.FILL)
    // horizontalAlign(com.intellij.ui.dsl.gridLayout.HorizontalAlign.FILL)
    javaClass.chainTry {
        val klass = Class.forName("com.intellij.ui.dsl.builder.Align")
        val param = Class.forName("com.intellij.ui.dsl.builder.AlignX")
            .kotlin.sealedSubclasses.first { it.simpleName == "FILL" }.objectInstance
        getMethod("align", klass).invoke(this@hFill, param)
    }?.chainTry {
        val klass = Class.forName("com.intellij.ui.dsl.gridLayout.HorizontalAlign")
        val param = klass.enumConstants.map { it as Enum<*> }.first { it.name == "FILL" }
        getMethod("horizontalAlign", klass).invoke(this@hFill, param)
    }
    return this
}

/**
 * Registers custom component data validations.
 * [block] will be called on [Cell.validationRequestor] events and
 * when [DialogPanel.apply] event is happens.
 */
fun <T : JComponent> Cell<T>.validate(block: T.() -> DialogValidation?): Cell<T> {
    block(component)?.let { validation(it) }
    return this
}