package cn.aixcyi.plugin.tinysnake;

import com.intellij.icons.AllIcons.Nodes;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.ColoredListCellRenderer;
import com.jetbrains.python.PythonLanguage;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyElementGeneratorImpl;
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

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        PsiFile psi = event.getData(CommonDataKeys.PSI_FILE);
        Presentation presentation = event.getPresentation();
        presentation.setVisible(psi != null && psi.getLanguage() == PythonLanguage.INSTANCE);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        PsiFile psi = event.getData(CommonDataKeys.PSI_FILE);
        if (project == null || psi == null) return;
        PyFile file = (PyFile) psi;
        DunderAllEntity all = new DunderAllEntity(file);  // 遍历所有顶层表达式获取所有符号

        // TODO:
        //  JBList<Pair<String, Icon>> choices = new JBList<>(symbols);
        //  choices.setEmptyText("没有可公开的顶级符号");

        JBPopup popup = JBPopupFactory.getInstance()
                .createPopupChooserBuilder(all.symbols)
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
                .setItemsChosenCallback(choices -> all.adds(choices, project))
                .setAdText("Ctrl+单击 选择多个，Shift+单击 批量选择")
                .setTitle("选择导出到 __all__ 的符号")
                .createPopup();

        popup.showInBestPositionFor(event.getDataContext());
    }
}

class DunderAllEntity {
    public static String VAR_NAME = "__all__";  //  __all__ 变量的名称
    public PyExpression varValue = null;  // __all__ 变量的值
    public List<Icon> icons = new ArrayList<>();  // symbols 原对象的类型对应的符号
    public List<String> symbols = new ArrayList<>();  // 顶层所有符号
    public List<String> exports = new ArrayList<>();  // __all__ 导出的所有符号

    public DunderAllEntity(PyFile file) {
        file.getStatements().forEach(this::add);
        reexport();
    }

    private void add(PyStatement statement) {
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

                // 顶层的 __all__ 变量
                if (Objects.equals(varName, VAR_NAME)) {
                    varValue = target.second;
                }
                // 顶层除 __all__ 以外的普通变量
                else {
                    symbols.add(varName);
                    icons.add(Nodes.Variable);
                }
            }
        }
        // TODO: else if (statement instanceof PyIfStatement) {}
    }

    /**
     * 重新导出 __all__ 变量的值的所有子元素。
     */
    private void reexport() {
        exports.clear();
        if (varValue == null) return;
        for (PsiElement child : varValue.getChildren())
            if (child instanceof PyStringLiteralExpression string)
                exports.add(string.getStringValue());
    }

    /**
     * 往 __all__ 变量的值添加“字符串”。
     *
     * @param project 项目。
     * @param items   所有需要添加的"字符串"。
     */
    public void adds(@NotNull final Set<? extends String> items, @NotNull Project project) {
        reexport();
        PyElementGeneratorImpl generator = new PyElementGeneratorImpl(project);
        exports.forEach(items::remove);
        if (varValue == null) {
            items.forEach(System.out::println);
            // TODO: 创建 __all__ 变量。
        }
        WriteCommandAction.runWriteCommandAction(project, () -> {
            for (String item : items) {
                varValue.add(generator.createStringLiteralFromString(item));
            }
        });
    }
}
