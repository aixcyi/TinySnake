package cn.aixcyi.plugin.tinysnake.action

import cn.aixcyi.plugin.tinysnake.entity.DocstringFormatSuggestion
import cn.aixcyi.plugin.tinysnake.util.IOUtil.message
import cn.aixcyi.plugin.tinysnake.util.getEditor
import cn.aixcyi.plugin.tinysnake.util.getPyFile
import com.intellij.codeInsight.hint.HintManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.jetbrains.python.PyTokenTypes

/**
 * 操作文档字符串 [docstring](https://docs.python.org/zh-cn/3/glossary.html#term-docstring) 的 [AnAction] 。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
abstract class DocstringAction : AnAction() {

    abstract fun actionPerformed(editor: Editor, project: Project, selection: String): Any?

    override fun actionPerformed(event: AnActionEvent) {
        val editor = event.getEditor(true) ?: return
        val file = event.getPyFile() ?: return
        val hint = HintManager.getInstance()  // 如果光标不是一个（多光标模式／列选择模式）或者不在 docstring 中，则进行提示

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

        if (this.actionPerformed(editor, file.project, selection) != null)
            return

        // 是否需要提示用户切换 docstring 格式以便正确渲染
        val suggestion = DocstringFormatSuggestion(file)
        if (suggestion.isRestFormat) {
            suggestion.notification.notify(file.project)
        }
    }
}