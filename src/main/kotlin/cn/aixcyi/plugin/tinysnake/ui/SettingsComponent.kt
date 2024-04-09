package cn.aixcyi.plugin.tinysnake.ui

import cn.aixcyi.plugin.tinysnake.Zoo.message
import cn.aixcyi.plugin.tinysnake.storage.Settings
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
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
 * 插件设置的页面组件。
 *
 * 对应的控制器是 [SettingsConfigurable] 。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
class SettingsComponent(private val state: Settings.State) {

    private val model: CollectionListModel<String> = CollectionListModel<String>(ArrayList(state.myShebangs))
    val mainPanel: JPanel = FormBuilder.createFormBuilder()
        .addComponent(createShebangsPart(), 1)
        .addComponentFillVertically(JPanel(), 0)
        .panel

    private fun createShebangsPart(): JPanel {
        val list = JBList(model)
        list.selectionMode = ListSelectionModel.SINGLE_SELECTION
        val innerPanel = MeowUiUtil.createTitledPanel(message("settings.ShebangsPart.title"))
        innerPanel.add(
            ToolbarDecorator.createDecorator(list)
                .setAddAction {
                    val string = Messages.showInputDialog(
                        message("GenerateShebang.input.message"),
                        message("GenerateShebang.input.title"),
                        null
                    )
                    if (string.isNullOrEmpty() || string.isBlank()) return@setAddAction
                    model.add(string)
                    list.selectionModel.leadSelectionIndex = model.size - 1
                }
                .setEditAction {
                    val string = Messages.showInputDialog(
                        message("GenerateShebang.input.message"),
                        message("GenerateShebang.input.title"),
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
                .addExtraAction(object : AnActionButton(message("settings.normal.reset"), AllIcons.Actions.GC) {
                    override fun getActionUpdateThread() = ActionUpdateThread.EDT

                    override fun actionPerformed(e: AnActionEvent) {
                        revert()
                    }
                })
                .createPanel()
        )
        return innerPanel
    }

    fun isModified() = model.toList() != state.myShebangs

    fun apply() {
        with(state) {
            myShebangs = model.toList()
        }
    }

    fun reset() {
        model.removeAll()
        model.addAll(0, state.myShebangs)
    }

    fun revert() {
        model.removeAll()
        model.addAll(0, Settings.getInstance().state.myShebangs)
    }

    fun dispose() {
    }
}