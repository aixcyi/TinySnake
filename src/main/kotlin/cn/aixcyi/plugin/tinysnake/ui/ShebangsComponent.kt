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
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.ListSelectionModel

/**
 * [Settings.State.myShebangs] 的页面组件，对应的设置控制器是 [ShebangsConfigurable] 。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
class ShebangsComponent(private val state: Settings.State) {
    val panel: JPanel = JPanel()
    private val model: CollectionListModel<String> = CollectionListModel<String>(ArrayList(state.myShebangs))

    init {
        val list = JBList(model)
        list.selectionMode = ListSelectionModel.SINGLE_SELECTION
        panel.setLayout(BoxLayout(panel, BoxLayout.Y_AXIS))
        panel.add(
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

    fun disposeUIResources() {
    }
}