package cn.aixcyi.plugin.tinysnake;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Python 代码片段构造工具。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
public class SnippetBuilder {

    /**
     * 构造序列字面值。
     *
     * @param items            序列元素。
     * @param head             序列开头。
     * @param tail             序列结尾。
     * @param isLineByLine     每个元素一行，而不是全部挤在同一行。
     * @param isEndsWithComma  最后一个元素之后额外附带一个逗号。
     * @param isUseSingleQuote 使用单引号而非双引号。
     * @return 拼接后的结果字符串。
     */
    @NotNull
    public static String createSequence(@NotNull List<String> items,
                                        @NotNull String head,
                                        @NotNull String tail,
                                        boolean isLineByLine,
                                        boolean isEndsWithComma,
                                        boolean isUseSingleQuote) {
        var soup = isUseSingleQuote
                ? items.stream().map((s -> "'" + s + "'")).toList()
                : items.stream().map((s -> "\"" + s + "\"")).toList();
        return head
                + String.join(isLineByLine ? ",\n" : ", ", soup)
                + (isEndsWithComma ? "," : "")
                + tail;
    }
}
