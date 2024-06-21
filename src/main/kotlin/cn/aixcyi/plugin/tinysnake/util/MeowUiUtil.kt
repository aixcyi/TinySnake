package cn.aixcyi.plugin.tinysnake.util

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.validation.DialogValidation
import com.intellij.openapi.util.text.TextWithMnemonic
import com.intellij.ui.AnActionButton
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.dsl.builder.Cell
import javax.swing.AbstractButton
import javax.swing.JComponent
import javax.swing.JLabel

/**
 * 图形界面相关工具。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
object MeowUiUtil

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
 * 让 [Cell] 填满当前的布局单元格（如果不经设置，会默认为左对齐）。
 */
fun <T : JComponent> Cell<T>.hFill(): Cell<T> {
    // 兼容以下两种写法：
    // align(com.intellij.ui.dsl.builder.AlignX.FILL)
    // horizontalAlign(com.intellij.ui.dsl.gridLayout.HorizontalAlign.FILL)
    exec {
        val klass = Class.forName("com.intellij.ui.dsl.builder.Align")
        val param = Class.forName("com.intellij.ui.dsl.builder.AlignX")
            .kotlin.sealedSubclasses.first { it.simpleName == "FILL" }.objectInstance
        javaClass.getMethod("align", klass).invoke(this, param)
    }?.exec {
        val klass = Class.forName("com.intellij.ui.dsl.gridLayout.HorizontalAlign")
        val param = klass.enumConstants.map { it as Enum<*> }.first { it.name == "FILL" }
        javaClass.getMethod("horizontalAlign", klass).invoke(this, param)
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

/**
 * 兼容旧版本的 `ToolbarDecorator.addExtraAction(AnActionButton)`
 * 和新版本的 `ToolbarDecorator.addExtraAction(AnAction)` 。
 *
 * @see [ToolbarDecorator.addExtraAction]
 */
fun ToolbarDecorator.putExtraAction(action: AnAction): ToolbarDecorator {
    exec {
        javaClass.getMethod("addExtraAction", AnAction::class.java).invoke(this, action)
    }?.exec {
        javaClass.getMethod("addExtraAction", AnActionButton::class.java).invoke(this, action as AnActionButton)
    }?.apply {
        throw NoSuchMethodException("ToolbarDecorator#addExtraAction was not found!")
    }
    return this
}