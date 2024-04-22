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

    /** Python 源码的创建方式。 */
    enum class Creation {

        /** 不创建。 */
        EMPTY,

        /** 创建一个 Python 文件。 */
        FILE,

        /** 创建一个 Python 包，并附带一个名为 `__init__.py` 的文件。 */
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

    /**
     * Django App 各个文件的模板。
     */
    class Template {
        companion object {
            val MIGRATIONS = Zoo.resource("/templates/migrations/__init__.py").readText().replace("\r\n", "\n")
            val DUNDER_INIT = Zoo.resource("/templates/__init__.py").readText().replace("\r\n", "\n")
            val ADMIN = Zoo.resource("/templates/admin.py").readText().replace("\r\n", "\n")
            val APPS = Zoo.resource("/templates/apps.py").readText().replace("\r\n", "\n")
            val MODELS = Zoo.resource("/templates/models.py").readText().replace("\r\n", "\n")
            val SERIALIZERS = Zoo.resource("/templates/serializers.py").readText().replace("\r\n", "\n")
            val TESTS = Zoo.resource("/templates/tests.py").readText().replace("\r\n", "\n")
            val VIEWS = Zoo.resource("/templates/views.py").readText().replace("\r\n", "\n")
            val URLS = Zoo.resource("/templates/urls.py").readText().replace("\r\n", "\n")

            fun renderApps(
                clsPostfix: String,
                appName: String,
                appLabel: String?,
                verboseName: String?,
                defaultAutoField: String?,
            ): String {
                var template = APPS.format(clsPostfix, appName)
                if (!appLabel.isNullOrEmpty())
                    template += "    label = '$appLabel'\n"
                if (!verboseName.isNullOrEmpty())
                    template += "    verbose_name = '$verboseName'\n"
                if (!defaultAutoField.isNullOrEmpty())
                    template += "    default_auto_field = '$defaultAutoField'\n"
                return template
            }
        }
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project) = project.service<DjangoAppGeneration>()
    }
}