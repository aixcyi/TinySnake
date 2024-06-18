package cn.aixcyi.plugin.tinysnake.storage

import cn.aixcyi.plugin.tinysnake.Zoo
import cn.aixcyi.plugin.tinysnake.util.IOUtil.resource
import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project

/**
 * 创建 Django 应用的初始设置。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
@Service(Service.Level.PROJECT)
@State(name = Zoo.DJANGO_APP_CREATION_NAME)
class DjangoAppGeneration : SimplePersistentStateComponent<DjangoAppGeneration.State>(State()) {

    companion object {
        @JvmStatic
        fun getInstance(project: Project) = project.service<DjangoAppGeneration>()
    }

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
        var adminCreation by property(Creation.FILE) { it == Creation.FILE }
        var appsCreation by property(Creation.FILE) { it == Creation.FILE }
        var modelsCreation by property(Creation.FILE) { it == Creation.FILE }
        var serializersCreation by property(Creation.FILE) { it == Creation.FILE }
        var viewsCreation by property(Creation.FILE) { it == Creation.FILE }
        var testsCreation by property(Creation.FILE) { it == Creation.FILE }
        var urlsCreation by property(Creation.FILE) { it == Creation.FILE }
        var adminName by property("admin") { it == "admin" }
        var appsName by property("apps") { it == "apps" }
        var modelsName by property("models") { it == "models" }
        var serializersName by property("serializers") { it == "serializers" }
        var viewsName by property("views") { it == "views" }
        var testsName by property("tests") { it == "tests" }
        var urlsName by property("urls") { it == "urls" }

        fun isNameNotExist(name: String): Boolean {
            return name !in setOf(
                adminName,
                appsName,
                modelsName,
                serializersName,
                viewsName,
                testsName,
                urlsName,
            )
        }
    }

    /**
     * Django 应用各个文件的模板。
     */
    class Template {
        companion object {
            val MIGRATIONS = resource("/templates/migrations/__init__.py").readText().replace("\r\n", "\n")
            val DUNDER_INIT = resource("/templates/__init__.py").readText().replace("\r\n", "\n")
            val ADMIN = resource("/templates/admin.py").readText().replace("\r\n", "\n")
            val APPS = resource("/templates/apps.py").readText().replace("\r\n", "\n")
            val MODELS = resource("/templates/models.py").readText().replace("\r\n", "\n")
            val SERIALIZERS = resource("/templates/serializers.py").readText().replace("\r\n", "\n")
            val TESTS = resource("/templates/tests.py").readText().replace("\r\n", "\n")
            val VIEWS = resource("/templates/views.py").readText().replace("\r\n", "\n")
            val URLS = resource("/templates/urls.py").readText().replace("\r\n", "\n")

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
}