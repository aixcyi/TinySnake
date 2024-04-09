package cn.aixcyi.plugin.tinysnake.entity

import cn.aixcyi.plugin.tinysnake.AppIcons
import cn.aixcyi.plugin.tinysnake.storage.DunderAllOptimization.Order
import com.intellij.icons.AllIcons
import com.jetbrains.python.PyNames
import com.jetbrains.python.psi.*
import javax.swing.Icon

/**
 * 文件内的所有顶层符号。
 */
class TopSymbols(file: PyFile) {

    private val symbols = mutableMapOf<String, Icon>()

    /**
     * 文件内所有顶层符号的名称。
     */
    val names
        get() = symbols.keys.toList()

    /**
     * 文件内所有顶层符号对应类型的图标。
     */
    val icons
        get() = symbols.values.toList()

    init {
        // 手动枚举是为了保持符号定义顺序
        file.statements.forEach { this.collect(it) }
    }

    /**
     * 参照文件内符号的定义顺序，对给定的一批符号进行排序。
     *
     * @param list 符号。
     * @param order 顺序。
     */
    fun sort(list: MutableList<String>, order: Order = Order.APPEARANCE) {
        when (order) {
            Order.CHARSET -> list.sortWith { s1, s2 -> s1.compareTo(s2) }
            Order.ALPHABET -> list.sortWith { s1, s2 -> s1.compareTo(s2, ignoreCase = true) }
            Order.APPEARANCE -> list.sortWith { s1, s2 -> names.indexOf(s1).compareTo(names.indexOf(s2)) }
        }
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
            symbols[className] = AllIcons.Nodes.Class
        }
        // 函数定义
        else if (statement is PyFunction) {
            val funcName = statement.getName() ?: return
            if (statement.protectionLevel != PyFunction.ProtectionLevel.PUBLIC) return
            symbols[funcName] = AllIcons.Nodes.Function
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
                    symbols[variableName] = AllIcons.Nodes.Variable
                }
                // 除了 __all__ 以外的特殊变量
                else if (PyNames.UNDERSCORED_ATTRIBUTES.contains(variableName) && PyNames.ALL != variableName) {
                    symbols[variableName] = AppIcons.Nodes.Variable
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
}