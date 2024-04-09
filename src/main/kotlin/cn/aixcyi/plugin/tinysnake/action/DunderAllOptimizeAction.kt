package cn.aixcyi.plugin.tinysnake.action

import cn.aixcyi.plugin.tinysnake.SnippetGenerator
import cn.aixcyi.plugin.tinysnake.Zoo.message
import cn.aixcyi.plugin.tinysnake.entity.DunderAll
import cn.aixcyi.plugin.tinysnake.entity.TopSymbols
import cn.aixcyi.plugin.tinysnake.ui.DunderAllOptimizer
import com.intellij.codeInsight.hint.HintManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.jetbrains.python.psi.PyFile

/**
 * 优化 Python 源码中已经存在的 `__all__` 变量的值。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
class DunderAllOptimizeAction : PyAction() {

    override fun actionPerformed(editor: Editor, event: AnActionEvent, file: PyFile) {
        val hint = HintManager.getInstance()
        val dunderAll = DunderAll(file)
        if (dunderAll.expression == null) {
            hint.showInformationHint(editor, message("hint.OptimizeDunderAll.missing"))
            return
        }
        if (!dunderAll.isValidAssignment) {
            hint.showInformationHint(editor, message("hint.OptimizeDunderAll.invalid"))
            return
        }

        // 选择优化方式
        val dialog = DunderAllOptimizer()
        if (!dialog.showAndGet()) return

        // 构造优化后的代码
        val exports = dunderAll.exports.toMutableList()
        TopSymbols(file).sort(exports, dialog.state.mySequenceOrder)
        val statement = SnippetGenerator(file).createStringListLiteral(exports, dialog.state)

        // 写入编辑器并产生一个撤销选项
        WriteCommandAction.runWriteCommandAction(
            file.project,
            message("command.OptimizeDunderAll"),
            null,
            { dunderAll.assignment!!.replace(statement) }
        )
        hint.showInformationHint(editor, message("hint.OptimizeDunderAll.done"))
    }
}