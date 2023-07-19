package cn.aixcyi.plugin.tinysnake;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.psi.PsiFile;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.components.JBList;
import com.jetbrains.python.PythonLanguage;
import com.jetbrains.python.psi.PyFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.JList;

import static javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;

/**
 * 在 "生成..." 菜单里添加的 "__all__"。
 * <p>
 * 该菜单用于生成或更新 Python 源码文件中的 __all__ 变量。
 *
 * @author aixcyi
 */
public class GenerateDunderAllAction extends AnAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        PsiFile psi = event.getData(CommonDataKeys.PSI_FILE);
        Presentation presentation = event.getPresentation();
        presentation.setVisible(psi != null && psi.getLanguage() == PythonLanguage.INSTANCE);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        PsiFile psi = event.getData(CommonDataKeys.PSI_FILE);
        if (project == null || psi == null) return;
        PyFile file = (PyFile) psi;
        DunderAllEntity all = new DunderAllEntity(file);  // 遍历所有顶层表达式获取所有符号

        // TODO:
        JBList<String> options = new JBList<>(new CollectionListModel<>(all.symbols));
        options.getEmptyText().setText("没有可公开的顶级符号");

        JBPopup popup = new PopupChooserBuilder<>(options)
                .setSelectionMode(MULTIPLE_INTERVAL_SELECTION)
                .setRenderer(new ColoredListCellRenderer<>() {
                    @Override
                    protected void customizeCellRenderer(@NotNull JList<? extends String> list,
                                                         String value,
                                                         int index,
                                                         boolean selected,
                                                         boolean hasFocus) {
                        this.append(value);
                        this.setIcon(all.icons.get(index));
                        this.setEnabled(!all.exports.contains(value));
                    }
                })
                .setItemsChosenCallback(choices -> all.adds(choices, project))
                .setAdText("Ctrl+单击 选择多个，Shift+单击 批量选择")
                .setTitle("选择导出到 __all__ 的符号")
                .createPopup();

        popup.showInBestPositionFor(event.getDataContext());
    }
}
