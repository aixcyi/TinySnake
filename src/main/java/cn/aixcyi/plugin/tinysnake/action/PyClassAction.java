package cn.aixcyi.plugin.tinysnake.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 围绕 Python 的类展开操作的 {@link PyAction}。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
public abstract class PyClassAction extends PyAction {

    @Override
    public void update(@NotNull AnActionEvent event, @NotNull PyFile file) {
        event.getPresentation().setVisible(getClassWhereCaretIn(event, file) != null);
    }

    /**
     * 获取编辑器中光标所处的类。
     *
     * @param event 消息事件。
     * @return PyClass 对象。若光标不在类内，则返回 null 。
     */
    protected @Nullable PyClass getClassWhereCaretIn(@NotNull AnActionEvent event, @NotNull PyFile file) {
        var editor = event.getData(CommonDataKeys.EDITOR);
        if (editor == null) return null;
        var offset = editor.getCaretModel().getOffset();
        var element = file.findElementAt(offset);  // 光标所在的符号
        return PsiTreeUtil.getParentOfType(element, PyClass.class);  // 自下而上查找 光标所在符号 所在的类。
    }
}
