package cn.aixcyi.plugin.tinysnake.entity

import com.jetbrains.python.PyNames
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.PyListLiteralExpression
import com.jetbrains.python.psi.PySetLiteralExpression
import com.jetbrains.python.psi.PyTupleExpression

/**
 * `__all__` 实体。
 *
 * 用于聚合 `__all__` 导出的符号、表达式、表达式的值等。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
class DunderAll(file: PyFile) {

    /** 通过 `__all__` 导出的所有符号。 */
    val exports = file.dunderAll ?: listOf()

    /** `__all__` 赋值表达式。 */
    val expression = file.findTopLevelAttribute(PyNames.ALL)

    /** `__all__` 赋值表达式中的值的部分。 */
    val assignment = expression?.findAssignedValue()

    /**
     * `__all__` 的值是否合法。
     *
     * @see <a href="https://docs.python.org/3/reference/simple_stmts.html#the-import-statement">The <code>import</code> statement</a>
     */
    val isValidAssignment
        get() = when (assignment) {
            // __all__ 的值必须是 a sequence of strings，
            // 也就是说列表、元组、集合都可以视为合法值。
            is PyListLiteralExpression,
            is PySetLiteralExpression,
            is PyTupleExpression -> true
            // 注意这里判断的是字面值，所以没有其它 sequence 类型。
            else -> false
        }
}