package cn.aixcyi.plugin.tinysnake.action;

import cn.aixcyi.plugin.tinysnake.SnippetBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.psi.impl.source.tree.PsiCommentImpl;
import com.intellij.ui.ColoredListCellRenderer;
import com.jetbrains.python.psi.PyFile;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.LinkedHashMap;

import static cn.aixcyi.plugin.tinysnake.Translation.$message;
import static javax.swing.ListSelectionModel.SINGLE_SELECTION;

/**
 * 为 Python 源码添加 Shebang 。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
public class GenerateShebangAction extends PyAction {
    private static final String TIP_ABS_PATH = "<选择基于项目的相对路径>";
    private static final String TIP_ANY_PATH = "<选择绝对路径>";
    private static final String TIP_EDIT_PATH = "<自定义文本>";

    @Override
    public void actionPerformed(@NotNull AnActionEvent event, @NotNull PyFile file) {
        var shebangs = new LinkedHashMap<String, Icon>();
        shebangs.put("/usr/bin/python3", AllIcons.Nodes.EmptyNode);
        shebangs.put("/usr/bin/env python3", AllIcons.Nodes.EmptyNode);
        shebangs.put("/usr/local/bin/python", AllIcons.Nodes.EmptyNode);
        shebangs.put("./venv/Scripts/python.exe", AllIcons.Nodes.EmptyNode);
        shebangs.put(TIP_ABS_PATH, AllIcons.Nodes.Project);
        shebangs.put(TIP_ANY_PATH, AllIcons.Nodes.Folder);
        shebangs.put(TIP_EDIT_PATH, AllIcons.Modules.EditFolder);

        var popup = JBPopupFactory.getInstance()
                .createPopupChooserBuilder(shebangs.keySet().stream().toList())
                .setMovable(true)
                .setSelectionMode(SINGLE_SELECTION)
                .setTitle($message("GenerateShebangAction.popup.title"))
                .setAdText($message("GenerateShebangAction.popup.ad_text"))
                .setItemChosenCallback(s -> invoke(s, file))
                .setRenderer(new ColoredListCellRenderer<>() {
                    @Override
                    protected void customizeCellRenderer(@NotNull JList<? extends String> list,
                                                         String value,
                                                         int index,
                                                         boolean selected,
                                                         boolean hasFocus) {
                        this.append(value);
                        this.setIcon(shebangs.get(value));
                    }
                })
                .createPopup();

        var editor = event.getData(LangDataKeys.EDITOR_EVEN_IF_INACTIVE);
        if (editor == null)
            popup.showInBestPositionFor(event.getDataContext());
        else
            popup.showInCenterOf(editor.getComponent());
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
                item = chosen.getPath();
            }
            case TIP_ABS_PATH -> {
                descriptor.setTitle(StringUtils.strip(TIP_ABS_PATH, "<>"));
                descriptor.setRoots(root);
                var chosen = FileChooser.chooseFile(descriptor, project, null);
                if (chosen == null) return;
                try {
                    item = root.toNioPath().relativize(chosen.toNioPath()).toString();
                } catch (Exception e) {
                    item = chosen.getPath();
                }
            }
            case TIP_EDIT_PATH -> {
                var string = Messages.showInputDialog(
                        $message("GenerateShebangAction.input.message"),
                        $message("GenerateShebangAction.input.title"),
                        null
                );
                if (string == null || string.isEmpty()) return;
                item = StringUtils.stripStart(string, "#!");  // 避免 #!#、#!!、#!#!
            }
        }
        insertShebang("#!" + item, file, project);
    }

    private void insertShebang(@NotNull String item, @NotNull PyFile file, Project project) {
        var firstChild = file.getFirstChild();
        Runnable runnable;

        if (firstChild instanceof PsiCommentImpl comment) {
            runnable = () -> comment.updateText(item);
        } else {
            var comment = new SnippetBuilder(file).cakeComment(item);
            runnable = () -> file.addBefore(comment, firstChild);
        }
        WriteCommandAction.runWriteCommandAction(
                project,
                $message("GenerateShebangAction.command.name"),
                "GenerateShebangAction",
                runnable
        );
    }
}
