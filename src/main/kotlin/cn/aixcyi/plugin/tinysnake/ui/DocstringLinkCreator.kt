package cn.aixcyi.plugin.tinysnake.ui

import cn.aixcyi.plugin.tinysnake.Zoo.message
import cn.aixcyi.plugin.tinysnake.util.isURL
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.ui.validation.DialogValidation
import com.intellij.ui.dsl.builder.LabelPosition
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.text
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor

/**
 * 文档字符串 [docstring](https://docs.python.org/zh-cn/3/glossary.html#term-docstring) 超链接编辑窗口。
 *
 * - 如果剪贴板中有 `http://` 或 `https://` 开头的字符串，并且链接字段为空，则自动填入。
 * - 如果剪贴板中有文本，并且标题字段为空，则自动填入。
 * - 自动聚焦内容为空的字段（优先聚焦标题字段）。
 * - 在字段都不为空时，自动聚焦内容来自剪贴板的字段。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 * @see <a href="https://peps.python.org/pep-0257/">PEP 257 – Docstring Conventions</a>
 */
class DocstringLinkCreator(
    project: Project,
    private var text: String = "",
    private var link: String = "",
) : DialogWrapper(project, true) {

    private var isFocusText: Boolean = text.isEmpty()

    /**
     * 注意，如果链接不在 docstring 的开头或结尾（也就是紧邻 `"""`）的话，那么需要与空格相邻，否则 PyCharm 无法正确渲染。
     */
    val docstring
        get() = "`$text <$link>`_"

    init {
        // 从剪贴板中获取文本，如果是 URL 则放入链接字段，否则放入标题字段，但避免覆盖原有内容。
        val copies = try {
            Toolkit.getDefaultToolkit().systemClipboard.getData(DataFlavor.stringFlavor) as String
        } catch (_: Exception) {
            ""
        }.ifBlank {
            ""
        }
        // 首先，如果任一字段为空，则空的字段优先获得焦点；
        // 其次，如果字段均非空，则来源于剪贴板的字段获得焦点；
        // 这一逻辑遵循 “显式优先于隐式” 原则，因为用户一般希望编辑非显式内容。
        if (copies.isNotBlank())
            if (link.isBlank() && copies.isURL("http", "https")) {
                link = copies
                isFocusText = false
            } else if (text.isBlank()) {
                text = copies
                isFocusText = true
            }
        if (text.isBlank() || link.isBlank()) {
            isFocusText = text.isEmpty()
        }

        // 一般的窗口初始化逻辑：
        isResizable = true
        title = message("dialog.DocstringLinkCreator.title")
        setSize(600, -1)  // 实在找不到什么方法可以设置一个相对于父窗口的宽度
        setOKButtonText(message("button.OK.text"))
        setCancelButtonText(message("button.Cancel.text"))
        super.init()
    }

    override fun createCenterPanel() = panel {
        row {
            textField()
                .hFill()
                .label(message("label.DocstringLinkText.text"), LabelPosition.TOP)
                .text(text)
                .bindText(::text)
                .apply { if (isFocusText) focused() }
                .validate {
                    DialogValidation {
                        if (this@validate.text.isEmpty())
                            ValidationInfo(message("validation.EmptyField")).asWarning()
                        else
                            null
                    }
                }
        }
        row {
            textField()
                .hFill()
                .label(message("label.DocstringLinkSource.text"), LabelPosition.TOP)
                .text(link)
                .bindText(::link)
                .apply { if (!isFocusText) focused() }
                .validate {
                    DialogValidation {
                        if (this@validate.text.isNotBlank() && this@validate.text.isURL("http", "https"))
                            null
                        else
                            ValidationInfo(message("validation.NotAHyperlink")).asWarning()
                    }
                }
        }
    }
}