package cn.aixcyi.plugin.tinysnake.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.psi.PyFile;
import org.jetbrains.annotations.NotNull;

public abstract class PyAction extends AnAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        PsiFile psi = event.getData(CommonDataKeys.PSI_FILE);
        if (psi instanceof PyFile file) {
            this.update(event, file);
        } else {
            event.getPresentation().setVisible(false);
        }
    }

    public void update(@NotNull AnActionEvent event, @NotNull PyFile file) {
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        PsiFile psi = event.getData(CommonDataKeys.PSI_FILE);
        if (psi instanceof PyFile file) {
            this.actionPerformed(event, file);
        }
    }

    public void actionPerformed(@NotNull AnActionEvent event, @NotNull PyFile file) {
    }
}
