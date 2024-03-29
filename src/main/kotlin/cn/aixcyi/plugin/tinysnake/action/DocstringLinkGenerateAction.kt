package cn.aixcyi.plugin.tinysnake.action

import cn.aixcyi.plugin.tinysnake.Zoo.message
import cn.aixcyi.plugin.tinysnake.ui.DocstringLinkCreator
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
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
class DocstringLinkGenerateAction : PyAction() {

    override fun getActionUpdateThread() = ActionUpdateThread.EDT

    override fun update(event: AnActionEvent, file: PyFile, editor: Editor) {
        // 如果光标不在 docstring 中，则禁用 Action
        event.presentation.isEnabled = getCaretDocstring(editor, file) != null
    }

    override fun actionPerformed(event: AnActionEvent, file: PyFile, editor: Editor) {
        // 如果光标不在 docstring 中，则直接无视操作，没必要进行提示
        getCaretDocstring(editor, file) ?: return

        // 有选中文本则进行「替换」，没有则进行「插入」
        val isReplace = editor.selectionModel.hasSelection()

        // 编辑确认
        val snippet = DocstringLinkCreator(message("DocstringLinkCreator.dialog.title"))
            .setText(if (isReplace) editor.selectionModel.selectedText else "")
            .setLink(this.getHyperlinkFromClipboard())
            .showThenGet()
            ?: return

        // 执行动作
        WriteCommandAction.runWriteCommandAction(
            file.project,
            message("command.GenerateDocstringLink"),
            null,
            if (!isReplace)
                Runnable { editor.document.insertString(editor.caretModel.offset, snippet) }
            else
                Runnable {
                    editor.document.replaceString(
                        editor.selectionModel.selectionStart,
                        editor.selectionModel.selectionEnd,
                        snippet
                    )
                }
        )
    }

    /**
     * 获取光标所在的 docstring 元素。如果光标多于一个，或找不到 docstring 都会返回 `null` 。
     */
    private fun getCaretDocstring(editor: Editor, file: PyFile): PsiElement? {
        if (editor.caretModel.caretCount > 1) return null
        val element = file.findElementAt(editor.caretModel.offset)
        return element?.let {
            if (PyTokenTypes.DOCSTRING.equals(element.node.elementType)) element else null
        }
    }

    /**
     * 获取剪切板中的超链接。如果没有，将返回空字符串。
     */
    private fun getHyperlinkFromClipboard(): String {
        val clipText = try {
            Toolkit.getDefaultToolkit().systemClipboard.getData(DataFlavor.stringFlavor) as String
        } catch (_: Exception) {
            ""
        }
        val lowerText = clipText.lowercase()
        return if (lowerText.startsWith("http://") || lowerText.startsWith("https://"))
            clipText
        else
            ""
    }
}