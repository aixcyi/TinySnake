package cn.aixcyi.plugin.tinysnake;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.psi.PyFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OptimizeDunderAllAction extends AnAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        PsiFile psi = event.getData(CommonDataKeys.PSI_FILE);
        if (!(psi instanceof PyFile file)) {
            event.getPresentation().setEnabled(false);
            return;
        }
        List<String> list = file.getDunderAll();
        event.getPresentation().setEnabled(list != null && list.size() > 1);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        PsiFile psi = event.getData(CommonDataKeys.PSI_FILE);
        if (!(psi instanceof PyFile file)) return;

        DunderAllOptimizerDialog dialog = new DunderAllOptimizerDialog();
        dialog.setLocationRelativeTo(null);
        dialog.setEntity(new DunderAllEntity(file));
        dialog.pack();
        dialog.setVisible(true);
    }
}
