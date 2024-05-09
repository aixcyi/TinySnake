package cn.aixcyi.plugin.tinysnake.action

import cn.aixcyi.plugin.tinysnake.Zoo.message
import cn.aixcyi.plugin.tinysnake.isWebUrl
import cn.aixcyi.plugin.tinysnake.ui.DocstringLinkCreator
import com.intellij.codeInsight.hint.HintManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.jetbrains.python.PyTokenTypes
import com.jetbrains.python.psi.PyFile
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor

/**
 * 在文档字符串（docstring）中插入超链接。
 *
 * - 在多个光标时（比如列选择模式下）该功能没有意义。
 * - 当光标不在 docstring 中时该功能没有意义。
 * - 如果选中了文本，则将其替换成生成的链接。
 * - 如果剪切板中有 `"http://"` 或 `"https://"` 开头的字符串，则自动填充到编辑窗口的链接部分，免去输入。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 * @see [com.jetbrains.python.psi.impl.PyPlainStringElementImpl]
 */
class GenerateDocstringLinkAction : PyAction() {

    override fun actionPerformed(editor: Editor, event: AnActionEvent, file: PyFile) {
        // 如果光标不是一个（多光标模式／列选择模式）或者不在 docstring 中，则进行提示
        val hint = HintManager.getInstance()
        if (editor.caretModel.caretCount != 1) {
            hint.showInformationHint(editor, message("hint.NotSupportMultipleCaretMode.text"))
            return
        }
        file.findElementAt(editor.caretModel.offset)?.let {
            if (PyTokenTypes.DOCSTRING.equals(it.node.elementType)) it else null
        } ?: let {
            hint.showInformationHint(editor, message("hint.CaretNotOnDocstring.text"))
            return
        }

        // 要么生成代码：有选中文本则进行「替换」，没有则进行「插入」
        // 要么取消动作
        val isReplace = editor.selectionModel.hasSelection()
        val dialog = DocstringLinkCreator(
            file.project,
            if (isReplace) (editor.selectionModel.selectedText ?: "") else "",
            this.getTextFromClipboard(),
        )
        val docstring = if (dialog.showAndGet()) dialog.docstring else return

        // 检测前后有没有空格，如果没有则补充，不然IDE没办法正确渲染
        val context = if (isReplace)
            editor.document.getText(
                TextRange(
                    editor.selectionModel.selectionStart - 1,
                    editor.selectionModel.selectionEnd + 1,
                )
            )
        else
            editor.document.getText(
                TextRange(
                    editor.caretModel.offset - 1,
                    editor.caretModel.offset + 1,
                )
            )
        val head = if (context.startsWith(' ')) "" else " "
        val tail = if (context.endsWith(' ')) "" else " "
        val snippet = "$head$docstring$tail"

        // 生成动作
        val runnable = if (!isReplace)
            Runnable { editor.document.insertString(editor.caretModel.offset, snippet) }
        else
            Runnable {
                editor.document.replaceString(
                    editor.selectionModel.selectionStart,
                    editor.selectionModel.selectionEnd,
                    snippet
                )
            }

        // 执行动作
        WriteCommandAction.runWriteCommandAction(
            file.project,
            message("command.GenerateDocstringLink"),
            null,
            runnable
        )
    }

    /**
     * 获取剪切板中的文本。
     *
     * @return 如果没有，将返回空字符串 `""` 。
     */
    private fun getTextFromClipboard() = try {
        Toolkit.getDefaultToolkit().systemClipboard.getData(DataFlavor.stringFlavor) as String
    } catch (_: Exception) {
        ""
    }.let {
        if (it.isNotBlank() && it.isWebUrl()) it else ""
    }
}