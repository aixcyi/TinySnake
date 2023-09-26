package cn.aixcyi.plugin.tinysnake;

import com.jetbrains.python.psi.LanguageLevel;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.impl.PyElementGeneratorImpl;
import org.jetbrains.annotations.NotNull;

/**
 * Python 元素构造器。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
public class SnippetGenerator extends PyElementGeneratorImpl {
    final LanguageLevel version;

    public SnippetGenerator(@NotNull PyFile file) {
        super(file.getProject());
        this.version = file.getLanguageLevel();
    }

    /**
     * 使用字符串构造特定类型的代码片段。
     *
     * @param type 代码片段类型。
     * @param text 代码片段字符串。
     * @return 代码片段对象。
     */
    public <T> @NotNull T createFromText(Class<T> type, String text) {
        return createFromText(this.version, type, text);
    }
}
