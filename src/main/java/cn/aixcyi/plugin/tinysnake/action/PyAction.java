package cn.aixcyi.plugin.tinysnake.action;

import com.intellij.openapi.actionSystem.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static cn.aixcyi.plugin.tinysnake.Translation.$message;

/**
 * 面向 Python 文件的 {@link AnAction}。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
public abstract class PyAction extends AnAction {

    public PyAction() {
        // 自动翻译所需要的 bundle-key 应该是 "action.<ActionID>.text"
        // 这里作出区别一是顺应整体格式，二是表明它不是自动翻译的。
        // https://plugins.jetbrains.com/docs/intellij/basic-action-system.html#localizing-actions-and-groups
        Presentation presentation = getTemplatePresentation();
        presentation.setText($message(getClass().getSimpleName() + ".action.text"));
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        var psi = event.getData(CommonDataKeys.PSI_FILE);
        if (psi instanceof PyFile file) {
            this.update(event, file);
        } else {
            event.getPresentation().setVisible(false);
        }
    }

    public void update(@NotNull AnActionEvent event, @NotNull PyFile file) {
        // update() 不一定会被用到，所以设置为普通方法，不强制要求重写。
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        var psi = event.getData(CommonDataKeys.PSI_FILE);
        if (psi instanceof PyFile file) {
            this.actionPerformed(event, file);
        }
    }

    // actionPerformed() 是主业务逻辑所在，是必定要重写的，故设置为抽象。
    public abstract void actionPerformed(@NotNull AnActionEvent event, @NotNull PyFile file);

    /**
     * 获取编辑器中的光标落在哪一个元素上。
     *
     * @param event 消息事件。
     * @param file  表示 Python 文件的 PSI 元素。
     * @return PSI元素。若光标不在类内，则返回 null 。
     */
    public @Nullable PsiElement getCaretElement(@NotNull AnActionEvent event, @NotNull PyFile file) {
        var editor = event.getData(CommonDataKeys.EDITOR);
        if (editor == null) return null;
        var offset = editor.getCaretModel().getOffset();
        return file.findElementAt(offset);
    }

    /**
     * 获取编辑器中的光标落在哪一个类上。
     *
     * @param event 消息事件。
     * @param file  表示 Python 文件的 PSI 元素。
     * @return PyClass 对象。若光标不在类内，则返回 null 。
     */
    public @Nullable PyClass getCaretClass(@NotNull AnActionEvent event, @NotNull PyFile file) {
        var element = getCaretElement(event, file);
        if (element == null) return null;
        return PsiTreeUtil.getParentOfType(element, PyClass.class);  // 自下而上查找 光标所在符号 所在的类。
    }
}
