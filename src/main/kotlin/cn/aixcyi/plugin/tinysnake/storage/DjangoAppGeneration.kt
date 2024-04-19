package cn.aixcyi.plugin.tinysnake.storage

import cn.aixcyi.plugin.tinysnake.Zoo
import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project

/**
 * Django App 创建的初始设置。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
@Service(Service.Level.PROJECT)
@State(name = Zoo.DJANGO_APP_CREATION_NAME)
class DjangoAppGeneration : SimplePersistentStateComponent<DjangoAppGeneration.State>(State()) {

    /**
     * 创建方式。
     */
    enum class Creation {
        /**
         * 不创建。
         */
        EMPTY,

        /**
         * 创建 Python 文件。
         */
        FILE,

        /**
         * 创建 Python 包。
         */
        PKG;

        override fun toString() = this.ordinal.toString()
    }

    class State : BaseState() {
        var defaultAutoField by property("") { it == "" }
        var admin by property(Creation.FILE) { it == Creation.FILE }
        var apps by property(Creation.FILE) { it == Creation.FILE }
        var models by property(Creation.FILE) { it == Creation.FILE }
        var serializers by property(Creation.FILE) { it == Creation.FILE }
        var views by property(Creation.FILE) { it == Creation.FILE }
        var tests by property(Creation.FILE) { it == Creation.FILE }
        var urls by property(Creation.FILE) { it == Creation.FILE }
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project) = project.service<DjangoAppGeneration>()
    }
}