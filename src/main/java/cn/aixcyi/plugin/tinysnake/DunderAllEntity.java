package cn.aixcyi.plugin.tinysnake;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyElementGeneratorImpl;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class DunderAllEntity {
    public static String VAR_NAME = "__all__";  //  __all__ 变量的名称
    public PyExpression varValue = null;  // __all__ 变量的值
    public List<Icon> icons = new ArrayList<>();  // symbols 原对象的类型对应的符号
    public List<String> symbols = new ArrayList<>();  // 顶层所有符号
    public List<String> exports = new ArrayList<>();  // __all__ 导出的所有符号

    public DunderAllEntity(PyFile file) {
        file.getStatements().forEach(this::collect);
        reexport();
    }

    /**
     * 解析表达式，并收集该表达式的符号（类定义的类名、函数定义的函数名、赋值语句的变量名等）。
     *
     * @param statement 一条顶层表达式。
     */
    private void collect(PyStatement statement) {
        // 类定义
        if (statement instanceof PyClass) {
            symbols.add(statement.getName());
            icons.add(AllIcons.Nodes.Class);
        }
        // 函数定义
        else if (statement instanceof PyFunction) {
            symbols.add(statement.getName());
            icons.add(AllIcons.Nodes.Method);
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
                    icons.add(AllIcons.Nodes.Variable);
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
