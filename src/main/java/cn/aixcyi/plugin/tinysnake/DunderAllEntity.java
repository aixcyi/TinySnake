package cn.aixcyi.plugin.tinysnake;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.QualifiedName;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyElementGeneratorImpl;
import com.jetbrains.python.psi.impl.PyExpressionStatementImpl;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DunderAllEntity {
    public static String VARIABLE_NAME = "__all__";  //  __all__ 的名称
    public PyTargetExpression variable;  // __all__ 自身
    public List<String> exports;  // __all__ 导出的所有符号
    public List<String> symbols = new ArrayList<>();  // 顶层所有符号
    public List<Icon> icons = new ArrayList<>();  // 顶层符号的类型所对应的符号
    private final PyFile file;

    public DunderAllEntity(@NotNull PyFile file) {
        this.file = file;
        this.file.getStatements().forEach(this::collect);
        this.variable = file.findTopLevelAttribute(VARIABLE_NAME);
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
            String className = statement.getName();
            if (className == null || className.startsWith("_")) return;
            symbols.add(className);
            icons.add(AllIcons.Nodes.Class);
        }
        // 函数定义
        else if (statement instanceof PyFunction function) {
            if (function.getProtectionLevel() != PyFunction.ProtectionLevel.PUBLIC) return;
            symbols.add(statement.getName());
            icons.add(AllIcons.Nodes.Function);
        }
        // 赋值表达式
        else if (statement instanceof PyAssignmentStatement assignment) {
            // 因为赋值表达式存在元组解包的情况，
            // 所以需要用循环来提取普通变量
            for (Pair<PyExpression, PyExpression> pair : assignment.getTargetsToValuesMapping()) {
                String varName = pair.first.getName();
                if (varName == null) continue;
                // 过滤掉附属的变量，比如 meow.age = 6
                if (pair.first instanceof PyTargetExpression target) {
                    QualifiedName qName = target.asQualifiedName();
                    if (qName != null && !qName.toString().equals(varName)) {
                        continue;
                    }
                }

                // 除了 __all__ 以外的 dunder 变量
                if (varName.startsWith("__") && !varName.equals(VARIABLE_NAME)) {
                    symbols.add(varName);
                    icons.add(AllIcons.Nodes.Variable);
                    // TODO: 图标与【结构】中的对不上
                    // 它的图标附加了 Visibility modifiers，见
                    // https://www.jetbrains.com/help/pycharm/symbols.html#common-icons
                }
                // 公开变量
                else if (!varName.startsWith("_")) {
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
     * 按照指定顺序对 __all__ 中的符号重新排序。
     *
     * @param order 排序方式。
     */
    public void sort(SymbolsOrder order, boolean alone) {
        Project project = file.getProject();
        if (order == null) return;
        if (variable == null) return;
        if (!(variable.findAssignedValue() instanceof PyListLiteralExpression list)) return;

        ArrayList<String> exporting = new ArrayList<>(exports);
        switch (order) {
            case CHARSET -> exporting.sort(String::compareTo);
            case ALPHABET -> exporting.sort(String::compareToIgnoreCase);
            case APPEARANCE -> exporting.sort((s1, s2) -> Integer.compare(symbols.indexOf(s1), symbols.indexOf(s2)));
        }
        String soup = String.join(alone ? ",\n" : ", ", exporting.stream().map(this::literalize).toList());
        String text = "[\n" + soup + "\n]";

        PyElementGeneratorImpl generator = new PyElementGeneratorImpl(project);
        Runnable runnable = () -> list.replace(
                generator.createFromText(file.getLanguageLevel(), PyExpressionStatementImpl.class, text)
        );
        WriteCommandAction.runWriteCommandAction(
                project, "优化 __all__", "OptimizeDunderAll", runnable
        );
    }

    /**
     * 往 __all__ 变量的值添加“字符串”。如果没有这个变量，就找个合适的地方创建之。
     *
     * @param items 所有需要添加的"字符串"。
     */
    public void patch(@NotNull final Set<? extends String> items) {
        Project project = file.getProject();
        Runnable runnable;
        PyExpression list = variable == null ? null : variable.findAssignedValue();
        PyElementGeneratorImpl generator = new PyElementGeneratorImpl(project);

        // 没有定义 __all__ ，或者赋的值不是列表的情况下，直接新建一个 __all__ 。
        // 因为后者的情况太复杂了，无法简洁地一概而论。
        if (variable == null || !(list instanceof PyListLiteralExpression)) {
            ArrayList<String> choices = new ArrayList<>(items);
            choices.sort((c1, c2) -> Integer.compare(symbols.indexOf(c1), symbols.indexOf(c2)));
            String soup = String.join(", ", choices.stream().map(this::literalize).toList());
            String text = "__all__ = [\n" + soup + "\n]";
            runnable = () -> file.addBefore(
                    generator.createFromText(file.getLanguageLevel(), PyAssignmentStatement.class, text),
                    findProperlyPlace()
            );
        } else {
            exports.forEach(items::remove);  // 去除已经在 __all__ 里的符号
            ArrayList<String> choices = new ArrayList<>(items);
            choices.sort((c1, c2) -> Integer.compare(symbols.indexOf(c1), symbols.indexOf(c2)));
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

            // 根据 PEP 8 的格式约定，其它语句都要排在 __all__ 后面
            return child;
        }
        return file.getFirstChild();
    }

    /**
     * 将字符串变为字面量字符串。
     *
     * @param text 任意字符串。
     * @return 带双引号的字符串。
     */
    private @NotNull String literalize(@NotNull String text) {
        return "\"" + text + "\"";
    }
}
