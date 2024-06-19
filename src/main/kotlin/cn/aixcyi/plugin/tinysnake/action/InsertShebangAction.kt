package cn.aixcyi.plugin.tinysnake.action

import cn.aixcyi.plugin.tinysnake.entity.SnippetGenerator
import cn.aixcyi.plugin.tinysnake.storage.Settings
import cn.aixcyi.plugin.tinysnake.ui.SettingsConfigurable
import cn.aixcyi.plugin.tinysnake.util.IOUtil.message
import com.intellij.codeInsight.hint.HintManager
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.psi.impl.source.tree.PsiCommentImpl
import com.jetbrains.python.psi.PyFile
import org.apache.commons.lang3.StringUtils

/**
 * 为 Python 文件添加 Shebang 。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 * @see <a href="https://github.com/JetBrains/intellij-community/blob/232/platform/platform-api/src/com/intellij/ide/actions/QuickSwitchSchemeAction.java#L59">QuickSwitchSchemeAction#showPopup</a>
 * @see <a href="https://github.com/JetBrains/intellij-community/blob/232/platform/platform-impl/src/com/intellij/ide/actions/QuickChangeKeymapAction.java#L35">QuickChangeKeymapAction#fillActions</a>
 */
class InsertShebangAction : DumbAwareAction() {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        // 如果不在编辑器中则隐藏菜单
        // 如果不在 Python 文件中则禁用菜单
        event.presentation.isVisible = event.getData(LangDataKeys.EDITOR_EVEN_IF_INACTIVE) != null
        event.presentation.isEnabled = event.getData(CommonDataKeys.PSI_FILE) is PyFile
    }

    override fun actionPerformed(event: AnActionEvent) {
        val editor = event.getData(LangDataKeys.EDITOR_EVEN_IF_INACTIVE) ?: return
        val file = (event.getData(CommonDataKeys.PSI_FILE).takeIf { it is PyFile } ?: return) as PyFile
        val project = file.project
        val state = Settings.getInstance().state
        val group = DefaultActionGroup(null as String?, true)
        for (shebang in state.myShebangs) {
            group.add(object : AnAction(shebang) {
                override fun actionPerformed(e: AnActionEvent) {
                    writeShebang(file, editor, "#!$shebang")
                }
            })
        }
        group.addSeparator()
        group.add(object : AnAction(message("action.GenerateShebangFromRelativePath.text")) {
            override fun actionPerformed(e: AnActionEvent) {
                val profile = project.projectFile ?: return
                val root = ProjectFileIndex.getInstance(project).getContentRootForFile(profile) ?: return

                // 创建一个起点为当前项目根目录的文件选择器
                val descriptor = FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor()
                descriptor.title = e.presentation.text
                descriptor.setRoots(root)
                val chosen = FileChooser.chooseFile(descriptor, project, null) ?: return
                val newShebang = try {
                    root.toNioPath().relativize(chosen.toNioPath()).toString()
                } catch (ex: Exception) {
                    chosen.path
                }
                writeShebang(file, editor, "#!$newShebang")
            }
        })
        group.add(object : AnAction(message("action.GenerateShebangFromAbsolutePath.text")) {
            override fun actionPerformed(e: AnActionEvent) {
                val descriptor = FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor()
                descriptor.title = e.presentation.text
                descriptor.setRoots()
                val chosen = FileChooser.chooseFile(descriptor, project, null) ?: return
                val newShebang = chosen.path
                writeShebang(file, editor, "#!$newShebang")
            }
        })
        group.add(object : AnAction(message("action.GenerateShebangFromAnyPath.text")) {
            override fun actionPerformed(e: AnActionEvent) {
                val string = Messages.showInputDialog(
                    message("dialog.InputGenerateShebang.message"),
                    message("dialog.InputGenerateShebang.title"),
                    null
                )
                if (string.isNullOrEmpty()) return
                val newShebang = StringUtils.stripStart(string, "#!") // 避免 #!#、#!!、#!#!
                writeShebang(file, editor, "#!$newShebang")
            }
        })
        group.addSeparator()
        group.add(object : AnAction(message("action.GotoConfiguration.text")) {
            override fun actionPerformed(e: AnActionEvent) {
                ShowSettingsUtil.getInstance().showSettingsDialog(
                    event.project,
                    SettingsConfigurable::class.java,
                )
            }
        })
        val popup = JBPopupFactory.getInstance().createActionGroupPopup(
            message("popup.SelectOneShebang.title"),
            group,
            event.dataContext,
            JBPopupFactory.ActionSelectionAid.NUMBERING,
            true
        )
        popup.showCenteredInCurrentWindow(project)
    }

    /**
     * 将 shebang 写入到文件第一行。
     *
     * 如果第一行是注释，且与传入的 [newShebang] 完全一致，则只弹出泡泡提示，不进行改动，否则直接替换；
     * 如果第一行不是注释，则将 [newShebang] 插入到第一行。
     *
     * @param file       文件
     * @param editor     编辑器
     * @param newShebang 新的 shebang。开头必须包含 `#!` 。
     */
    private fun writeShebang(file: PyFile, editor: Editor, newShebang: String) {
        val hint = HintManager.getInstance()
        val newComment = SnippetGenerator(file).createSingleLineComment(newShebang)
        val firstElement = file.firstChild
        val firstComment = if (firstElement is PsiCommentImpl) firstElement.text else null

        // 如果第一行不是注释，那么直接在第一行插入新的shebang。
        val runnable = if (firstComment == null) {
            Runnable { file.addBefore(newComment, firstElement) }
        } else if (firstComment == newShebang) {
            hint.showInformationHint(editor, message("hint.ShebangExisted.text"))
            return
        } else if (firstComment.startsWith("#!")) {
            Runnable { firstElement.replace(newComment) }
        } else {
            Runnable { file.addBefore(newComment, firstElement) }
        }

        WriteCommandAction.runWriteCommandAction(
            file.project,
            message("command.GenerateShebang"),
            null,
            runnable
        )
    }
}