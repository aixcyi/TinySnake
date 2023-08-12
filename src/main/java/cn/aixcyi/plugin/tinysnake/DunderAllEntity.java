package cn.aixcyi.plugin.tinysnake;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.util.QualifiedName;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DunderAllEntity {
    /** __all__ 变量自身。*/
    public @Nullable PyTargetExpression variable;
    /** __all__ 导出的所有符号。*/
    public @NotNull List<String> exports;
    /** 顶层所有符号。*/
    public @NotNull List<String> symbols = new ArrayList<>();
    /** 顶层符号的类型所对应的符号。*/
    public @NotNull List<Icon> icons = new ArrayList<>();

    public DunderAllEntity(@NotNull PyFile file) {
        file.getStatements().forEach(this::collect);
        this.variable = file.findTopLevelAttribute(PyNames.ALL);
        try {
            this.exports = Objects.requireNonNull(file.getDunderAll());
        } catch (NullPointerException e) {
            this.exports = new ArrayList<>();
        }
    }

    /**
     * 解析表达式，并收集该表达式的符号（类定义的类名、函数定义的函数名、赋值语句的变量名等）。
     *
     * @param statement 一条顶层表达式。
     */
    private void collect(PyStatement statement) {
        // 类定义
        if (statement instanceof PyClass) {
            var className = statement.getName();
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
                var varName = pair.first.getName();
                if (varName == null) continue;
                // 过滤掉附属的变量，比如 meow.age = 6
                if (pair.first instanceof PyTargetExpression target) {
                    QualifiedName qName = target.asQualifiedName();
                    if (qName != null && !qName.toString().equals(varName)) {
                        continue;
                    }
                }
                // 公开变量
                if (!varName.startsWith("_")) {
                    symbols.add(varName);
                    icons.add(AllIcons.Nodes.Variable);
                }
                // 除了 __all__ 以外的特殊变量
                else if (PyNames.UNDERSCORED_ATTRIBUTES.contains(varName) && !PyNames.ALL.equals(varName)) {
                    symbols.add(varName);
                    icons.add(PythonIcons.Nodes.Variable);
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
     * 对给定的一批符号进行排序。
     *
     * @param list     给定的一批符号。
     * @param ordering 排序顺序。
     * @return list 自身。
     */
    public List<String> sort(@NotNull List<String> list, @NotNull SequenceOrder ordering) {
        switch (ordering) {
            case CHARSET -> list.sort(String::compareTo);
            case ALPHABET -> list.sort(String::compareToIgnoreCase);
            case APPEARANCE -> list.sort((s1, s2) -> Integer.compare(symbols.indexOf(s1), symbols.indexOf(s2)));
        }
        return list;
    }

    /**
     * 获取 __all__ 变量的值。如果变量不存在，或值的类型不是列表或元组，则返回 null 。
     *
     * @return 值表达式对象。
     */
    public PyExpression getVariableValue() {
        if (variable != null) {
            var exp = variable.findAssignedValue();
            if (exp instanceof PyListLiteralExpression || exp instanceof PyTupleExpression) {
                return exp;
            }
        }
        return null;
    }
}
