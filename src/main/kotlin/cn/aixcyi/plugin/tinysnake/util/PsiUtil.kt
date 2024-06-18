package cn.aixcyi.plugin.tinysnake.util

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.util.QualifiedName
import com.jetbrains.python.psi.PyPsiFacade

/**
 * [PSI](https://plugins.jetbrains.com/docs/intellij/psi.html) 相关工具。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
class PsiUtil

/**
 * @see <a href="https://peps.python.org/pep-0263/#defining-the-encoding">PEP 263 - Defining the Encoding</a>
 */
private val REGEX_ENCODING_DEFINE: Regex
    get() = "^[ \\t\\f]*#.*?coding[:=][ \\t]*([-_.a-zA-Z0-9]+)".toRegex()


fun PsiComment.isShebang() = this.text.startsWith("#!")
fun PsiComment.isEncodingDefine() = REGEX_ENCODING_DEFINE.containsMatchIn(this.text)

// 搬过来是为了避免兼容性警告
/**
 * @author Ilya.Kazakevich
 */
fun PsiFileSystemItem.getQName(): QualifiedName? {
    val name = PyPsiFacade.getInstance(this.project)
        .findShortestImportableName(this.virtualFile, this)
        ?: return null
    return QualifiedName.fromDottedString(name)
}