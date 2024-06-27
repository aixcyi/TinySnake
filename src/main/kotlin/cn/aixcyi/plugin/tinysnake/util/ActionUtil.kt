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

/** 选中范围开始位置（未选中则使用光标位置）的左侧是否为空白（即没有字符，或为任一 [blankChar] 或空格）。 */
fun Editor.isSelectionStartLeftBlank(vararg blankChar: Char): Boolean {
    val position = this.offsetToLogicalPosition(this.selectionModel.selectionStart)
    if (position.column == 0)  // 列号为 column+1，0 表示在行首。
        return true
    // 因为列号不为 0，说明前面肯定还有字符，因此以下代码不会报错
    val leftChar = this.document.charsSequence[this.selectionModel.selectionStart - 1]
    if (blankChar.isEmpty())
        return leftChar == ' '
    return blankChar.any { it == leftChar }
}

/** 选中范围结束位置（未选中则使用光标位置）的右侧是否为空白（即没有字符，或为任一 [blankChar] 或空格）。 */
fun Editor.isSelectionEndRightBlank(vararg blankChar: Char): Boolean {
    val position = this.offsetToLogicalPosition(this.selectionModel.selectionEnd)
    val lineEnd = this.offsetToLogicalPosition(this.document.getLineEndOffset(position.line)).column
    if (position.column == lineEnd)
        return true
    // 字符数组下标为 n 的那个字符的右侧索引是 n+1，因此下标可能会超出范围，也就是到达行尾
    val rightChar = eval { this.document.charsSequence[this.selectionModel.selectionEnd] } ?: return true
    if (blankChar.isEmpty())
        return rightChar == ' '
    return blankChar.any { it == rightChar }
}