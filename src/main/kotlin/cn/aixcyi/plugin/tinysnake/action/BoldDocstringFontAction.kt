package cn.aixcyi.plugin.tinysnake.action

import cn.aixcyi.plugin.tinysnake.util.IOUtil.message
import cn.aixcyi.plugin.tinysnake.util.getPyFile
import cn.aixcyi.plugin.tinysnake.util.isSelectionEndRightBlank
import cn.aixcyi.plugin.tinysnake.util.isSelectionStartLeftBlank
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project

/**
 * 加粗[文档字符串](https://docs.python.org/zh-cn/3/glossary.html#term-docstring)里选中的文本。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 * @see <a href="https://devguide.python.org/documentation/markup/">reStructuredText markup</a>
 */
class BoldDocstringFontAction : DocstringAction(), DumbAware {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        // 如果不在 Python 文件中则禁用菜单
        event.presentation.isEnabled = event.getPyFile() != null
    }

    override fun actionPerformed(editor: Editor, project: Project, selection: String): Any? {
        // 检测前后有没有空格，如果没有则补充，不然IDE没办法正确渲染
        val head = if (editor.isSelectionStartLeftBlank()) "**" else " **"
        val tail = if (editor.isSelectionEndRightBlank()) "**" else "** "
        val snippet = "$head$selection$tail"

        // 如果没有选中文本，则「插入」并将光标移到中间
        if (selection.isEmpty()) {
            WriteCommandAction.runWriteCommandAction(project, message("command.Docstring.ToggleBold"), null, {
                editor.document.insertString(editor.caretModel.offset, snippet)
                editor.caretModel.moveToOffset(editor.caretModel.offset + head.length)
            })
        }
        // 如果选中了文本则进行「替换」并保持选中范围
        else {
            WriteCommandAction.runWriteCommandAction(project, message("command.Docstring.ToggleBold"), null, {
                editor.document.replaceString(
                    editor.selectionModel.selectionStart,
                    editor.selectionModel.selectionEnd,
                    snippet,
                )
                editor.selectionModel.setSelection(
                    editor.selectionModel.selectionStart + head.length,
                    editor.selectionModel.selectionEnd - tail.length,
                )
                editor.caretModel.moveToOffset(editor.selectionModel.selectionEnd)
            })
        }
        return null
    }
}