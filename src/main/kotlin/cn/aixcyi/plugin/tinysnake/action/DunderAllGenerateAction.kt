package cn.aixcyi.plugin.tinysnake.action

import cn.aixcyi.plugin.tinysnake.SnippetGenerator
import cn.aixcyi.plugin.tinysnake.Zoo.message
import cn.aixcyi.plugin.tinysnake.entity.DunderAll
import cn.aixcyi.plugin.tinysnake.entity.TopSymbols
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ui.popup.PopupChooserBuilder
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.ui.CollectionListModel
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.components.JBList
import com.jetbrains.python.PyNames
import com.jetbrains.python.psi.PyExpressionStatement
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.PyFromImportStatement
import com.jetbrains.python.psi.PyStringLiteralExpression
import javax.swing.JList
import javax.swing.ListSelectionModel

/**
 * 为 Python 源码生成 `__all__` 变量，或向其值插入用户选中的 Python 符号。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
class DunderAllGenerateAction : PyAction() {

    override fun actionPerformed(editor: Editor, event: AnActionEvent, file: PyFile) {
        // <action id="GenerateDunderAllWithImports">
        val isWithImports = event.actionManager.getId(this).lowercase().contains("import")
        val dunderAll = DunderAll(file)
        val symbols = TopSymbols(file, withImports = isWithImports)
        val options = JBList(CollectionListModel(symbols.names))
        val popup = PopupChooserBuilder(options)
            .setMovable(true)
            .setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION)
            .setTitle(message("GenerateDunderAll.popup.title"))
            .setAdText(message("GenerateDunderAll.popup.ad_text"))
            .setItemsChosenCallback { this.onChosen(file, it, symbols, dunderAll) }
            .setRenderer(object : ColoredListCellRenderer<String>() {
                override fun customizeCellRenderer(
                    list: JList<out String>,
                    value: String,
                    index: Int,
                    selected: Boolean,
                    hasFocus: Boolean,
                ) {
                    this.append(value)
                    this.icon = symbols.icons[index]
                    this.isEnabled = !dunderAll.exports.contains(value)
                }
            })
            .createPopup() // options 的 EmptyText 在这一步会被覆盖掉

        options.emptyText.setText(message("GenerateDunderAll.popup.empty_text")) // 所以只能在这里设置 EmptyText
        popup.showInBestPositionFor(event.dataContext)
    }

    /**
     * `JBPopup` 列表项被选中。
     *
     * @param file Python 文件。
     * @param items 所有选中项。
     * @param symbols 文件内的顶层符号。
     * @param dunderAll `__all__` 实体。
     */
    private fun onChosen(
        file: PyFile,
        items: Set<String>,
        symbols: TopSymbols,
        dunderAll: DunderAll,
    ) {
        val choices = (items - dunderAll.exports.toSet()).toMutableList()  // 去除已经在 __all__ 里的符号
        val runnable: Runnable
        val generator = SnippetGenerator(file)
        symbols.sort(choices)

        if (dunderAll.isValidAssignment) {
            runnable = Runnable {
                for (choice in choices) {
                    dunderAll.assignment!!.add(generator.createStringLiteralFromString(choice))
                }
            }
        } else {
            val anchor = properlyPlaceTo(file)
            val statement = generator.createAssignment(
                PyNames.ALL,
                SnippetGenerator.stringList(choices),
            )
            runnable = Runnable { file.addBefore(statement, anchor) }
        }
        WriteCommandAction.runWriteCommandAction(
            file.project,
            message("command.GenerateDunderAll"),
            null,
            runnable
        )
    }

    /**
     * 确定 `__all__` 变量的位置。
     *
     * @return 变量应该放在哪个元素的前面。
     * @see <a href="https://peps.python.org/pep-0008/#module-level-dunder-names">PEP 8 - 模块级别 Dunder 的布局位置</a>
     */
    private fun properlyPlaceTo(file: PyFile): PsiElement {
        // 这里只考虑完全遵守 PEP 规范的情况，
        // 因为不遵守规范时无法确定确切的位置。
        for (child in file.children) {
            // 跳过文件自身的 docstring
            if (child is PyExpressionStatement && child.expression is PyStringLiteralExpression) {
                continue
            }
            // 跳过 __future__ 导入
            else if (child is PyFromImportStatement && child.isFromFuture) {
                continue
            }
            // 跳过 shebang 和文件编码定义
            else if (child is PsiComment && (child.isShebang() || child.isEncodingDefine())) {
                continue
            }
            // 跳过空格（虽然我也不知道是哪里来的）
            else if (child is PsiWhiteSpace) {
                continue
            }
            // 其它语句都要排在 __all__ 后面
            return child
        }
        return file.firstChild
    }
}

// 见 https://peps.python.org/pep-0263/#defining-the-encoding
private val REGEX_ENCODING_DEFINE: Regex
    get() = "^[ \\t\\f]*#.*?coding[:=][ \\t]*([-_.a-zA-Z0-9]+)".toRegex()

private fun PsiComment.isShebang() = this.text.startsWith("#!")
private fun PsiComment.isEncodingDefine() = REGEX_ENCODING_DEFINE.containsMatchIn(this.text)
