package cn.aixcyi.plugin.tinysnake.ui

import cn.aixcyi.plugin.tinysnake.Zoo
import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

/**
 * 插件设置的控制器，对应的是主页面组件。
 *
 * 插件采用了一个主设置页+多个子设置页的设计，这个类就是主设置页对应的控制器，目前没有任何组件。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
class TinySnakeConfigurable : Configurable {

    override fun getDisplayName() = Zoo.PLUGIN_DISPLAY_NAME

    override fun createComponent(): JComponent? {
        return null
    }

    override fun isModified() = false

    override fun apply() {
    }
}