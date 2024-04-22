package cn.aixcyi.plugin.tinysnake.entity

import cn.aixcyi.plugin.tinysnake.storage.DjangoAppGeneration.Creation
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import com.intellij.psi.util.QualifiedName
import com.jetbrains.python.PyNames
import com.jetbrains.python.PythonFileType
import kotlin.io.path.Path

/**
 * Python 包代理模型。
 *
 * - 调用 [create] 创建一个 Python 包后，可以直接调用 [add] 往包里添加文件或子包。
 * - [create] 每次调用都会从 [root] 创建包，上一次记录的 [folder] 将会被覆盖。
 */
class PyPackageProxy(project: Project) {

    private val fileFactory = PsiFileFactory.getInstance(project)
    private val virtualRoot = ProjectFileIndex.getInstance(project).getContentRootForFile(project.projectFile!!)!!
    private val psiRoot = PsiManager.getInstance(project).findDirectory(virtualRoot)!!

    /** 项目根目录。 */
    val root = Path(virtualRoot.path)

    /** **最后一次** 创建的 Python 包对应的 PSI 文件夹。 */
    var folder = psiRoot

    /**
     * 创建 Python 包。
     *
     * @param name 相对于根目录的包路径。比如 `django.db.models` 。
     * @param code `__init__.py` 的代码。
     * @return 包的 PSI 文件夹实体。
     */
    fun create(name: QualifiedName, code: String): PsiDirectory {
        folder = psiRoot
        val namespace = fileFactory.createFileFromText(PyNames.INIT_DOT_PY, PythonFileType.INSTANCE, code)
        for (component in name.components) {
            folder = folder.findSubdirectory(component) ?: folder.createSubdirectory(component)
        }
        folder.add(namespace)
        return folder
    }

    /**
     * 在 **当前文件夹** 内添加 Python 文件或子包。
     *
     * @param name 包名或文件名。如果是添加文件，会自动添加 `".py"` 作为文件扩展名。
     * @param code 文件代码。
     * @param creation 创建方式。
     */
    fun add(name: String, code: String, creation: Creation) {
        when (creation) {
            Creation.EMPTY -> {}
            Creation.FILE -> {
                val file = fileFactory.createFileFromText("$name.py", PythonFileType.INSTANCE, code)
                folder.add(file)
            }

            Creation.PKG -> {
                val namespace = fileFactory.createFileFromText(PyNames.INIT_DOT_PY, PythonFileType.INSTANCE, code)
                val subFolder = folder.createSubdirectory(name)
                subFolder.add(namespace)
            }
        }
    }
}