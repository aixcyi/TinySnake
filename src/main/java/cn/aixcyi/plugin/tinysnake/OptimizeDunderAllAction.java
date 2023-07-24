package cn.aixcyi.plugin.tinysnake;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.PythonLanguage;
import com.jetbrains.python.psi.PyFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static javax.swing.ListSelectionModel.SINGLE_SELECTION;

public class OptimizeDunderAllAction extends AnAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        PsiFile psi = event.getData(CommonDataKeys.PSI_FILE);
        if (!(psi instanceof PyFile file)) return;
        event.getPresentation().setEnabled(
                psi.getLanguage() == PythonLanguage.INSTANCE && file.getDunderAll() != null
        );
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        PsiFile psi = event.getData(CommonDataKeys.PSI_FILE);
        if (project == null || !(psi instanceof PyFile file)) return;

        DunderAllEntity all = new DunderAllEntity(file);

        JFrame frame = WindowManager.getInstance().getFrame(project);
        if (frame == null) return;
        JBPopupFactory
                .getInstance()
                .createPopupChooserBuilder(SymbolsOrder.getLabels())
                .setItemChosenCallback(all::sort)
                .setSelectionMode(SINGLE_SELECTION)
                .setTitle("排序方式")
                .setMovable(true)
                .createPopup()
                .showInCenterOf(frame);
    }
}
