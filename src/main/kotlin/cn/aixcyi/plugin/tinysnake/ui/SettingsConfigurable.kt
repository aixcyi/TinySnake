package cn.aixcyi.plugin.tinysnake.ui

import cn.aixcyi.plugin.tinysnake.storage.Settings
import cn.aixcyi.plugin.tinysnake.util.IOUtil.message
import cn.aixcyi.plugin.tinysnake.util.hFill
import cn.aixcyi.plugin.tinysnake.util.putExtraAction
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.Messages
import com.intellij.ui.AnActionButton
import com.intellij.ui.CollectionListModel
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import com.intellij.ui.dsl.builder.panel
import javax.swing.ListSelectionModel

/**
 * Tiny Snake 插件设置。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
class SettingsConfigurable : SearchableConfigurable {

    companion object {
        const val ID = "tinysnake.SettingsConfigurable"
    }

    private val state = Settings.getInstance().state
    private val model = CollectionListModel(state.myShebangs)
    private val list = JBList(model).apply { selectionMode = ListSelectionModel.SINGLE_SELECTION }

    override fun getId() = ID

    override fun getDisplayName() = message("configurable.TinySnake.display_name")

    override fun createComponent() = panel {
        group(message("panel.ShebangsPart.title")) {
            row {
                cell(createToolbarList()).hFill()
            }
        }
    }

    private fun createToolbarList() = ToolbarDecorator.createDecorator(list)
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
        .putExtraAction(object :
            AnActionButton(message("action.ResetToDefaultConfigs.text"), AllIcons.General.Reset) {
            override fun getActionUpdateThread() = ActionUpdateThread.BGT

            override fun actionPerformed(e: AnActionEvent) {
                revert()
            }

            override fun updateButton(e: AnActionEvent) {
                val cc = contextComponent
                e.presentation.isEnabled = cc != null && cc.isShowing && cc.isEnabled && !isOriginal()
            }
        })
        .createPanel()

    override fun enableSearch(option: String?): Runnable? {
        // TODO<FUTURE>: 实现搜索
        return null
    }

    /**
     * 组件的状态（对于最后一次持久化的状态来说）是否已被修改。
     */
    override fun isModified() = model.toList() != state.myShebangs

    /**
     * 组件的状态是不是（插件预设的）初始状态。
     */
    fun isOriginal() = model.toList() == Settings.PRESET_SHEBANGS

    /**
     * 应用设置。（即把组件中的状态持久化）
     */
    override fun apply() {
        with(state) {
            myShebangs = model.toList()
        }
    }

    /**
     * 将组件中的状态重置为最后一次持久化的状态。
     */
    override fun reset() {
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

    override fun disposeUIResources() {
    }
}