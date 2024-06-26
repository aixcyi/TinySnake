package cn.aixcyi.plugin.tinysnake.action

import cn.aixcyi.plugin.tinysnake.entity.DunderAll
import cn.aixcyi.plugin.tinysnake.entity.SnippetGenerator
import cn.aixcyi.plugin.tinysnake.entity.TopSymbols
import cn.aixcyi.plugin.tinysnake.ui.DunderAllOptimizer
import cn.aixcyi.plugin.tinysnake.util.IOUtil.message
import cn.aixcyi.plugin.tinysnake.util.getEditor
import cn.aixcyi.plugin.tinysnake.util.getPyFile
import com.intellij.codeInsight.hint.HintManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction

/**
 * 优化 Python 源码中已经存在的 `__all__` 变量的值。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
class OptimizeDunderAllAction : AnAction() {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        // 如果不在 Python 文件中则禁用菜单
        event.presentation.isEnabled = event.getPyFile() != null
    }

    override fun actionPerformed(event: AnActionEvent) {
        val editor = event.getEditor(true) ?: return
        val file = event.getPyFile() ?: return
        val hint = HintManager.getInstance()
        val dunderAll = DunderAll(file)
        if (dunderAll.expression == null) {
            hint.showInformationHint(editor, message("hint.DunderAllNotFound.text"))
            return
        }
        if (!dunderAll.isValidAssignment()) {
            hint.showInformationHint(editor, message("hint.InvalidDunderAll.text"))
            return
        }

        // 选择优化方式
        val dialog = DunderAllOptimizer()
        if (!dialog.showAndGet()) return

        // 构造优化后的代码
        val handler = TopSymbols(file)
        val statement = dunderAll.exports.toMutableList()
            .apply { handler.sort(this, dialog.state.mySequenceOrder) }
            .apply { if (dialog.state.isAutoRemoveNonexistence) handler.remove(this) }
            .let { SnippetGenerator(file).createStringListLiteral(it, dialog.state) }

        // 写入编辑器并产生一个撤销选项
        WriteCommandAction.runWriteCommandAction(
            file.project,
            message("command.OptimizeDunderAll"),
            null,
            { dunderAll.assignment!!.replace(statement) }
        )
        hint.showInformationHint(editor, message("hint.DunderAllOptimized.text"))
    }
}