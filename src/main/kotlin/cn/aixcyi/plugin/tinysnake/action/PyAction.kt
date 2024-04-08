package cn.aixcyi.plugin.tinysnake.action

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.editor.Editor
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
        this.update(editor, event, file)
    }

    /**
     * 更新 Action 的组件显示。
     *
     * @param editor 当前编辑器。
     * @param event 触发当前 Action 的消息事件。
     * @param file 当前 Python 文件。
     */
    open fun update(editor: Editor, event: AnActionEvent, file: PyFile) {
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
        this.actionPerformed(editor, event, psi)
    }

    /**
     * 触发当前 Action 后执行的业务代码。
     *
     * @param editor 当前编辑器。
     * @param event 触发当前 Action 的消息事件。
     * @param file 当前 Python 文件。
     */
    abstract fun actionPerformed(editor: Editor, event: AnActionEvent, file: PyFile)
}