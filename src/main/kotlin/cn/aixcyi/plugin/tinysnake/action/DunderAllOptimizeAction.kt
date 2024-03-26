package cn.aixcyi.plugin.tinysnake.action

import cn.aixcyi.plugin.tinysnake.Snippet
import cn.aixcyi.plugin.tinysnake.SnippetGenerator
import cn.aixcyi.plugin.tinysnake.Zoo.message
import cn.aixcyi.plugin.tinysnake.entity.DunderAll
import cn.aixcyi.plugin.tinysnake.ui.DunderAllOptimizer
import com.intellij.codeInsight.hint.HintManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.jetbrains.python.psi.PyFile

/**
 * 优化 Python 源码中已经存在的 __all__ 变量的值。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
class DunderAllOptimizeAction : PyAction() {
    override fun actionPerformed(event: AnActionEvent, file: PyFile, editor: Editor) {
        // 准备基础设施
        val hint = HintManager.getInstance()

        // 查找 __all__ 相关
        val all = DunderAll(file)
        if (all.variable == null) {
            hint.showInformationHint(editor, message("hint.OptimizeDunderAll.missing"))
            return
        }

        // 选择优化方式
        val dialog = DunderAllOptimizer()
        if (!dialog.showAndGet()) return

        // 执行优化
        val project = file.project
        val list = all.getVariableValue()
        if (list != null) {
            // 构造优化后的代码
            val exporting = all.sort(ArrayList(all.exports), dialog.state.mySequenceOrder)
            val sequences = Snippet.makeStringList(exporting, dialog.state)
            val statement = SnippetGenerator(file).createListLiteral(sequences)
            // 写入编辑器并产生一个撤销选项
            WriteCommandAction.runWriteCommandAction(
                project,
                message("command.OptimizeDunderAll"),
                null,
                { list.replace(statement) }
            )
            hint.showInformationHint(editor, message("hint.OptimizeDunderAll.done"))
        } else {
            hint.showErrorHint(editor, message("hint.OptimizeDunderAll.invalid"))
        }
    }
}