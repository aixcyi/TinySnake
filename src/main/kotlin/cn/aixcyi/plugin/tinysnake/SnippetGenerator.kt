package cn.aixcyi.plugin.tinysnake

import cn.aixcyi.plugin.tinysnake.storage.DunderAllOptimization
import com.intellij.psi.PsiComment
import com.jetbrains.python.psi.PyAssignmentStatement
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.impl.PyElementGeneratorImpl
import com.jetbrains.python.psi.impl.PyExpressionStatementImpl

/**
 * Python 代码片段构造器。
 *
 * - 静态方法用于构造字符串，动态方法用于构造代码对象。
 * - 这是一个专门辅助该插件的工具类，并不是通用的构造工具.对于通用的构造，见 [createFromText]。
 *
 * @param file Python 文件。
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
class SnippetGenerator(file: PyFile) {
    private val myLanguage = file.languageLevel
    private val proxy = PyElementGeneratorImpl(file.project)

    companion object {
        /**
         * 构造仅包含 `string` 的带格式的 `list` 字面值。
         *
         * @param strings `list` 中的各个字符串。不必也不能预先添加引号。
         * @param state 格式化参数。
         * @return 字面值所对应的字符串。
         */
        fun stringList(
            strings: List<String>,
            state: DunderAllOptimization.State = DunderAllOptimization.getInstance().state
        ): String {
            val quote = if (state.isUseSingleQuote) "'" else "\""
            return if (state.isEndsWithComma)
                strings.joinToString(
                    separator = if (state.isLineByLine) "\n" else " ",
                    postfix = if (state.isLineByLine) "\n]" else "]",
                    prefix = if (state.isLineByLine) "[\n" else "[",
                ) { "$quote$it$quote," }
            else
                strings.joinToString(
                    separator = if (state.isLineByLine) ",\n" else ", ",
                    postfix = if (state.isLineByLine) "\n]" else "]",
                    prefix = if (state.isLineByLine) "[\n" else "[",
                ) { "$quote$it$quote" }
        }
    }

    /**
     * 使用字符串构造特定类型的代码片段。
     *
     * @param type 代码片段类型。
     * @param code 代码片段字符串。
     * @return 代码片段对象。
     */
    fun <T> createFromText(type: Class<T>, code: String): T = proxy.createFromText(myLanguage, type, code)

    /**
     * 构造仅包含 `string` 的带格式的 `list` 的字面值。
     *
     * @param strings `list` 中的各个字符串。不必也不能预先添加引号。
     * @param state 格式化参数。
     * @return 表达式语句对象。
     */
    fun createStringListLiteral(
        strings: List<String>,
        state: DunderAllOptimization.State = DunderAllOptimization.getInstance().state
    ): PyExpressionStatementImpl {
        val code = stringList(strings, state)
        return createFromText(PyExpressionStatementImpl::class.java, code)
    }

    /**
     * 构造单行注释。
     *
     * - 以 `"#"` 开头会被规范为 `"# "` 。
     * - 以 `"# "` 或 `"#!"` 开头则不会在开头重复添加 `"# "` 。
     *
     * @param comment 注释内容。
     * @return 注释对象。
     */
    fun createSingleLineComment(comment: String) = createFromText(
        PsiComment::class.java,
        if (comment.startsWith("# ") || comment.startsWith("#!")) comment
        else
            if (!comment.startsWith("#")) "# $comment"
            else "# " + comment.substring(1)
    )

    /**
     * 构造单个变量的赋值表达式。
     *
     * @param variable 变量名。
     * @param value 变量值。
     * @return 赋值语句对象。
     */
    fun createAssignment(variable: String, value: String) = createFromText(
        PyAssignmentStatement::class.java,
        "$variable = $value"
    )

    fun createStringLiteralFromString(unescaped: String) = proxy.createStringLiteralFromString(unescaped)!!
}