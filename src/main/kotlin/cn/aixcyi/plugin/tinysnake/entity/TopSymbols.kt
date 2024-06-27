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
 * 类内部手动枚举所有符号，并递归筛选顶层符号。手动枚举是为了让不同类型的符号混合之后保持定义顺序。
 *
 * @param file 要查找的文件。
 * @param withImports 是否包括导入。因为存在 `from xxx import *` 这样的语句，所以搜索导入的话会消耗更多时间和内存。
 * @param specifiedIcon 固定所有符号对应的图标。仅供 [TopSymbols] 自身递归使用。
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
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

    /** 文件内所有顶层符号的名称。 */
    val names
        get() = symbols.keys.toList()

    /** 文件内所有顶层符号对应类型的图标。 */
    val icons
        get() = symbols.values.toList()

    init {
        file.statements.forEach { this.collect(it) }
    }

    /**
     * 参照文件内符号的定义顺序，按照自定义顺序 [order] 对 [list] 进行排序。
     */
    fun sort(list: MutableList<String>, order: Order = Order.APPEARANCE) {
        when (order) {
            Order.CHARSET -> list.sortWith { s1, s2 -> s1.compareTo(s2) }
            Order.ALPHABET -> list.sortWith { s1, s2 -> s1.compareTo(s2, ignoreCase = true) }
            Order.APPEARANCE -> list.sortWith { s1, s2 -> names.indexOf(s1).compareTo(names.indexOf(s2)) }
        }
    }

    /**
     * 移除 [list] 中不存在于文件内的符号。
     */
    fun remove(list: MutableList<String>) {
        for (index in list.indices.reversed()) {
            if (!symbols.keys.contains(list[index])) {
                list.removeAt(index)
            }
        }
    }

    /**
     * 解析并收集表达式 [statement] 的符号，包括
     *
     * - 类定义中的类名
     * - 函数定义中的函数名
     * - 赋值语句、判断语句中定义的变量名
     * - 导入语句所导入的符号名或其别名
     */
    private fun collect(statement: PyStatement) {
        // 类定义
        if (statement is PyClass) {
            val className = statement.name ?: return
            if (className.startsWith("_")) return
            symbols[className] = specifiedIcon ?: AllIcons.Nodes.Class
        }
        // 函数定义
        else if (statement is PyFunction) {
            val funcName = statement.name ?: return
            if (funcName.startsWith("_")) return
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
                    symbols[variableName] = specifiedIcon ?: AppIcons.Variable
                }
            }
        }
        // 判断语句
        else if (statement is PyIfStatement) {
            for (ps in statement.getIfPart().statementList.statements) {
                this.collect(ps)
            }
        }
        // from 根包.子包 import 孙包
        // from 根包.子包 import 孙包 as 别名
        // from 根包.子包.孙包 import 符号
        // from 根包.子包.孙包 import 符号 as 别名
        // from 根包.子包.孙包 import *
        else if (withImports && statement is PyFromImportStatement) {
            if (statement.isFromFuture)
                return
            if (statement.isStarImport) {
                this.parseFromStarImport(statement)
                return
            }
            for (element in statement.importElements) {
                symbols[element.visibleName ?: continue] = specifiedIcon ?: AllIcons.Nodes.Include
            }
        }
        // import 根包
        // import 根包.子包
        // import 根包.子包.孙包
        // import 根包.子包.孙包 as 孙包别名
        else if (withImports && statement is PyImportStatement) {
            for (element in statement.importElements) {
                symbols[element.visibleName ?: continue] = specifiedIcon ?: AllIcons.Nodes.Include
            }
        }
    }

    /**
     * 解析 `from xxx import *` 所导入的符号。
     *
     * - 如果包提供了 `__all__` 的话会直接合入这个列表。
     * - 如果包没有提供 `__all__` 则使用 [TopSymbols] 递归搜索顶级符号，递归层数见 [TopSymbols.LIMIT_FILE_VISITS_QTY]。
     */
    private fun parseFromStarImport(statement: PyFromImportStatement) {
        if (visitedFile.size >= LIMIT_FILE_VISITS_QTY) return
        val file = PyUtil.turnDirIntoInit(statement.resolveImportSource())
        if (file !is PyFile) return
        if (file in visitedFile) return
        visitedFile.add(file)

        if (file.dunderAll != null) {
            file.dunderAll!!.forEach {
                symbols[it] = specifiedIcon ?: AllIcons.Nodes.Include
            }
        } else {
            val entity = TopSymbols(file, withImports, AllIcons.Nodes.Include)
            symbols.putAll(entity.symbols)
        }
    }
}