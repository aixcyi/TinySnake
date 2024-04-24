package cn.aixcyi.plugin.tinysnake.action

import cn.aixcyi.plugin.tinysnake.StringUtil
import cn.aixcyi.plugin.tinysnake.Zoo.message
import cn.aixcyi.plugin.tinysnake.entity.PyPackageProxy
import cn.aixcyi.plugin.tinysnake.storage.DjangoAppGeneration.Template
import cn.aixcyi.plugin.tinysnake.tailless
import cn.aixcyi.plugin.tinysnake.ui.DjangoAppGenerator
import com.intellij.ide.projectView.ProjectView
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode
import com.intellij.ide.projectView.impl.nodes.PsiFileNode
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.util.QualifiedName
import com.intellij.util.ui.tree.TreeUtil
import com.jetbrains.python.psi.PyPsiFacade
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.io.path.exists

/**
 * 使用图形界面创建 Django App。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
class DjangoAppGenerateAction : DumbAwareAction() {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        // https://github.com/JetBrains/intellij-community/blob/241.8102/python/src/com/jetbrains/python/actions/CreatePackageAction.java#L205
        event.presentation.isVisible = run {
            event.project ?: return@run false
            val ideView = event.getData(LangDataKeys.IDE_VIEW) ?: return@run false
            return@run ideView.directories.isNotEmpty()
        }
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return

        // 获取目录树中当前选中的树枝，因为用户可以选择多个，所以这里取第一个
        val selection = ProjectView.getInstance(project)
            .currentProjectViewPane
            .selectionPaths
            ?.firstNotNullOfOrNull(TreeUtil::getLastUserObject)
        val baseQName = when (selection) {
            is PsiDirectoryNode -> selection.value?.getQName()
            is PsiDirectory -> selection.getQName()
            is PsiFileNode -> (selection.parent as PsiDirectoryNode).value?.getQName()
            is PsiFile -> selection.parent?.getQName()
            else -> null
        } ?: QualifiedName.fromComponents()

        // 编辑 Django App 初始设置
        val dialog = DjangoAppGenerator(project).setName(baseQName)
        if (!dialog.showAndGet()) return

        // 创建代理模型，准备模板
        val proxy = PyPackageProxy(project)
        val folder = proxy.root / dialog.name  // 实际上就是 proxy.create() 之后的 proxy.folder，只不过需要先确保文件夹不存在
        val appsTemplate = Template.renderApps(
            StringUtil.snakeToUpperCamel(dialog.label).tailless("App").tailless("AppConfig"),
            dialog.name.toString(),
            dialog.label,
            dialog.verboseName,
            dialog.creation.defaultAutoField,
        )
        if (folder.exists()) {
            Messages.showWarningDialog(
                project,
                "$folder",
                message("dialog.PackageExistedWarning.title").format(dialog.name),
            )
            return
        }

        // 创建 Python 包，并导航到这个包
        val runnable = {
            // 创建 App 包
            proxy.create(dialog.name, Template.DUNDER_INIT)
            proxy.add("admin", Template.ADMIN, dialog.creation.admin)
            proxy.add("apps", appsTemplate, dialog.creation.apps)
            proxy.add("models", Template.MODELS, dialog.creation.models)
            proxy.add("serializers", Template.SERIALIZERS, dialog.creation.serializers)
            proxy.add("tests", Template.TESTS, dialog.creation.tests)
            proxy.add("views", Template.VIEWS, dialog.creation.views)
            proxy.add("urls", Template.URLS, dialog.creation.urls)
            val packageVF = proxy.folder.virtualFile
            // 创建 App 包内的 migrations 子包
            proxy.create(dialog.name.append("migrations"), Template.MIGRATIONS)
            // 导航到 App 包
            ProjectView.getInstance(project).select(null, packageVF, true)
        }
        WriteCommandAction.runWriteCommandAction(
            project,
            message("command.GenerateDjangoApp"),
            null,
            runnable,
        )
    }
}

private operator fun Path.div(module: QualifiedName): Path {
    var path = this
    for (component in module.components)
        path /= component
    return path
}

// 搬过来是为了避免兼容性警告
/**
 * @author Ilya.Kazakevich
 */
private fun PsiFileSystemItem.getQName(): QualifiedName? {
    val name = PyPsiFacade.getInstance(this.project).findShortestImportableName(this.virtualFile, this) ?: return null
    return QualifiedName.fromDottedString(name)
}