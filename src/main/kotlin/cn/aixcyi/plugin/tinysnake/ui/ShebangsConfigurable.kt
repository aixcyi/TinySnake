package cn.aixcyi.plugin.tinysnake.ui

import cn.aixcyi.plugin.tinysnake.Zoo.message
import cn.aixcyi.plugin.tinysnake.storage.Settings
import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

/**
 * [Settings.State.myShebangs] 的设置控制器，对应的页面组件是 [ShebangsComponent] 。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
class ShebangsConfigurable : Configurable {
    private var component: ShebangsComponent? = null

    override fun getDisplayName() = message("settings.shebangs.name")

    override fun createComponent(): JComponent {
        component = ShebangsComponent(Settings.getInstance().state)
        return component!!.panel
    }

    override fun isModified() = component?.isModified() ?: false

    override fun apply() {
        component?.apply()
    }

    override fun reset() {
        component?.reset()
    }

    override fun disposeUIResources() {
        component?.disposeUIResources()
        component = null
    }
}