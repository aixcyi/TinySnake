package cn.aixcyi.plugin.tinysnake;

import com.intellij.psi.PsiComment;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyElementGeneratorImpl;
import org.jetbrains.annotations.NotNull;

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

    public String makeSequence(@NotNull List<String> items,
                               @NotNull BracketsStyle style,
                               boolean lineByLine,
                               boolean stringify) {
        var soup = stringify ? items.stream().map((s -> "\"" + s + "\"")).toList() : items;
        return style.wrap(String.join(lineByLine ? ",\n" : ", ", soup));
    }

    /**
     * 创建字符串字面值。
     */
    public PyStringLiteralExpression cakeString(@NotNull String string) {
        return generator.createStringLiteralFromString(string);
    }

    /**
     * 创建备注。
     */
    public PsiComment cakeComment(@NotNull String comment) {
        return generator.createFromText(this.version, PsiComment.class, comment);
    }

    /**
     * 创建列表。
     *
     * @param items 要添加到列表中的元素。
     * @param lineByLine 每个元素各占一行。
     * @param stringify 将所有元素变为字符串字面值。
     * @return 表达式对象。
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
     */
    public PyAssignmentStatement cakeAssignment(@NotNull String variable, @NotNull String value) {
        return generator.createFromText(
                this.version,
                PyAssignmentStatement.class,
                variable + " = " + value
        );
    }
}
