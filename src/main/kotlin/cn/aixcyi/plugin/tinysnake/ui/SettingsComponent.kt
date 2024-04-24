package cn.aixcyi.plugin.tinysnake.ui

import cn.aixcyi.plugin.tinysnake.Zoo.message
import cn.aixcyi.plugin.tinysnake.runTry
import cn.aixcyi.plugin.tinysnake.storage.Settings
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import com.intellij.ui.AnActionButton
import com.intellij.ui.CollectionListModel
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import com.intellij.util.ui.FormBuilder
import javax.swing.JPanel
import javax.swing.ListSelectionModel

/**
 * 插件设置的 UI 页面组件。
 *
 * - 对应的控制器是 [SettingsConfigurable] 。
 * - [SettingsConfigurable] 可能会在后台线程中被实例化，在其构造器中创建 UI 组件可能会降低 UI 响应能力，
 *   因此将代码分离到单独的类中进行。参见 [Implementations for Settings Extension Points](https://plugins.jetbrains.com/docs/intellij/settings-guide.html#constructors)。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
class SettingsComponent(private val state: Settings.State) {

    private val model: CollectionListModel<String> = CollectionListModel<String>(ArrayList(state.myShebangs))

    /** 根面板。存放此类所有组件的组件。 */
    val mainPanel: JPanel = FormBuilder.createFormBuilder()
        .addComponent(createShebangsPart(), 1)
        .addComponentFillVertically(JPanel(), 0)
        .panel

    /**
     * 创建 shebang 列表管理部分的组件。
     *
     * @return 这部分组件的父组件（也就是 **这一部分** 的根面板）。
     */
    private fun createShebangsPart(): JPanel {
        val list = JBList(model)
        list.selectionMode = ListSelectionModel.SINGLE_SELECTION
        val toolbar = ToolbarDecorator.createDecorator(list)
            .setAddAction {
                val string = Messages.showInputDialog(
                    message("dialog.InputGenerateShebang.message"),
                    message("dialog.InputGenerateShebang.title"),
                    null
                )
                if (string.isNullOrEmpty() || string.isBlank()) return@setAddAction
                model.add(string)
                list.selectionModel.leadSelectionIndex = model.size - 1
            }
            .setEditAction {
                val string = Messages.showInputDialog(
                    message("dialog.InputGenerateShebang.message"),
                    message("dialog.InputGenerateShebang.title"),
                    null,
                    model.getElementAt(list.selectedIndex),
                    null
                )
                if (string.isNullOrEmpty() || string.isBlank()) return@setEditAction
                model.setElementAt(string, list.selectedIndex)
            }
            .setRemoveAction {
                model.remove(list.selectedIndex)
                list.selectionModel.leadSelectionIndex = list.leadSelectionIndex
            }
        val revertButton = object :
            AnActionButton(message("action.ResetToDefaultConfigs.text"), AllIcons.General.Reset) {
            override fun getActionUpdateThread() = ActionUpdateThread.BGT

            override fun actionPerformed(e: AnActionEvent) {
                revert()
            }

            override fun updateButton(e: AnActionEvent) {
                val cc = contextComponent
                e.presentation.isEnabled = cc != null && cc.isShowing && cc.isEnabled && !isOriginal()
            }
        }
        toolbar.javaClass.runTry {
            getMethod("addExtraAction", AnAction::class.java).invoke(toolbar, revertButton as AnAction)
        }?.runTry {
            getMethod("addExtraAction", AnActionButton::class.java).invoke(toolbar, revertButton)
        }
        val innerPanel = MeowUiUtil.createTitledPanel(message("panel.ShebangsPart.title"))
        innerPanel.add(toolbar.createPanel())
        return innerPanel
    }

    /**
     * 组件的状态（对于最后一次持久化的状态来说）是否已被修改。
     */
    fun isModified() = model.toList() != state.myShebangs

    /**
     * 组件的状态是不是（代码预设的）初始状态。
     */
    fun isOriginal() = model.toList() == Settings.PRESET_SHEBANGS

    /**
     * 应用设置。（即把组件中的状态持久化）
     */
    fun apply() {
        with(state) {
            myShebangs = model.toList()
        }
    }

    /**
     * 将组件中的状态重置为最后一次持久化的状态。
     */
    fun reset() {
        model.removeAll()
        model.addAll(0, state.myShebangs)
    }

    /**
     * 将组件的状态还原为（代码预设的）初始状态。
     */
    fun revert() {
        model.removeAll()
        model.addAll(0, Settings.PRESET_SHEBANGS)
    }

    /**
     * 释放组件。
     */
    fun dispose() {
    }
}