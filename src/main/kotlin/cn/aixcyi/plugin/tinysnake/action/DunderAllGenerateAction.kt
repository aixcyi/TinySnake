package cn.aixcyi.plugin.tinysnake.action

import cn.aixcyi.plugin.tinysnake.Snippet
import cn.aixcyi.plugin.tinysnake.SnippetGenerator
import cn.aixcyi.plugin.tinysnake.Zoo.message
import cn.aixcyi.plugin.tinysnake.entity.DunderAll
import cn.aixcyi.plugin.tinysnake.storage.DunderAllOptimization
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ui.popup.PopupChooserBuilder
import com.intellij.psi.PsiElement
import com.intellij.ui.CollectionListModel
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.components.JBList
import com.jetbrains.python.PyNames
import com.jetbrains.python.psi.*
import javax.swing.JList
import javax.swing.ListSelectionModel

/**
 * 为 Python 源码生成 ``__all__`` 变量，或向其值插入用户选中的 Python 符号。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
class DunderAllGenerateAction : PyAction() {

    override fun actionPerformed(event: AnActionEvent, file: PyFile, editor: Editor) {
        val all = DunderAll(file)

        val options = JBList(CollectionListModel(all.symbols))
        val popup = PopupChooserBuilder(options)
            .setMovable(true)
            .setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION)
            .setTitle(message("GenerateDunderAll.popup.title"))
            .setAdText(message("GenerateDunderAll.popup.ad_text"))
            .setItemsChosenCallback { items: Set<String> -> this.patchValue(file, all, items) }
            .setRenderer(object : ColoredListCellRenderer<String>() {
                override fun customizeCellRenderer(
                    list: JList<out String>,
                    value: String,
                    index: Int,
                    selected: Boolean,
                    hasFocus: Boolean,
                ) {
                    this.append(value)
                    this.icon = all.icons[index]
                    this.isEnabled = !all.exports.contains(value)
                }
            })
            .createPopup() // options 的 EmptyText 在这一步会被覆盖掉

        options.emptyText.setText(message("GenerateDunderAll.popup.empty_text")) // 所以只能在这里设置 EmptyText
        popup.showInBestPositionFor(event.dataContext)
    }

    /**
     * 往 ``__all__`` 添加“字符串”。如果没有这个变量，就找个合适的地方创建之。
     *
     * @param items 所有需要添加的"字符串"。
     */
    private fun patchValue(
        file: PyFile,
        all: DunderAll,
        items: Set<String>
    ) {
        val runnable: Runnable
        val list = all.getVariableValue()
        val project = file.project
        val generator = SnippetGenerator(file)

        if (list == null) {
            val choices = all.sort(ArrayList(items), DunderAllOptimization.Order.APPEARANCE)
            val varValue = Snippet.makeStringList(choices)
            val statement = generator.createAssignment(PyNames.ALL, varValue)
            runnable = Runnable { file.addBefore(statement, findProperlyPlace(file)) }
        } else {
            val choices = ArrayList(items.minus(all.exports.toSet()))// 去除已经在 __all__ 里的符号
            all.sort(choices, DunderAllOptimization.Order.APPEARANCE)
            runnable = Runnable {
                for (choice in choices) {
                    list.add(generator.createStringLiteralFromString(choice!!))
                }
            }
        }
        WriteCommandAction.runWriteCommandAction(
            project,
            message("command.GenerateDunderAll"),
            null,
            runnable
        )
    }

    /**
     * 确定 ``__all__`` 变量的位置。
     *
     * @return 变量应该放在哪个元素的前面。
     * @see <a href="https://peps.python.org/pep-0008/#module-level-dunder-names">PEP 8 - 模块级别 Dunder 的布局位置</a>
     */
    private fun findProperlyPlace(file: PyFile): PsiElement {
        for (child in file.children) {
            if (child !is PyElement) continue

            // 跳过文件的 docstring
            if (child is PyExpressionStatement) {
                val expression: PyExpression = child.expression
                if (expression is PyStringLiteralExpression) continue
            } else if (child is PyFromImportStatement) {
                if (child.isFromFuture) continue
            }

            // 拿不到注释，所以不作判断

            // 根据 PEP 8 的格式约定，其它语句都要排在 __all__ 后面
            return child
        }
        return file.firstChild
    }
}