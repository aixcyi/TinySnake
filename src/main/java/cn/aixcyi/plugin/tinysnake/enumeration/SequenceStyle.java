package cn.aixcyi.plugin.tinysnake.enumeration;

import org.jetbrains.annotations.NotNull;

/**
 * 字面序列的风格。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
public enum SequenceStyle {

    /**
     * <pre>animals = "Bird", "Cat", "Dog"</pre>
     */
    BARE("", ""),

    /**
     * <pre>animals = ("Bird", "Cat", "Dog")</pre>
     */
    TUPLE("(", ")"),

    /**
     * <pre>animals = ["Bird", "Cat", "Dog"]</pre>
     */
    LIST("[", "]"),

    /**
     * <pre>animals = {"Bird", "Cat", "Dog"}</pre>
     */
    SET("{", "}"),

    /**
     * <pre>animals = (<br>    "Bird", "Cat", "Dog"<br>)</pre>
     */
    WINGED_TUPLE("(\n", "\n)"),

    /**
     * <pre>animals = [<br>    "Bird", "Cat", "Dog"<br>]</pre>
     */
    WINGED_LIST("[\n", "\n]"),

    /**
     * <pre>animals = {<br>    "Bird", "Cat", "Dog"<br>}</pre>
     */
    WINGED_SET("{\n", "\n}"),

    /**
     * <pre>animals = ("Bird",<br>           "Cat",<br>           "Dog"<br>           )</pre>
     */
    AXE_TUPLE("(", "\n)"),

    /**
     * <pre>animals = ["Bird",<br>           "Cat",<br>           "Dog"<br>           ]</pre>
     */
    AXE_LIST("[", "\n]"),

    /**
     * <pre>animals = {"Bird",<br>           "Cat",<br>           "Dog"<br>           }</pre>
     */
    AXE_SET("{", "\n}"),

    // 或许以后还有更多
    ;

    private final String head;
    private final String tail;

    SequenceStyle(String head, String tail) {
        this.head = head;
        this.tail = tail;
    }

    public @NotNull String wrap(@NotNull String string) {
        return this.head + string + this.tail;
    }
}
