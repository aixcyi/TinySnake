package cn.aixcyi.plugin.tinysnake.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.jetbrains.python.psi.PyFile;
import org.jetbrains.annotations.NotNull;

/**
 * 围绕 Python 的类展开操作的 {@link PyAction}。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
public abstract class PyClassAction extends PyAction {

    @Override
    public void update(@NotNull AnActionEvent event, @NotNull PyFile file) {
        event.getPresentation().setVisible(getCaretClass(event, file) != null);
    }
}
