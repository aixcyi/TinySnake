package cn.aixcyi.plugin.tinysnake;

import com.intellij.icons.AllIcons.Nodes;
import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.util.Consumer;
import com.jetbrains.python.psi.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.*;

import static javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;

/**
 * 在 "生成..." 菜单里添加的 "__all__"。
 * <p>
 * 该菜单用于生成或更新 Python 源码文件中的 __all__ 变量。
 *
 * @author aixcyi
 */
public class GenerateDunderAllAction extends AnAction {
    public static final String VAR_NAME_DUNDER_ALL = "__all__";

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        PsiFile psi = event.getData(CommonDataKeys.PSI_FILE);
        if (psi == null) return;

        Language language = psi.getLanguage();
        Presentation presentation = event.getPresentation();
        presentation.setVisible(language == Language.findLanguageByID("Python"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        PsiFile psi = event.getData(CommonDataKeys.PSI_FILE);
        if (psi == null) return;
        PyFile file = (PyFile) psi;

        List<String> symbols = new ArrayList<>();  // 顶层所有符号
        List<Icon> icons = new ArrayList<>();  // symbols 原对象的类型对应的符号
        Set<String> exports = new HashSet<>();  // __all__ 导出的所有符号

        // 遍历所有顶层表达式获取：类名、函数名、变量名
        file.getStatements().forEach(statement -> {
            // 类定义
            if (statement instanceof PyClass) {
                symbols.add(statement.getName());
                icons.add(Nodes.Class);
            }
            // 函数定义
            else if (statement instanceof PyFunction) {
                symbols.add(statement.getName());
                icons.add(Nodes.Method);
            }
            // 赋值表达式
            else if (statement instanceof PyAssignmentStatement assignment) {
                // 因为赋值表达式存在元组解包的情况，
                // 所以需要用循环来提取普通变量
                for (Pair<PyExpression, PyExpression> target : assignment.getTargetsToValuesMapping()) {
                    String varName = target.first.getName();

                    // 顶层的普通变量
                    if (!Objects.equals(varName, VAR_NAME_DUNDER_ALL)) {
                        symbols.add(varName);
                        icons.add(Nodes.Variable);
                        continue;
                    }
                    // 所有通过 __all__ 公开的符号
                    for (PsiElement child : target.second.getChildren())
                        if (child instanceof PyStringLiteralExpression string)
                            exports.add(string.getStringValue());
                }
            }
            // TODO: else if (statement instanceof PyIfStatement) {}
        });

        // TODO:
        //  JBList<Pair<String, Icon>> choices = new JBList<>(symbols);
        //  choices.setEmptyText("没有可公开的顶级符号");

        JBPopup popup = JBPopupFactory.getInstance()
                .createPopupChooserBuilder(symbols)
                .setSelectionMode(MULTIPLE_INTERVAL_SELECTION)
                .setRenderer(new ColoredListCellRenderer<>() {
                    @Override
                    protected void customizeCellRenderer(@NotNull JList<? extends String> list,
                                                         String value,
                                                         int index,
                                                         boolean selected,
                                                         boolean hasFocus) {
                        this.append(value);
                        this.setIcon(icons.get(index));
                        this.setEnabled(!exports.contains(value));
                    }
                })
                .setItemsChosenCallback(new Consumer<Set<? extends String>>() {
                    @Override
                    public void consume(Set<? extends String> choices) {
                        // TODO: choices - exports 之后才是用户可以添加的
                        choices.forEach(System.out::println);
                    }
                })
                .setAdText("Ctrl+单击 选择多个，Shift+单击 批量选择")
                .setTitle("选择导出到 __all__ 的符号")
                .createPopup();

        popup.showInBestPositionFor(event.getDataContext());
    }
}
