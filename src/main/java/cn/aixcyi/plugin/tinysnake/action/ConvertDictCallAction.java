package cn.aixcyi.plugin.tinysnake.action;

import cn.aixcyi.plugin.tinysnake.SnippetGenerator;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyExpressionStatementImpl;
import org.jetbrains.annotations.NotNull;

import static cn.aixcyi.plugin.tinysnake.Translation.$message;

/**
 * 将函数调用 {@code dict(key=value)} 转换为字典字面量 {@code {"key": value}}。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
public class ConvertDictCallAction extends PyAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event, @NotNull PyFile file, @NotNull Editor editor) {
        var hint = HintManager.getInstance();
        var generator = new SnippetGenerator(file);

        // 查找光标附近的 dict() 或 dict
        var calling = getCaretElement(event, file, PyCallExpression.class);
        var literal = getCaretElement(event, file, PyDictLiteralExpression.class);

        // 两者都不存在
        if (calling == null && literal == null) {
            hint.showInformationHint(editor, $message("hint.ConvertDictCallAction.notfound"));
            return;
        }
        // 两者都存在，那么谁的位置更靠后，谁就是被嵌套的
        boolean isCall2Dict =
                calling != null && literal != null
                        ? literal.getTextOffset() < calling.getTextOffset()
                        : calling != null;

        // 将 dict() 调用转换为 dict 字面值
        if (isCall2Dict) {
            var snippet = new StringBuilder("{\n");
            for (var argument : calling.getArguments()) {
                // 关键字参数
                if (argument instanceof PyKeywordArgument kwarg) {
                    var key = kwarg.getKeyword();
                    var val = kwarg.getValueExpression();
                    if (key == null || val == null) {
                        editor.getCaretModel().moveToOffset(argument.getTextOffset());
                        hint.showErrorHint(editor, $message("hint.ConvertDictCallAction.syntax"));
                        return;
                    }
                    var k = generator.createStringLiteralFromString(key).getText();
                    var v = val.getText();
                    snippet.append(k).append(": ").append(v).append(",\n");
                }
                // 字典解包
                else if (argument instanceof PyStarArgument arg && arg.isKeyword()) {
                    snippet.append(arg.getText()).append(",\n");
                }
                // 位置参数
                else {
                    editor.getCaretModel().moveToOffset(argument.getTextOffset());
                    hint.showErrorHint(editor, $message("hint.ConvertDictCallAction.unpack"));
                    return;
                }
            }
            var statement = generator.createFromText(
                    PyExpressionStatementImpl.class,
                    snippet.append("}").toString()
            );
            WriteCommandAction.runWriteCommandAction(
                    file.getProject(),
                    $message("command.ConvertDictCallToData"),
                    null,
                    () -> calling.replace(statement)
            );
        }
        // 将 dict 字面值转为 dict() 调用
        else {
            var snippet = new StringBuilder("dict(\n");
            var kwargs = new StringBuilder("**{\n");
            for (PyKeyValueExpression e : literal.getElements()) {
                var key = ((PyStringLiteralExpression) e.getKey()).getStringValue();
                var val = e.getValue();
                if (val == null) {
                    editor.getCaretModel().moveToOffset(e.getTextOffset());
                    hint.showErrorHint(editor, $message("hint.ConvertDictCallAction.syntax"));
                    return;
                }
                if (PyNames.isIdentifier(key))
                    snippet.append(key).append('=').append(val.getText()).append(",\n");
                else
                    kwargs
                            .append(generator.createStringLiteralFromString(key).getText())
                            .append(": ")
                            .append(val.getText())
                            .append(",\n");
            }
            if ("**{\n".contentEquals(kwargs)) {
                snippet.append(")");
            } else {
                kwargs.append("}\n");
                snippet.append(kwargs).append(")");
            }
            var statement = generator.createFromText(
                    PyExpressionStatementImpl.class,
                    snippet.toString()
            );
            WriteCommandAction.runWriteCommandAction(
                    file.getProject(),
                    $message("command.ConvertDictDataToCall"),
                    null,
                    () -> literal.replace(statement)
            );
        }
    }
}
