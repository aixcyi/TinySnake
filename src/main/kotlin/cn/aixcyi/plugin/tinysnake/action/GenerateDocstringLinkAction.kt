package cn.aixcyi.plugin.tinysnake.action

import cn.aixcyi.plugin.tinysnake.ui.DocstringLinkCreator
import cn.aixcyi.plugin.tinysnake.util.IOUtil.message
import cn.aixcyi.plugin.tinysnake.util.getPyFile
import cn.aixcyi.plugin.tinysnake.util.isSelectionEndRightBlank
import cn.aixcyi.plugin.tinysnake.util.isSelectionStartLeftBlank
import cn.aixcyi.plugin.tinysnake.util.isURL
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project

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
 * @see <a href="https://devguide.python.org/documentation/markup/">reStructuredText markup</a>
 */
class GenerateDocstringLinkAction : DocstringAction() {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        // 如果不在 Python 文件中则禁用菜单
        event.presentation.isEnabled = event.getPyFile() != null
    }

    override fun actionPerformed(editor: Editor, project: Project, selection: String): Any? {
        // 如果选中了URL就填到链接字段，选中了普通文本就填到标题字段，啥都没选中就不填
        val dialog = DocstringLinkCreator(
            project,
            if (selection.isURL("http", "https")) "" else selection,  // “没选中导致设置了空字符串”与“默认空字符串”是等价的
            if (selection.isURL("http", "https")) selection else "",
        )
        if (!dialog.showAndGet())
            return Any()

        // 检测前后有没有空格，如果没有则补充，不然IDE没办法正确渲染
        val head = if (editor.isSelectionStartLeftBlank()) "" else " "
        val tail = if (editor.isSelectionEndRightBlank()) "" else " "
        val snippet = "$head${dialog.docstring}$tail"

        // 没有选中文本则进行「插入」，选中了文本则进行「替换」
        WriteCommandAction.runWriteCommandAction(
            project,
            message("command.Docstring.CreateLink"),
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
        return null
    }
}