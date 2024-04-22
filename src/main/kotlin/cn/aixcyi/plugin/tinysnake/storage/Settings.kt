package cn.aixcyi.plugin.tinysnake.storage

import cn.aixcyi.plugin.tinysnake.Zoo
import com.intellij.openapi.components.*

/**
 * 插件设置。
 *
 * - 只存储应用级别的、需要在「设置」面板中编辑的设置。
 * - 在其它地方可以编辑的设置不应在「设置」面板中重复出现，也不应放在此类中。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
@Service(Service.Level.APP)
@State(name = Zoo.PLUGIN_SETTINGS_NAME, storages = [Storage(Zoo.PLUGIN_LEVEL_STORAGE)])
class Settings : SimplePersistentStateComponent<Settings.State>(State()) {

    class State : BaseState() {
        var myShebangs by property(PRESET_SHEBANGS) { it == PRESET_SHEBANGS }
    }

    companion object {
        /**
         * 所有预置的 shebang。
         */
        val PRESET_SHEBANGS = listOf(
            "/usr/bin/python3",
            "/usr/bin/env python3",
            "/usr/local/bin/python",
            "./venv/Scripts/python.exe",
        )

        fun getInstance() = service<Settings>()
    }
}