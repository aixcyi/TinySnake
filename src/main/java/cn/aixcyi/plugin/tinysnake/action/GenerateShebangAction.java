package cn.aixcyi.plugin.tinysnake.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.impl.source.tree.PsiCommentImpl;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.impl.PyElementGeneratorImpl;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static javax.swing.ListSelectionModel.SINGLE_SELECTION;

/**
 * 为 Python 源码添加 shebang 行。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
public class GenerateShebangAction extends PyAction {
    private static final String TIP_ABS_PATH = "<选择基于项目的相对路径>";
    private static final String TIP_ANY_PATH = "<选择绝对路径>";

    @Override
    public void actionPerformed(@NotNull AnActionEvent event, @NotNull PyFile file) {
        var frame = WindowManager.getInstance().getFrame(event.getProject());

        var shebangs = List.of(
                "#!/usr/bin/python3",
                "#!/usr/bin/env python3",
                "#!/usr/local/bin/python",
                "#!./venv/Scripts/python.exe",
                TIP_ABS_PATH,
                TIP_ANY_PATH
        );
        var popup = JBPopupFactory.getInstance().createPopupChooserBuilder(shebangs)
                .setSelectionMode(SINGLE_SELECTION)
                .setItemChosenCallback(s -> invoke(s, file))
                .setAdText("原有的 Shebang 将被覆盖")
                .setTitle("选择一个 shebang")
                .setMovable(true)
                .createPopup();

        if (frame == null)
            popup.showInBestPositionFor(event.getDataContext());
        else
            popup.showInCenterOf(frame);
    }

    private void invoke(@NotNull String item, @NotNull PyFile file) {
        var project = file.getProject();
        var profile = project.getProjectFile();
        if (profile == null) return;
        var root = ProjectFileIndex.getInstance(project).getContentRootForFile(profile);
        if (root == null) return;
        var descriptor = FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor();

        switch (item) {
            case TIP_ANY_PATH -> {
                descriptor.setTitle(StringUtils.strip(TIP_ANY_PATH, "<>"));
                descriptor.setRoots();
                var chosen = FileChooser.chooseFile(descriptor, project, null);
                if (chosen == null) return;
                item = "#!" + chosen.getPath();
            }
            case TIP_ABS_PATH -> {
                descriptor.setTitle(StringUtils.strip(TIP_ABS_PATH, "<>"));
                descriptor.setRoots(root);
                var chosen = FileChooser.chooseFile(descriptor, project, null);
                if (chosen == null) return;
                try {
                    item = "#!" + root.toNioPath().relativize(chosen.toNioPath());
                } catch (Exception e) {
                    item = "#!" + chosen.getPath();
                }
            }
        }
        insertShebang(item, file, project);
    }

    private void insertShebang(@NotNull String item, @NotNull PyFile file, Project project) {
        var firstChild = file.getFirstChild();
        Runnable runnable;

        if (firstChild instanceof PsiCommentImpl comment) {
            runnable = () -> comment.updateText(item);
        } else {
            var comment = new PyElementGeneratorImpl(project).createFromText(
                    file.getLanguageLevel(), PsiCommentImpl.class, item
            );
            runnable = () -> file.addBefore(comment, firstChild);
        }
        WriteCommandAction.runWriteCommandAction(
                project, "生成 shebang", "GenerateShebangAction", runnable
        );
    }
}
