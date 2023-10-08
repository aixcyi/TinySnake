package cn.aixcyi.plugin.tinysnake;

import com.intellij.psi.PsiComment;
import com.jetbrains.python.psi.LanguageLevel;
import com.jetbrains.python.psi.PyAssignmentStatement;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.impl.PyElementGeneratorImpl;
import com.jetbrains.python.psi.impl.PyExpressionStatementImpl;
import org.jetbrains.annotations.NotNull;

/**
 * Python 元素构造器。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
public class SnippetGenerator extends PyElementGeneratorImpl {
    private final LanguageLevel myLanguage;

    public SnippetGenerator(@NotNull PyFile file) {
        super(file.getProject());
        myLanguage = file.getLanguageLevel();
    }

    /**
     * 使用字符串构造特定类型的代码片段。
     *
     * @param type 代码片段类型。
     * @param text 代码片段字符串。
     * @return 代码片段对象。
     */
    @NotNull
    public <T> T createFromText(Class<T> type, String text) {
        return createFromText(myLanguage, type, text);
    }

    public PyExpressionStatementImpl createListLiteral(String expression) {
        return createFromText(
                myLanguage,
                PyExpressionStatementImpl.class,
                expression
        );
    }

    /**
     * 构造单行注释。
     *
     * @param comment 注释内容。
     * @return 注释对象（{@link PsiComment}）。
     */
    public PsiComment createSingleLineComment(@NotNull String comment) {
        return createFromText(
                myLanguage,
                PsiComment.class,
                comment.startsWith("# ") || comment.startsWith("#!")
                        ? comment
                        : comment.startsWith("#")
                        ? "# " + comment.substring(1)
                        : "# " + comment
        );
    }

    /**
     * 构造赋值表达式。
     *
     * @param variable 等号左侧（的变量名）。
     * @param value    等号右侧（的变量值）。
     * @return 赋值语句对象（{@link PyAssignmentStatement}）。
     */
    public PyAssignmentStatement createAssignment(@NotNull String variable, @NotNull String value) {
        return createFromText(
                myLanguage,
                PyAssignmentStatement.class,
                variable + " = " + value
        );
    }
}
