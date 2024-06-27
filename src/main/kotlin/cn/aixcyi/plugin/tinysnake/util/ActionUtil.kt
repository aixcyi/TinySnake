package cn.aixcyi.plugin.tinysnake.util

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.util.QualifiedName
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.PyPsiFacade

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

/** 获取 [PyFile] 。 */
fun AnActionEvent.getPyFile(): PyFile? = eval { this.getData(CommonDataKeys.PSI_FILE) as PyFile }

/**
 * 获取编辑器。
 *
 * @param evenIfInactive 见 [LangDataKeys.EDITOR_EVEN_IF_INACTIVE]
 * @return 见 [Editor]
 */
fun AnActionEvent.getEditor(evenIfInactive: Boolean = false): Editor? {
    if (!evenIfInactive)
        return this.getData(LangDataKeys.EDITOR)
    return this.getData(LangDataKeys.EDITOR_EVEN_IF_INACTIVE)
}