package cn.aixcyi.plugin.tinysnake.entity

import cn.aixcyi.plugin.tinysnake.AppIcons
import cn.aixcyi.plugin.tinysnake.storage.DunderAllOptimization.Order
import com.intellij.icons.AllIcons
import com.jetbrains.python.PyNames
import com.jetbrains.python.psi.*
import javax.swing.Icon

/**
 * 文件内的所有顶层符号。
 *
 * @param file 要查找的文件。
 * @param withImports 是否包括导入。因为存在 `from xxx import *` 这样的语句，所以搜索导入的话会消耗更多时间和内存。
 * @param specifiedIcon 固定所有符号对应的图标。仅供 [TopSymbols] 自身递归使用。
 */
class TopSymbols(
    file: PyFile,
    private val withImports: Boolean = false,
    private val specifiedIcon: Icon? = null,  // 在递归查找导入的符号时，用于改变找到的项目的图标
) {

    companion object {
        private const val LIMIT_FILE_VISITS_QTY = 10
    }

    private val symbols = mutableMapOf<String, Icon>()
    private val visitedFile = mutableSetOf<PyFile>()

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
            symbols[className] = specifiedIcon ?: AllIcons.Nodes.Class
        }
        // 函数定义
        else if (statement is PyFunction) {
            val funcName = statement.getName() ?: return
            if (statement.protectionLevel != PyFunction.ProtectionLevel.PUBLIC) return
            symbols[funcName] = specifiedIcon ?: AllIcons.Nodes.Function
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
                    symbols[variableName] = specifiedIcon ?: AllIcons.Nodes.Variable
                }
                // 除了 __all__ 以外的特殊变量
                else if (PyNames.UNDERSCORED_ATTRIBUTES.contains(variableName) && PyNames.ALL != variableName) {
                    symbols[variableName] = specifiedIcon ?: AppIcons.Nodes.Variable
                }
            }
        }
        // 判断语句
        else if (statement is PyIfStatement) {
            for (ps in statement.getIfPart().statementList.statements) {
                collect(ps)
            }
        }
        // import 符号 as 别名
        // import 符号
        // import xxx.yyy.zzz.符号  实际上就是导入  xxx
        else if (withImports && statement is PyImportStatement) {
            for (element in statement.importElements) {
                symbols[element.asName ?: element.visibleName ?: continue] = specifiedIcon ?: AllIcons.Nodes.Include
            }
        }
        // from xxx import 符号 as 别名
        // from xxx import 符号
        // from xxx import *
        else if (withImports && statement is PyFromImportStatement) {
            if (statement.isFromFuture) return
            if (statement.isStarImport) {
                this.collect(statement)
                return
            }
            for (element in statement.importElements) {
                symbols[element.asName ?: element.visibleName ?: continue] = specifiedIcon ?: AllIcons.Nodes.Include
            }
        }
    }

    /**
     * 解析 `from xxx import *` 所导入的符号。
     *
     * - 如果包提供了 `__all__` 的话会直接合入这个列表。
     * - 如果包没有提供 `__all__` 则使用 [TopSymbols] 递归搜索顶级符号，递归层数见 [TopSymbols.LIMIT_FILE_VISITS_QTY]。
     */
    private fun collect(statement: PyFromImportStatement) {
        if (visitedFile.size >= LIMIT_FILE_VISITS_QTY) return
        val file = PyUtil.turnDirIntoInit(statement.resolveImportSource())
        if (file !is PyFile) return
        if (file in visitedFile) return else visitedFile.add(file)
        if (file.dunderAll != null) {
            file.dunderAll!!.forEach { symbols[it] = specifiedIcon ?: AllIcons.Nodes.Include }
        } else {
            val entity = TopSymbols(file, withImports, AllIcons.Nodes.Include)
            symbols.putAll(entity.symbols)
        }
    }
}