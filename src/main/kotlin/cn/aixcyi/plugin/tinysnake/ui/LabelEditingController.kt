package cn.aixcyi.plugin.tinysnake.ui

import java.awt.CardLayout
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

/**
 * 标签文本编辑控制器。
 *
 * - 左键双击标签即可进入编辑状态。左键双击的判定是连续点击数超过 `1`，也就是连续三击甚至更多也是可以的，这主要是避免鼠标不灵敏导致错过判定。
 * - 编辑框失去焦点即可退出编辑状态。
 *
 * @param label 标签。用于展示文本。
 * @param field 文本框。用于编辑文本。
 * @param iv 初值。当编辑框内容为空时还原到最初的值（而非最后一次确认的值），该操作优先于 [validator]。提供 `null` 则跳过此操作。
 * @param validator 校验器。编辑框内容是否可以被展示到标签上。
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
class LabelEditingController(
    private val label: JLabel,
    private val field: JTextField,
    private val iv: String? = null,
    private val validator: ((String) -> Boolean) = { it.isNotEmpty() },
) {

    companion object {
        /**
         * 从一个 [JPanel] 中获取标签和编辑框，并构造控制器。
         *
         * @param card 组件数量必须是 2，且布局必须是 [CardLayout]，否则会触发断言。
         * @param iv 初值。默认为 `null`，见 [LabelEditingController.iv]。
         * @param validator 校验器，默认为 [String.isNotEmpty]。见 [LabelEditingController.validator]。
         */
        @JvmStatic
        fun of(
            card: JPanel,
            iv: String? = null,
            validator: (String) -> Boolean = { it.isNotEmpty() },
        ): LabelEditingController {
            assert(card.layout is CardLayout)
            assert(card.componentCount == 2)
            return LabelEditingController(
                card.components[0] as JLabel,
                card.components[1] as JTextField,
                iv,
                validator,
            )
        }
    }

    /**
     * 标签文本。
     *
     * - 不会得到未编辑完成的文本。即：在标签标题与编辑框文本不同的情况下，仅返回标签标题。
     * - 设置后，会覆盖编辑框内原有的文本。
     */
    var text: String
        get() = label.text
        set(value) {
            this.label.text = value
            this.field.text = value
        }

    init {
        display()
        label.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                // 「左键双击」时进入编辑状态
                if (e.component !is JLabel || e.button != MouseEvent.BUTTON1 || e.clickCount < 2) return
                edit()
                field.caretPosition = field.text.length
                field.selectAll()
                field.requestFocusInWindow()
            }
        })
        field.addFocusListener(object : FocusAdapter() {
            // 「失去焦点」时退出编辑状态
            override fun focusLost(e: FocusEvent) {
                display()
            }
        })
    }

    /**
     * 切换到编辑状态。
     */
    private fun edit() {
        field.text = label.text
        field.isVisible = true
        label.isVisible = false
    }

    /**
     * 退出编辑状态。
     */
    private fun display() {
        if (field.text.isEmpty() && iv != null)
            label.text = iv
        else if (validator(field.text))
            label.text = field.text
        label.isVisible = true
        field.isVisible = false
    }
}