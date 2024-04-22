package cn.aixcyi.plugin.tinysnake.action

import cn.aixcyi.plugin.tinysnake.StringUtil
import cn.aixcyi.plugin.tinysnake.Zoo.message
import cn.aixcyi.plugin.tinysnake.entity.PyPackageProxy
import cn.aixcyi.plugin.tinysnake.storage.DjangoAppGeneration.Template
import cn.aixcyi.plugin.tinysnake.tailless
import cn.aixcyi.plugin.tinysnake.ui.DjangoAppGenerator
import com.intellij.ide.projectView.ProjectView
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDirectory
import com.intellij.psi.util.QualifiedName
import com.intellij.util.io.exists
import com.jetbrains.extensions.getQName
import com.jetbrains.python.PyNames
import com.jetbrains.python.psi.PyFile
import java.nio.file.Path
import kotlin.io.path.div

/**
 * 使用图形界面创建 Django App。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
class DjangoAppGenerateAction : AnAction() {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return

        // 获取目录树中当前选中的文件
        val toolwindow = ProjectView.getInstance(project)
        val baseQName = when (val selection = toolwindow.currentProjectViewPane.selectedElement) {
            // ${PROJECT_ROOT}/django/db/models/  -->  django.db.models
            is PsiDirectory -> selection.getQName()

            // ${PROJECT_ROOT}/django/db/models/__init__.py  -->  django.db.models
            is PyFile -> if (selection.name == PyNames.INIT_DOT_PY)
                selection.getQName()

            // ${PROJECT_ROOT}/django/db/models/aggregates.py  -->  django.db.models.aggregates
            else
                selection.getQName()?.removeTail(1)

            else -> return
        } ?: QualifiedName.fromComponents()

        // 编辑 Django App 初始设置
        val dialog = DjangoAppGenerator(project)
        dialog.name = baseQName
        if (!dialog.showAndGet()) return

        // 创建代理模型，准备模板
        val proxy = PyPackageProxy(project)
        val folder = proxy.root / dialog.name  // 实际上就是 proxy.create() 之后的 proxy.folder，只不过需要先确保文件夹不存在
        val appsTemplate = Template.APPS.format(
            // AppConfig 类名前缀
            StringUtil.snakeToUpperCamel(dialog.label).tailless("App").tailless("AppConfig"),
            dialog.creation.defaultAutoField,
            dialog.name.toString(),
            dialog.label,
        )
        if (folder.exists()) {
            Messages.showWarningDialog(
                project,
                "$folder",
                message("warning.PackageExisted.title").format(dialog.name),
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