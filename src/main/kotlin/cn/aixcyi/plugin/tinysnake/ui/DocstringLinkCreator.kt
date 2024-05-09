package cn.aixcyi.plugin.tinysnake.ui

import cn.aixcyi.plugin.tinysnake.Zoo.message
import cn.aixcyi.plugin.tinysnake.isWebUrl
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.ui.validation.DialogValidation
import com.intellij.ui.dsl.builder.LabelPosition
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.text

/**
 * Docstring 超链接编辑窗口。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 * @see <a href="https://peps.python.org/pep-0257/">PEP 257 – Docstring Conventions</a>
 */
class DocstringLinkCreator(
    project: Project,
    private var text: String = "",
    private var link: String = "",
) : DialogWrapper(project, true) {

    /**
     * 注意，如果链接不在 docstring 的开头或结尾（也就是紧邻 `"""`）的话，那么需要与空格相邻，否则 PyCharm 无法正确渲染。
     */
    val docstring
        get() = "`$text <$link>`_"

    init {
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
                .focusIf { text.isEmpty() }
        }
        row {
            textField()
                .hFill()
                .label(message("label.DocstringLinkSource.text"), LabelPosition.TOP)
                .text(link)
                .bindText(::link)
                .focusIf(text.isNotEmpty())
                .validate {
                    DialogValidation {
                        if (this@validate.text.isNotBlank() && this@validate.text.isWebUrl())
                            null
                        else
                            ValidationInfo(message("validation.NotAHyperlink")).asWarning()
                    }
                }
        }
    }
}