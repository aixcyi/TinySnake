package cn.aixcyi.plugin.tinysnake

import com.intellij.psi.PsiComment
import com.jetbrains.python.psi.PyAssignmentStatement
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.impl.PyElementGeneratorImpl
import com.jetbrains.python.psi.impl.PyExpressionStatementImpl

/**
 * Python 元素构造器。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
class SnippetGenerator(file: PyFile) : PyElementGeneratorImpl(file.project) {
    private val myLanguage = file.languageLevel

    /**
     * 使用字符串构造特定类型的代码片段。
     *
     * @param type 代码片段类型。
     * @param code 代码片段字符串。
     * @return 代码片段对象。
     */
    fun <T> createFromText(type: Class<T>, code: String): T = createFromText(myLanguage, type, code)

    /**
     * 构造列表字面值。
     */
    fun createListLiteral(code: String) = createFromText(PyExpressionStatementImpl::class.java, code)

    /**
     * 构造单行注释。
     *
     * @param comment 注释内容。
     * @return 注释对象 [PsiComment] 。
     */
    fun createSingleLineComment(comment: String) = createFromText(
        myLanguage,
        PsiComment::class.java,
        if (comment.startsWith("# ") || comment.startsWith("#!")) comment
        else
            if (!comment.startsWith("#")) "# $comment"
            else "# " + comment.substring(1)
    )

    /**
     * 构造赋值表达式。
     *
     * @param variable 等号左侧（的变量名）。
     * @param value    等号右侧（的变量值）。
     * @return 赋值语句对象 [PyAssignmentStatement] 。
     */
    fun createAssignment(variable: String, value: String) = createFromText(
        myLanguage,
        PyAssignmentStatement::class.java,
        "$variable = $value"
    )
}