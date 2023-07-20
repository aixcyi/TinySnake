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
    public List<String> exports;  // __all__ 导出的所有符号
    public List<String> symbols = new ArrayList<>();  // 顶层所有符号
    public List<Icon> icons = new ArrayList<>();  // 顶层符号的类型所对应的符号
    private final PyFile file;

    public DunderAllEntity(@NotNull PyFile file) {
        this.file = file;
        this.file.getStatements().forEach(this::collect);
        this.exports = this.file.getDunderAll();
        this.exports = this.exports == null ? new ArrayList<>() : this.exports;
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
        // 判断语句
        else if (statement instanceof PyIfStatement declaration) {
            for (PyStatement ps : declaration.getIfPart().getStatementList().getStatements()) {
                collect(ps);
            }
        }
    }

    /**
     * 往 __all__ 变量的值添加“字符串”。
     *
     * @param project 项目。
     * @param items   所有需要添加的"字符串"。
     */
    public void adds(@NotNull final Set<? extends String> items, @NotNull Project project) {
        PyElementGeneratorImpl generator = new PyElementGeneratorImpl(project);
        if (varValue == null) {
            String soup = String.join(", ", items.stream().map(this::literalize).toList());
            String text = "__all__ = [" + soup + "]";
            WriteCommandAction.runWriteCommandAction(project, "生成 __all__", "GenerateDunderAll", () -> {
                PyAssignmentStatement s = generator.createFromText(file.getLanguageLevel(), PyAssignmentStatement.class, text);
                file.addBefore(s, findProperlyPlace());
            });
        } else {
            exports.forEach(items::remove);
            WriteCommandAction.runWriteCommandAction(project, "生成 __all__", "GenerateDunderAll", () -> {
                for (String item : items) {
                    varValue.add(generator.createStringLiteralFromString(item));
                }
            });
        }
    }

    /**
     * 确定 __all__ 变量的位置。
     *
     * @return 变量应该放在哪个元素的前面。
     * @see <a href="https://peps.python.org/pep-0008/#module-level-dunder-names">PEP 8 - 模块级别 Dunder 的布局位置</a>
     */
    private @NotNull PsiElement findProperlyPlace() {
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

            // 其它语句都排在 __all__ 后面，
            return child;
        }
        return file.getFirstChild();
    }

    private String literalize(@NotNull String text) {
        return "\"" + text + "\"";
    }
}
