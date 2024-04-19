package cn.aixcyi.plugin.tinysnake.storage

import cn.aixcyi.plugin.tinysnake.Zoo
import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project

/**
 * Django App 各源文件的模板。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
@Service(Service.Level.PROJECT)
@State(name = Zoo.DJANGO_APP_TEMPLATE_NAME)
class DjangoAppTemplate : SimplePersistentStateComponent<DjangoAppTemplate.Template>(Template()) {

    class Template : BaseState() {
        var dunderInit by property(TMP_INIT) { it == TMP_INIT }
        var admin by property(TMP_ADMIN) { it == TMP_ADMIN }
        var apps by property(TMP_APPS) { it == TMP_APPS }
        var models by property(TMP_MODELS) { it == TMP_MODELS }
        var serializers by property(TMP_SERIALIZERS) { it == TMP_SERIALIZERS }
        var views by property(TMP_TESTS) { it == TMP_TESTS }
        var urls by property(TMP_VIEWS) { it == TMP_VIEWS }
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project) = project.service<DjangoAppTemplate>()

        val TMP_INIT = Zoo.resource("/templates/__init__.py").readText()
        val TMP_ADMIN = Zoo.resource("/templates/admin.py").readText()
        val TMP_APPS = Zoo.resource("/templates/apps.py").readText()
        val TMP_MODELS = Zoo.resource("/templates/models.py").readText()
        val TMP_SERIALIZERS = Zoo.resource("/templates/serializers.py").readText()
        val TMP_TESTS = Zoo.resource("/templates/tests.py").readText()
        val TMP_VIEWS = Zoo.resource("/templates/views.py").readText()
    }
}