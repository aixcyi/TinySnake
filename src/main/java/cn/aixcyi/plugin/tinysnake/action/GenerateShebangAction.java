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
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.psi.impl.source.tree.PsiCommentImpl;
import com.jetbrains.python.psi.PyFile;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

import static cn.aixcyi.plugin.tinysnake.Translation.$message;

/**
 * 为 Python 源码添加 Shebang 。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
public class GenerateShebangAction extends PyAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event, @NotNull PyFile file) {
        var lines = List.of(
                "/usr/bin/python3",
                "/usr/bin/env python3",
                "/usr/local/bin/python",
                "./venv/Scripts/python.exe",
                $message("GenerateShebangAction.popup.path_relative"),  // reversedIndex: -3
                $message("GenerateShebangAction.popup.path_absolute"),  // reversedIndex: -2
                $message("GenerateShebangAction.popup.path_any")  // reversedIndex: -1
        );
        var icons = List.of(
                AllIcons.Nodes.EmptyNode,
                AllIcons.Nodes.EmptyNode,
                AllIcons.Nodes.EmptyNode,
                AllIcons.Nodes.EmptyNode,
                AllIcons.Nodes.Project,
                AllIcons.Nodes.Folder,
                AllIcons.Modules.EditFolder
        );
        var title = $message("GenerateShebangAction.popup.title");
        var step = new BaseListPopupStep<>(title, lines, icons) {
            @Override
            public PopupStep<?> onChosen(String selectedValue, boolean finalChoice) {
                var items = getValues();
                int index = items.indexOf(selectedValue);
                int reversedIndex = index == -1 ? 0 : index - items.size();
                doFinalStep(() -> invoke(reversedIndex, selectedValue, file));
                return super.onChosen(selectedValue, finalChoice);
            }
        };

        var popup = JBPopupFactory.getInstance().createListPopup(step);
        popup.setAdText($message("GenerateShebangAction.popup.ad_text"), SwingConstants.LEFT);

        var editor = event.getData(LangDataKeys.EDITOR_EVEN_IF_INACTIVE);
        if (editor == null)
            popup.showInBestPositionFor(event.getDataContext());
        else
            popup.showInCenterOf(editor.getComponent());
    }

    // 在备注中搜索 reversedIndex
    private void invoke(int reversedIndex, String item, @NotNull PyFile file) {
        if (reversedIndex >= 0) return;
        var project = file.getProject();
        var profile = project.getProjectFile();
        if (profile == null) return;
        var root = ProjectFileIndex.getInstance(project).getContentRootForFile(profile);
        if (root == null) return;
        var descriptor = FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor();

        // Ctrl + F -> reversedIndex
        switch (reversedIndex) {
            // 添加绝对路径
            case -2 -> {
                descriptor.setTitle(StringUtils.strip(item, "<>"));
                descriptor.setRoots();
                var chosen = FileChooser.chooseFile(descriptor, project, null);
                if (chosen == null) return;
                item = chosen.getPath();
            }
            // 添加相对路径
            case -3 -> {
                descriptor.setTitle(StringUtils.strip(item, "<>"));
                descriptor.setRoots(root);
                var chosen = FileChooser.chooseFile(descriptor, project, null);
                if (chosen == null) return;
                try {
                    item = root.toNioPath().relativize(chosen.toNioPath()).toString();
                } catch (Exception e) {
                    item = chosen.getPath();
                }
            }
            // 添加自定义文本
            case -1 -> {
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
