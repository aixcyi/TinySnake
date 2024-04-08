package cn.aixcyi.plugin.tinysnake.entity

import cn.aixcyi.plugin.tinysnake.AppIcons
import cn.aixcyi.plugin.tinysnake.storage.DunderAllOptimization.Order
import com.intellij.icons.AllIcons
import com.jetbrains.python.PyNames
import com.jetbrains.python.psi.*
import javax.swing.Icon

class DunderAll(file: PyFile) {

    /** `__all__` 变量自身。 */
    var variable: PyTargetExpression? = null

    /** `__all__` 导出的所有符号。 */
    var exports = listOf<String>()

    /** 顶层所有符号。 */
    var symbols = mutableListOf<String>()

    /** 顶层符号的类型所对应的符号。 */
    var icons = mutableListOf<Icon>()

    init {
        file.statements.forEach { this.collect(it) }
        this.variable = file.findTopLevelAttribute(PyNames.ALL)
        this.exports = file.dunderAll ?: ArrayList()
    }

    /**
     * 解析表达式，并收集该表达式的符号（类定义的类名、函数定义的函数名、赋值语句的变量名等）。
     *
     * @param statement 一条顶层表达式。
     */
    private fun collect(statement: PyStatement) {
        // 类定义
        if (statement is PyClass) {
            val className = statement.getName() ?: return
            if (className.startsWith("_")) return
            symbols.add(className)
            icons.add(AllIcons.Nodes.Class)
        }
        // 函数定义
        else if (statement is PyFunction) {
            val funcName = statement.getName() ?: return
            if (statement.protectionLevel != PyFunction.ProtectionLevel.PUBLIC) return
            symbols.add(funcName)
            icons.add(AllIcons.Nodes.Function)
        }
        // 赋值表达式
        else if (statement is PyAssignmentStatement) {
            // 因为赋值表达式存在元组解包的情况，
            // 所以需要用循环来提取普通变量
            for (pair in statement.targetsToValuesMapping) {
                val variable = pair.first
                val variableName = pair.first.name ?: continue
                // 过滤掉附属的变量，比如 meow.age = 6
                if (variable is PyTargetExpression) {
                    val qName = variable.asQualifiedName()
                    if (qName != null && qName.toString() != variableName) {
                        continue
                    }
                }
                // 公开变量
                if (!variableName.startsWith("_")) {
                    symbols.add(variableName)
                    icons.add(AllIcons.Nodes.Variable)
                }
                // 除了 __all__ 以外的特殊变量
                else if (PyNames.UNDERSCORED_ATTRIBUTES.contains(variableName) && PyNames.ALL != variableName) {
                    symbols.add(variableName)
                    icons.add(AppIcons.Nodes.Variable)
                }
            }
        }
        // 判断语句
        else if (statement is PyIfStatement) {
            for (ps in statement.getIfPart().statementList.statements) {
                collect(ps)
            }
        }
    }

    /**
     * 对给定的一批符号进行排序。
     *
     * @param list  给定的一批符号。
     * @param order 排序顺序。
     * @return list 自身。
     */
    fun sort(list: MutableList<String>, order: Order): List<String> {
        when (order) {
            Order.CHARSET -> list.sortWith { a, b -> a.compareTo(b) }
            Order.ALPHABET -> list.sortWith { a, b -> a.compareTo(b, ignoreCase = true) }
            Order.APPEARANCE -> list.sortWith { s1, s2 -> symbols.indexOf(s1).compareTo(symbols.indexOf(s2)) }
        }
        return list
    }

    /**
     * 获取 `__all__` 变量的值。如果变量不存在，或值的类型不是列表或元组，则返回 `null` 。
     *
     * @return 值表达式对象。
     */
    fun getVariableValue(): PyExpression? {
        if (variable != null) {
            val exp = variable!!.findAssignedValue()
            if (exp is PyListLiteralExpression || exp is PyTupleExpression) {
                return exp
            }
        }
        return null
    }
}