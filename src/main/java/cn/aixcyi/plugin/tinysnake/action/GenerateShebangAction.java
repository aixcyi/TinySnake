package cn.aixcyi.plugin.tinysnake.action;

import cn.aixcyi.plugin.tinysnake.SnippetGenerator;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.psi.impl.source.tree.PsiCommentImpl;
import com.jetbrains.python.psi.PyFile;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static cn.aixcyi.plugin.tinysnake.Translation.$message;

/**
 * 为 Python 文件添加 Shebang 。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 * @see <a href="https://github.com/JetBrains/intellij-community/blob/232/platform/platform-api/src/com/intellij/ide/actions/QuickSwitchSchemeAction.java#L59">QuickSwitchSchemeAction#showPopup</a>
 * @see <a href="https://github.com/JetBrains/intellij-community/blob/232/platform/platform-impl/src/com/intellij/ide/actions/QuickChangeKeymapAction.java#L35">QuickChangeKeymapAction#fillActions</a>
 */
public class GenerateShebangAction extends PyAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event, @NotNull PyFile file, @NotNull Editor editor) {
        var project = file.getProject();
        var shebangs = List.of(
                "/usr/bin/python3",
                "/usr/bin/env python3",
                "/usr/local/bin/python",
                "./venv/Scripts/python.exe"
        );
        var group = new DefaultActionGroup((String) null, true);
        for (String shebang : shebangs) {
            group.add(new AnAction(shebang) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                    writeShebang(file, editor, "#!" + shebang);
                }
            });
        }
        group.addSeparator();
        group.add(new AnAction($message("action.GenerateShebangFromRelativePath.text")) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                var profile = project.getProjectFile();
                if (profile == null) return;
                var root = ProjectFileIndex.getInstance(project).getContentRootForFile(profile);
                if (root == null) return;

                // 创建一个起点为当前项目根目录的文件选择器
                var descriptor = FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor();
                descriptor.setTitle(e.getPresentation().getText());
                descriptor.setRoots(root);
                var chosen = FileChooser.chooseFile(descriptor, project, null);
                if (chosen == null) return;

                String newShebang;
                try {
                    newShebang = root.toNioPath().relativize(chosen.toNioPath()).toString();
                } catch (Exception ex) {
                    newShebang = chosen.getPath();
                }
                writeShebang(file, editor, "#!" + newShebang);
            }
        });
        group.add(new AnAction($message("action.GenerateShebangFromAbsolutePath.text")) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                var descriptor = FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor();
                descriptor.setTitle(e.getPresentation().getText());
                descriptor.setRoots();
                var chosen = FileChooser.chooseFile(descriptor, project, null);
                if (chosen == null) return;
                var newShebang = chosen.getPath();
                writeShebang(file, editor, "#!" + newShebang);
            }
        });
        group.add(new AnAction($message("action.GenerateShebangFromAnyPath.text")) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                var string = Messages.showInputDialog(
                        $message("GenerateShebang.input.message"),
                        $message("GenerateShebang.input.title"),
                        null
                );
                if (string == null || string.isEmpty()) return;
                var newShebang = StringUtils.stripStart(string, "#!");  // 避免 #!#、#!!、#!#!
                writeShebang(file, editor, "#!" + newShebang);
            }
        });
        ListPopup popup = JBPopupFactory.getInstance().createActionGroupPopup(
                $message("GenerateShebang.popup.title"),
                group,
                event.getDataContext(),
                JBPopupFactory.ActionSelectionAid.NUMBERING,
                true
        );
        popup.showCenteredInCurrentWindow(project);
    }

    /**
     * 将 shebang 写入到文件第一行。
     * <p>
     * 如果第一行是注释，且与传入的 {@code newShebang} 完全一致，则只弹出泡泡提示，不进行改动，否则直接替换；
     * 如果第一行不是注释，则将 {@code newShebang} 插入到第一行。
     *
     * @param file       文件
     * @param editor     编辑器
     * @param newShebang 新的 shebang。必须包含开头的 "#!" 。
     */
    private void writeShebang(@NotNull PyFile file,
                              @NotNull Editor editor,
                              @NotNull String newShebang) {
        var hint = HintManager.getInstance();
        var newComment = new SnippetGenerator(file).createSingleLineComment(newShebang);
        var firstElement = file.getFirstChild();
        var firstComment = firstElement instanceof PsiCommentImpl comment ? comment.getText() : null;
        Runnable runnable;

        // 如果第一行不是注释，那么直接在第一行插入新的shebang。
        if (firstComment == null) {
            runnable = () -> file.addBefore(newComment, firstElement);
        }
        // 如果第一行与新的shebang一致，则弹出泡泡提示，但不作改动。
        else if (firstComment.equals(newShebang)) {
            hint.showInformationHint(editor, $message("hint.GenerateShebang.same"));
            return;
        }
        // 如果第一行也是shebang注释，那么替换之。
        else if (firstComment.startsWith("#!")) {
            runnable = () -> firstElement.replace(newComment);
        }
        // 其余情况也是直接在第一行插入新的shebang。
        else {
            runnable = () -> file.addBefore(newComment, firstElement);
        }

        WriteCommandAction.runWriteCommandAction(
                file.getProject(),
                $message("command.GenerateShebang"),
                null,
                runnable
        );
    }
}
