package cn.aixcyi.plugin.tinysnake.action

import cn.aixcyi.plugin.tinysnake.SnippetGenerator
import cn.aixcyi.plugin.tinysnake.Zoo.message
import com.intellij.codeInsight.hint.HintManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.jetbrains.python.PyNames
import com.jetbrains.python.psi.*
import com.jetbrains.python.psi.impl.PyExpressionStatementImpl

class DictAndCallConvertAction : PyAction() {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        // 仅当光标在 dict 字典或 dict() 调用内时才启用
        val psi = event.getData(CommonDataKeys.PSI_FILE)
        if (psi is PyFile) {
            val calling = getCaretElement(event, psi, PyCallExpression::class.java)
            val literal = getCaretElement(event, psi, PyDictLiteralExpression::class.java)
            if (calling != null || literal != null) {
                event.presentation.isEnabled = true
                return
            }
        }
        event.presentation.isEnabled = false
    }

    override fun actionPerformed(event: AnActionEvent, file: PyFile, editor: Editor) {
        val hint = HintManager.getInstance()
        val generator = SnippetGenerator(file)

        // 查找光标附近的 dict() 或 dict
        val calling = getCaretElement(event, file, PyCallExpression::class.java)
        val literal = getCaretElement(event, file, PyDictLiteralExpression::class.java)
        val isDictCall = calling != null && calling.name == "dict"

        // 两者都不存在
        if (!isDictCall && literal == null) {
            hint.showInformationHint(editor, message("hint.ConvertDictCall.notfound"))
            return
        }
        // 两者都存在，那么谁的位置更靠后，谁就是被嵌套的
        val isCall2Dict =
            if (isDictCall && literal != null) literal.textOffset < calling!!.textOffset
            else isDictCall

        // 将 dict() 调用转换为 dict 字面值
        if (isCall2Dict) {
            val snippet = StringBuilder("{\n")
            for (argument in calling!!.arguments) {
                // 关键字参数
                if (argument is PyKeywordArgument) {
                    val kRaw = argument.keyword
                    val vRaw = argument.valueExpression
                    if (kRaw == null || vRaw == null) {
                        editor.caretModel.moveToOffset(argument.getTextOffset())
                        hint.showErrorHint(editor, message("hint.ConvertDictCall.syntax"))
                        return
                    }
                    val k = generator.createStringLiteralFromString(kRaw).text
                    val v = vRaw.text
                    snippet.append(k).append(": ").append(v).append(",\n")
                }
                // 字典解包
                else if (argument is PyStarArgument && argument.isKeyword) {
                    snippet.append(argument.getText()).append(",\n")
                }
                // 位置参数
                else {
                    editor.caretModel.moveToOffset(argument.textOffset)
                    hint.showErrorHint(editor, message("hint.ConvertDictCall.unpack"))
                    return
                }
            }
            val statement = generator.createFromText(
                PyExpressionStatementImpl::class.java,
                snippet.append("}").toString()
            )
            WriteCommandAction.runWriteCommandAction(
                file.project,
                message("command.ConvertDictCallToData"),
                null,
                { calling.replace(statement) }
            )
        }
        // 将 dict 字面值转为 dict() 调用
        else {
            val snippet = StringBuilder("dict(\n")
            val kwargs = StringBuilder("**{\n")
            for (e in literal!!.elements) {
                val key = e.key
                val `val` = e.value
                if (key !is PyStringLiteralExpression || `val` == null) {
                    val start = e.textOffset
                    val stop = start + key.textLength
                    editor.selectionModel.setSelection(start, stop)
                    hint.showErrorHint(editor, message("hint.ConvertDictCall.syntax"))
                    return
                }
                val keyName = key.stringValue
                if (PyNames.isIdentifier(keyName)) snippet.append(key).append('=').append(`val`.text).append(",\n")
                else kwargs
                    .append(generator.createStringLiteralFromString(keyName).text)
                    .append(": ")
                    .append(`val`.text)
                    .append(",\n")
            }
            if ("**{\n".contentEquals(kwargs)) {
                snippet.append(")")
            } else {
                kwargs.append("}\n")
                snippet.append(kwargs).append(")")
            }
            val statement = generator.createFromText(
                PyExpressionStatementImpl::class.java,
                snippet.toString()
            )
            WriteCommandAction.runWriteCommandAction(
                file.project,
                message("command.ConvertDictDataToCall"),
                null,
                { literal.replace(statement) }
            )
        }
    }
}