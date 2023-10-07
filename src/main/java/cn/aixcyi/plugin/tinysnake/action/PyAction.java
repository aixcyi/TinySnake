package cn.aixcyi.plugin.tinysnake.action;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.psi.PyFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 面向 Python 定制的 {@link AnAction}。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
public abstract class PyAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        var editor = event.getData(LangDataKeys.EDITOR_EVEN_IF_INACTIVE);
        if (editor == null) {
            event.getPresentation().setVisible(false);
            return;
        }
        var psi = event.getData(CommonDataKeys.PSI_FILE);
        if (psi instanceof PyFile file) {
            this.actionPerformed(event, file, editor);
        }
    }

    // actionPerformed() 是主业务逻辑所在，是必定要重写的，故设置为抽象。
    public abstract void actionPerformed(@NotNull AnActionEvent event, @NotNull PyFile file, @NotNull Editor editor);

    /**
     * 查找光标所在的元素。
     *
     * @param event 消息事件。
     * @param file  表示 Python 文件的 PSI 元素。
     * @return PSI元素。若光标不在任何元素内，则返回 null 。
     */
    public @Nullable PsiElement getCaretElement(@NotNull AnActionEvent event, @NotNull PyFile file) {
        var editor = event.getData(CommonDataKeys.EDITOR);
        if (editor == null) return null;
        var offset = editor.getCaretModel().getOffset();
        return file.findElementAt(offset);
    }

    /**
     * 查找光标所在的元素。
     *
     * @param event 消息事件。
     * @param file  表示 Python 文件的 PSI 元素。
     * @param type  元素或父元素的类型。
     * @return PSI元素。若光标不在特定类型的元素内，则返回 null 。
     */
    public <T extends com.intellij.psi.PsiElement> @Nullable T getCaretElement(
            @NotNull AnActionEvent event,
            @NotNull PyFile file,
            @NotNull Class<T> type
    ) {
        var element = getCaretElement(event, file);
        if (element == null) return null;
        return PsiTreeUtil.getParentOfType(element, type);  // 自内向外查找 光标所在处 外面的父元素。
    }
}
