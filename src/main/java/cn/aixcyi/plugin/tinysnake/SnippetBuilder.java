package cn.aixcyi.plugin.tinysnake;

import com.intellij.psi.PsiComment;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyElementGeneratorImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Python 代码片段构造工具。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
public class SnippetBuilder {
    private final LanguageLevel version;
    private final PyElementGeneratorImpl generator;

    public SnippetBuilder(@NotNull PyFile file) {
        this.version = file.getLanguageLevel();
        this.generator = new PyElementGeneratorImpl(file.getProject());
    }

    /**
     * 拼接序列。
     *
     * @param items      序列元素。
     * @param style      括号风格。
     * @param lineByLine 每个元素一行，而不是全部挤在同一行。
     * @param stringify  为每个元素配上双引号。
     * @return 拼接后的结果字符串。
     */
    public String makeSequence(@NotNull List<String> items,
                               @NotNull BracketsStyle style,
                               boolean lineByLine,
                               boolean stringify) {
        var soup = stringify ? items.stream().map((s -> "\"" + s + "\"")).toList() : items;
        return style.wrap(String.join(lineByLine ? ",\n" : ", ", soup));
    }

    /**
     * 创建字符串字面值。
     *
     * @param string 字符串内容。不必包含引号。
     * @return 字符串字面值对象（{@link PyStringLiteralExpression}）。
     */
    public PyStringLiteralExpression cakeString(@NotNull String string) {
        return generator.createStringLiteralFromString(string);
    }

    /**
     * 创建备注。
     *
     * @param comment 注释内容。需要包含老头的 "#" 号。
     * @return 注释对象（{@link PsiComment}）。
     */
    public PsiComment cakeComment(@NotNull String comment) {
        return generator.createFromText(this.version, PsiComment.class, comment);
    }

    /**
     * 创建列表。
     *
     * @param items      要添加到列表中的元素。
     * @param lineByLine 每个元素各占一行。
     * @param stringify  将所有元素变为字符串字面值。
     * @return 列表对象（{@link PyListLiteralExpression}）。
     */
    public PyListLiteralExpression cakeList(@NotNull List<String> items, boolean lineByLine, boolean stringify) {
        return generator.createFromText(
                this.version,
                PyListLiteralExpression.class,
                makeSequence(items, BracketsStyle.BRANCH_LIST, lineByLine, stringify)
        );
    }

    /**
     * 创建赋值表达式。
     *
     * @param variable 变量名
     * @param value    变量值
     * @return 赋值语句对象（{@link PyAssignmentStatement}）。
     */
    public PyAssignmentStatement cakeAssignment(@NotNull String variable, @NotNull String value) {
        return generator.createFromText(
                this.version,
                PyAssignmentStatement.class,
                variable + " = " + value
        );
    }

    /**
     * 创建函数。
     *
     * @param name      函数名。
     * @param arguments 参数列表。
     * @param body      函数体。如果留空则默认为 "pass" 。
     * @param docstring 文档字符串，需要手动添加三双引号和必须的换行。若留空则不会显示。
     * @return 函数对象（{@link PyFunction}）。
     */
    public PyFunction cakeFunction(@NotNull String name,
                                   @Nullable List<String> arguments,
                                   @Nullable String body,
                                   @Nullable String docstring) {
        var params = arguments == null ? "" : makeSequence(arguments, BracketsStyle.BARE, false, false);
        var text = "def " + name + "(" + params + "):\n"
                + (docstring == null ? "" : docstring)
                + (body == null ? "pass" : body);
        return this.generator.createFromText(this.version, PyFunction.class, text);
    }
}
