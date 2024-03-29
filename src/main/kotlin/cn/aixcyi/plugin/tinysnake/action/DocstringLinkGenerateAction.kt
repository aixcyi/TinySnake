package cn.aixcyi.plugin.tinysnake.action

import cn.aixcyi.plugin.tinysnake.Zoo.message
import cn.aixcyi.plugin.tinysnake.ui.DocstringLinkCreator
import com.intellij.codeInsight.hint.HintManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.jetbrains.python.PyTokenTypes
import com.jetbrains.python.psi.PyFile

/**
 * 在文档字符串（docstring）中插入超链接。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 * @see [com.jetbrains.python.psi.impl.PyPlainStringElementImpl]
 */
class DocstringLinkGenerateAction : PyAction() {

    override fun getActionUpdateThread() = ActionUpdateThread.EDT

    override fun update(event: AnActionEvent, file: PyFile, editor: Editor) {
        // 如果光标不在 docstring 中，则隐藏 Action
        event.presentation.isVisible = getCaretDocstring(event, file) != null
    }

    override fun actionPerformed(event: AnActionEvent, file: PyFile, editor: Editor) {
        val hint = HintManager.getInstance()
        if (getCaretDocstring(event, file) == null) {
            hint.showInformationHint(editor, message("hint.DocstringLinkCreator.notfound"))
            return
        }
        val dialog = DocstringLinkCreator(message("DocstringLinkCreator.dialog.title"))
        if (!dialog.showAndGet()) return
        val snippet = "`${dialog.text} <${dialog.link}>`_"
        WriteCommandAction.runWriteCommandAction(
            file.project,
            message("command.GenerateDocstringLink"),
            null,
            { editor.document.insertString(editor.caretModel.offset, snippet) }
        )
    }

    private fun getCaretDocstring(event: AnActionEvent, file: PyFile): PsiElement? {
        val element = getCaretElement(event, file)
        return element?.let {
            if (PyTokenTypes.DOCSTRING.equals(element.node.elementType)) element else null
        }
    }
}