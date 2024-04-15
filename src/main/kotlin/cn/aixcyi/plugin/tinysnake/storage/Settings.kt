package cn.aixcyi.plugin.tinysnake.storage

import cn.aixcyi.plugin.tinysnake.Zoo
import com.intellij.openapi.components.*

@Service(Service.Level.APP)
@State(name = Zoo.PLUGIN_SETTINGS_NAME, storages = [Storage(Zoo.PLUGIN_LEVEL_STORAGE)])
class Settings : SimplePersistentStateComponent<Settings.State>(State()) {

    class State : BaseState() {
        var myShebangs by property(PRESET_SHEBANGS) { it == PRESET_SHEBANGS }
    }

    companion object {
        val PRESET_SHEBANGS = listOf(
            "/usr/bin/python3",
            "/usr/bin/env python3",
            "/usr/local/bin/python",
            "./venv/Scripts/python.exe",
        )

        fun getInstance() = service<Settings>()
    }
}