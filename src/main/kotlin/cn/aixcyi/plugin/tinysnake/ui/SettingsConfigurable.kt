package cn.aixcyi.plugin.tinysnake.ui

import cn.aixcyi.plugin.tinysnake.Zoo
import cn.aixcyi.plugin.tinysnake.storage.Settings
import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

/**
 * 插件设置的控制器。
 *
 * 对应的页面组件是 [SettingsComponent] 。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
class SettingsConfigurable : Configurable {
    private var component: SettingsComponent? = null

    override fun getDisplayName() = Zoo.PLUGIN_DISPLAY_NAME

    override fun createComponent(): JComponent {
        component = SettingsComponent(Settings.getInstance().state)
        return component!!.mainPanel
    }

    override fun isModified() = component?.isModified() ?: false

    override fun apply() {
        component?.apply()
    }

    override fun reset() {
        component?.reset()
    }

    override fun disposeUIResources() {
        component?.dispose()
        component = null
    }
}