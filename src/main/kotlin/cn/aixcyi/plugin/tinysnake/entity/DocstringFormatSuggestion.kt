package cn.aixcyi.plugin.tinysnake.entity

import cn.aixcyi.plugin.tinysnake.util.IOUtil.message
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.options.ShowSettingsUtil
import com.jetbrains.python.configuration.PyIntegratedToolsModulesConfigurable
import com.jetbrains.python.documentation.PyDocumentationSettings
import com.jetbrains.python.documentation.docstrings.DocStringFormat
import com.jetbrains.python.psi.PyFile

/**
 * 文档字符串格式建议。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
class DocstringFormatSuggestion(private val file: PyFile) {

    /** 与文档字符串格式有关的设置。 */
    private val settings = PyDocumentationSettings.getInstance(
        ModuleUtilCore.findModuleForPsiElement(file.containingFile)
            ?: ModuleManager.getInstance(file.project).modules.firstOrNull()
    )

    /** 带有“切换格式”和“打开设置”两个链接的通知泡泡。 */
    val notification = NotificationGroupManager.getInstance()
        .getNotificationGroup("TinySnake.SuggestionsGroup")
        .createNotification(
            message("notification.DocstringFormatSuggestion.title"),
            message("notification.DocstringFormatSuggestion.content"),
            NotificationType.INFORMATION,
        )
        .setSuggestionType(true)
        .addAction(NotificationAction.create(message("text.SwitchDocstringFormat")) { _, notification ->
            settings.format = DocStringFormat.REST
            notification.expire()
        })
        .addAction(NotificationAction.create(message("text.GotoDocstringFormatSettings")) { event, notification ->
            ShowSettingsUtil.getInstance().showSettingsDialog(
                event.project,
                PyIntegratedToolsModulesConfigurable::class.java,
            )
            notification.hideBalloon()
        })

    /** 当前是否将文档字符串设置为了 reStructuredText 格式。 */
    val isRestFormat
        get() = settings.getFormatForFile(file.containingFile) == DocStringFormat.REST
}