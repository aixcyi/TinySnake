package cn.aixcyi.plugin.tinysnake.action;

import cn.aixcyi.plugin.tinysnake.DunderAllEntity;
import cn.aixcyi.plugin.tinysnake.SymbolsOrder;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.components.JBList;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyElementGeneratorImpl;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;

/**
 * 为 Python 源码生成 __all__ 变量，或向其值插入用户选中的 Python 符号。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
public class GenerateDunderAllAction extends PyAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event, @NotNull PyFile file) {
        DunderAllEntity all = new DunderAllEntity(file);  // 遍历所有顶层表达式获取所有符号

        JBList<String> options = new JBList<>(new CollectionListModel<>(all.symbols));
        JBPopup popup = new PopupChooserBuilder<>(options)
                .setSelectionMode(MULTIPLE_INTERVAL_SELECTION)
                .setRenderer(new ColoredListCellRenderer<>() {
                    @Override
                    protected void customizeCellRenderer(@NotNull JList<? extends String> list,
                                                         String value,
                                                         int index,
                                                         boolean selected,
                                                         boolean hasFocus) {
                        this.append(value);
                        this.setIcon(all.icons.get(index));
                        this.setEnabled(!all.exports.contains(value));
                    }
                })
                .setItemsChosenCallback(items -> this.patchValue(file, all, items))
                .setAdText("Ctrl+A全选／Ctrl+单击多选／Shift+单击连选")
                .setTitle("选择导出到 __all__ 的符号")
                .setMovable(true)
                .createPopup();  // options 的 EmptyText 在这一步会被覆盖掉

        options.getEmptyText().setText("没有可公开的顶级符号");  // 所以只能在这里设置 EmptyText
        popup.showInBestPositionFor(event.getDataContext());
    }

    /**
     * 往 __all__ 变量的值添加“字符串”。如果没有这个变量，就找个合适的地方创建之。
     *
     * @param items 所有需要添加的"字符串"。
     */
    public void patchValue(@NotNull PyFile file,
                           @NotNull DunderAllEntity all,
                           @NotNull final Set<? extends String> items) {
        Project project = file.getProject();
        Runnable runnable;
        PyExpression list = all.variable == null ? null : all.variable.findAssignedValue();
        PyElementGeneratorImpl generator = new PyElementGeneratorImpl(project);

        // 没有定义 __all__ ，或者赋的值不是列表的情况下，直接新建一个 __all__ 。
        // 因为后者的情况太复杂了，无法简洁地一概而论。
        if (all.variable == null || !(list instanceof PyListLiteralExpression)) {
            List<String> choices = all.sort(new ArrayList<>(items), SymbolsOrder.APPEARANCE);
            String text = all.buildAssignment(choices, false);
            runnable = () -> file.addBefore(
                    generator.createFromText(file.getLanguageLevel(), PyAssignmentStatement.class, text),
                    findProperlyPlace(file)
            );
        } else {
            all.exports.forEach(items::remove);  // 去除已经在 __all__ 里的符号
            ArrayList<String> choices = new ArrayList<>(items);
            all.sort(choices, SymbolsOrder.APPEARANCE);
            runnable = () -> {
                for (String choice : choices) {
                    list.add(generator.createStringLiteralFromString(choice));
                }
            };
        }
        WriteCommandAction.runWriteCommandAction(
                project, "生成 __all__", "GenerateDunderAll", runnable
        );
    }

    /**
     * 确定 __all__ 变量的位置。
     *
     * @return 变量应该放在哪个元素的前面。
     * @see <a href="https://peps.python.org/pep-0008/#module-level-dunder-names">PEP 8 - 模块级别 Dunder 的布局位置</a>
     */
    private @NotNull PsiElement findProperlyPlace(@NotNull PyFile file) {
        for (PsiElement child : file.getChildren()) {
            if (!(child instanceof PyElement element)) continue;

            // 跳过文件的 docstring
            if (element instanceof PyExpressionStatement statement) {
                PyExpression expression = statement.getExpression();
                if (expression instanceof PyStringLiteralExpression) continue;
            }
            // 跳过 from __future__ import (xxx)
            else if (element instanceof PyFromImportStatement fis) {
                if (fis.isFromFuture()) continue;
            }
            // 拿不到注释，所以不作判断

            // 根据 PEP 8 的格式约定，其它语句都要排在 __all__ 后面
            return child;
        }
        return file.getFirstChild();
    }
}
