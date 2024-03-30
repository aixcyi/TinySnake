package cn.aixcyi.plugin.tinysnake.action

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.psi.PyFile

/**
 * 面向 Python 文件定制的 [AnAction] 。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
abstract class PyAction : AnAction() {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    /**
     * 更新 Action 的组件显示。
     *
     * @param event 触发当前 Action 的消息事件。
     */
    override fun update(event: AnActionEvent) {
        // 如果不在编辑器中则隐藏菜单
        val editor = event.getData(LangDataKeys.EDITOR_EVEN_IF_INACTIVE) ?: run {
            event.presentation.isVisible = false
            return
        }
        // 如果不在 Python 文件中则禁用菜单
        val file = event.getData(CommonDataKeys.PSI_FILE)
        if (file !is PyFile) {
            event.presentation.isEnabled = false
            return
        }
        this.update(event, file, editor)
    }

    /**
     * 更新 Action 的组件显示。
     *
     * @param event 触发当前 Action 的消息事件。
     * @param file 当前 Python 文件。
     * @param editor 当前编辑器。
     */
    open fun update(event: AnActionEvent, file: PyFile, editor: Editor) {
        event.presentation.isVisible = true
        event.presentation.isEnabled = true
    }

    /**
     * 触发当前 Action 后执行的业务代码。
     *
     * @param event 触发当前 Action 的消息事件。
     */
    override fun actionPerformed(event: AnActionEvent) {
        val editor = event.getData(LangDataKeys.EDITOR_EVEN_IF_INACTIVE) ?: return
        val psi = event.getData(CommonDataKeys.PSI_FILE)
        if (psi !is PyFile) return
        this.actionPerformed(event, psi, editor)
    }

    /**
     * 触发当前 Action 后执行的业务代码。
     *
     * @param event 触发当前 Action 的消息事件。
     * @param file 当前 Python 文件。
     * @param editor 当前编辑器。
     */
    abstract fun actionPerformed(event: AnActionEvent, file: PyFile, editor: Editor)

    /**
     * 查找光标所在的特定类型的元素。
     *
     * - 若光标不在任何元素内，则返回 `null` 。
     * - 当光标多于一个时（比如处于列选择模式下）会直接返回 `null` 。
     *
     * @param editor 当前编辑器。
     * @param file  表示 Python 文件的 PSI 元素。
     * @param type  元素或父元素的类型。
     * @return PSI元素。若光标不在特定类型的元素内，则返回 null 。
     */
    fun <T : PsiElement> getCaretElement(editor: Editor, file: PyFile, type: Class<T>): T? {
        if (editor.caretModel.caretCount > 1) return null
        val element = file.findElementAt(editor.caretModel.offset)
        return element?.let { PsiTreeUtil.getParentOfType(element, type) }  // 自内向外查找 光标所在处元素 外面的父元素。
    }
}