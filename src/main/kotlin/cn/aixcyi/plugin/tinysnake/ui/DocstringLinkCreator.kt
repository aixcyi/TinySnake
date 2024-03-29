package cn.aixcyi.plugin.tinysnake.ui

import com.intellij.openapi.ui.DialogWrapper
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField

/**
 * Docstring 超链接生成窗口。
 *
 * @param windowTitle 窗口标题。
 */
class DocstringLinkCreator(windowTitle: String) : DialogWrapper(true) {
    private var contentPane: JPanel? = null
    private var textField: JTextField? = null
    private var linkField: JTextField? = null

    init {
        isResizable = true
        title = windowTitle
        init()
    }

    /**
     * 设置锚文本。
     */
    fun setText(text: String): DocstringLinkCreator {
        textField!!.text = text
        return this
    }

    /**
     * 设置超链接。
     */
    fun setLink(link: String): DocstringLinkCreator {
        linkField!!.text = link
        return this
    }

    /**
     * 弹出窗口让用户编辑，并在确认后返回可以放在 docstring 中展示的超链接代码。
     *
     * @return 如果用户取消编辑，将返回 `null` 。
     */
    fun showThenGet(): String? {
        // 前后都额外加一个空格，是为了避免用户没有添加空格，导致渲染失败。
        return if (!showAndGet()) return null
        else " `${textField!!.text} <${linkField!!.text}>`_ "
    }

    override fun createCenterPanel(): JComponent {
        val dialogPanel = JPanel(BorderLayout())
        dialogPanel.add(contentPane!!, BorderLayout.CENTER)
        return dialogPanel
    }
}