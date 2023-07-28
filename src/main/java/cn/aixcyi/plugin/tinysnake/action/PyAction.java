package cn.aixcyi.plugin.tinysnake.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.jetbrains.python.psi.PyFile;
import org.jetbrains.annotations.NotNull;

/**
 * 基于 Python 源码而定制的、后台更新的 AnAction。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
public abstract class PyAction extends AnAction {

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

    // update() 不一定会被用到，所以设置为普通方法，不强制要求重写。
    public void update(@NotNull AnActionEvent event, @NotNull PyFile file) {
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
}
