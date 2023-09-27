package cn.aixcyi.plugin.tinysnake.action;

import cn.aixcyi.plugin.tinysnake.dialog.DocstringLinkCreator;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.PyTokenTypes;
import com.jetbrains.python.psi.PyFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static cn.aixcyi.plugin.tinysnake.Translation.$message;

/**
 * 在文档字符串（docstring）中插入超链接。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 * @see com.jetbrains.python.psi.impl.PyPlainStringElementImpl
 */
public class GenerateDocstringLinkAction extends PyAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event, @NotNull PyFile file, @NotNull Editor editor) {
        var hint = HintManager.getInstance();
        var offset = editor.getCaretModel().getOffset();
        var project = file.getProject();
        var document = editor.getDocument();
        if (getCaretDocstring(event, file) == null) {
            hint.showInformationHint(editor, $message("hint.DocstringLinkCreator.notfound"));
            return;
        }
        var dialog = new DocstringLinkCreator($message("DocstringLinkCreator.dialog.title"));
        if (!dialog.showAndGet()) return;
        var snippet = "`%s <%s>`_".formatted(dialog.getText(), dialog.getLink());
        WriteCommandAction.runWriteCommandAction(
                project,
                $message("command.GenerateDocstringLink"),
                null,
                () -> document.insertString(offset, snippet)
        );
    }

    @Nullable
    private PsiElement getCaretDocstring(@NotNull AnActionEvent event, @NotNull PyFile file) {
        var element = getCaretElement(event, file);
        if (element == null) return null;
        return PyTokenTypes.DOCSTRING.equals(element.getNode().getElementType()) ? element : null;
    }
}
