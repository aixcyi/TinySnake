package cn.aixcyi.plugin.tinysnake.action;

import cn.aixcyi.plugin.tinysnake.SequenceStyle;
import cn.aixcyi.plugin.tinysnake.DunderAllEntity;
import cn.aixcyi.plugin.tinysnake.SnippetBuilder;
import cn.aixcyi.plugin.tinysnake.SequenceOrder;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.components.JBList;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.psi.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Set;

import static cn.aixcyi.plugin.tinysnake.Translation.MENU;
import static javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;

/**
 * 为 Python 源码生成 __all__ 变量，或向其值插入用户选中的 Python 符号。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
public class GenerateDunderAllAction extends PyAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event, @NotNull PyFile file) {
        var all = new DunderAllEntity(file);  // 遍历所有顶层表达式获取所有符号

        var options = new JBList<>(new CollectionListModel<>(all.symbols));
        var popup = new PopupChooserBuilder<>(options)
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
                .setAdText(MENU.get("GenerateDunderAllAction.popup.ad_text"))
                .setTitle(MENU.get("GenerateDunderAllAction.popup.title"))
                .setMovable(true)
                .createPopup();  // options 的 EmptyText 在这一步会被覆盖掉

        options.getEmptyText().setText(MENU.get("GenerateDunderAllAction.popup.empty_text"));  // 所以只能在这里设置 EmptyText
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
        Runnable runnable;
        var list = all.getVariableValue();
        var project = file.getProject();
        var builder = new SnippetBuilder(file);

        if (list == null) {
            var choices = all.sort(new ArrayList<>(items), SequenceOrder.APPEARANCE);
            var varValue = builder.makeSequence(choices, SequenceStyle.WINGED_LIST, true, true);
            var statement = builder.cakeAssignment(PyNames.ALL, varValue);
            runnable = () -> file.addBefore(statement, findProperlyPlace(file));
        } else {
            all.exports.forEach(items::remove);  // 去除已经在 __all__ 里的符号
            var choices = new ArrayList<String>(items);
            all.sort(choices, SequenceOrder.APPEARANCE);
            runnable = () -> {
                for (String choice : choices) {
                    list.add(builder.cakeString(choice));
                }
            };
        }
        WriteCommandAction.runWriteCommandAction(
                project,
                MENU.get("GenerateDunderAllAction.command.name"),
                "GenerateDunderAll",
                runnable
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
                var expression = statement.getExpression();
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
