package cn.aixcyi.plugin.tinysnake.action

import cn.aixcyi.plugin.tinysnake.Zoo.message
import cn.aixcyi.plugin.tinysnake.entity.DocstringFormatSuggestion
import cn.aixcyi.plugin.tinysnake.ui.DocstringLinkCreator
import cn.aixcyi.plugin.tinysnake.util.isURL
import com.intellij.codeInsight.hint.HintManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.jetbrains.python.PyTokenTypes
import com.jetbrains.python.psi.PyFile

/**
 * 在文档字符串 [docstring](https://docs.python.org/zh-cn/3/glossary.html#term-docstring) 中插入超链接。
 *
 * - 当光标不在 docstring 中时会弹出提示。
 * - 在光标不止一个（如列选择模式下）时，会自动移除所有非主光标。
 * - 如果选择了多行，会自动移除选中部分，只剩光标。
 * - 如果选中了以 `http://` 或 `https://` 开头的字符串，会将选中的文本填充到链接字段。
 * - 如果选中了普通文本，会将选中的文本填充到标题字段。
 * - 如果选中了文本，则将其替换成生成的链接。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
class GenerateDocstringLinkAction : PyAction() {

    override fun actionPerformed(editor: Editor, event: AnActionEvent, file: PyFile) {
        // 如果光标不是一个（多光标模式／列选择模式）或者不在 docstring 中，则进行提示
        val hint = HintManager.getInstance()

        // 是否需要提示用户切换 docstring 格式以便正确渲染
        val suggestion = DocstringFormatSuggestion(file)
        val notification = if (suggestion.isRestFormat) null else suggestion.notification

        // 如果光标不在 docstring 中
        file.findElementAt(editor.caretModel.offset)?.let {
            if (PyTokenTypes.DOCSTRING.equals(it.node.elementType)) it else null
        } ?: let {
            hint.showInformationHint(editor, message("hint.CaretNotOnDocstring.text"))
            return
        }

        // 如果光标不止一个（多光标模式／列选择模式）
        if (editor.caretModel.caretCount > 1)
            editor.caretModel.removeSecondaryCarets()

        // 避免跨行选择
        val selection = editor.selectionModel.selectedText ?: ""
        if (selection.contains("[\\r\\n]".toRegex()))
            editor.selectionModel.removeSelection()

        // 如果选中了URL就填到链接字段，选中了普通文本就填到标题字段，啥都没选中就不填
        val dialog = DocstringLinkCreator(
            file.project,
            if (selection.isURL("http", "https")) "" else selection,  // “没选中导致设置了空字符串”与“默认空字符串”是等价的
            if (selection.isURL("http", "https")) selection else "",
        )
        if (!dialog.showAndGet())
            return

        // 检测前后有没有空格，如果没有则补充，不然IDE没办法正确渲染
        val head = if (this.shouldPatchHead(editor)) " " else ""
        val tail = if (this.shouldPatchTail(editor)) " " else ""
        val snippet = "$head${dialog.docstring}$tail"

        // 没有选中文本则进行「插入」，选中了文本则进行「替换」
        WriteCommandAction.runWriteCommandAction(
            file.project,
            message("command.GenerateDocstringLink"),
            null,
            if (selection.isEmpty())
                Runnable {
                    editor.document.insertString(editor.caretModel.offset, snippet)
                }
            else
                Runnable {
                    editor.document.replaceString(
                        editor.selectionModel.selectionStart,
                        editor.selectionModel.selectionEnd,
                        snippet,
                    )
                }
        )
        notification?.notify(file.project)
    }

    private fun shouldPatchHead(editor: Editor): Boolean {
        val position = editor.offsetToLogicalPosition(editor.selectionModel.selectionStart)
        if (position.column == 0)  // 列号为 column+1，0 表示在行首。
            return false
        // 因为列号不为 0，说明前面肯定还有字符，因此以下代码不会报错
        return editor.document.charsSequence[editor.selectionModel.selectionStart - 1] != ' '
    }

    private fun shouldPatchTail(editor: Editor): Boolean {
        val position = editor.offsetToLogicalPosition(editor.selectionModel.selectionEnd)
        val endColumn = editor.offsetToLogicalPosition(editor.document.getLineEndOffset(position.line)).column
        if (position.column == endColumn)
            return false
        // 字符数组下标为 n 的那个字符的右侧索引是 n+1，因此以下代码有可能会报错
        return try {
            editor.document.charsSequence[editor.selectionModel.selectionEnd] != ' '
        } catch (_: IndexOutOfBoundsException) {
            false
        }
    }
}