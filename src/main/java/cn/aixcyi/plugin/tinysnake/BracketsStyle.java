package cn.aixcyi.plugin.tinysnake;

import org.jetbrains.annotations.NotNull;

/**
 * 括号的风格。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
public enum BracketsStyle {
    /**
     * <pre>
     *     animals = ("Cat", "Dog", "Rat")
     * </pre>
     */
    TUPLE("(", ")"),

    /**
     * <pre>
     *     animals = ("Cat", "Dog",
     *                "Rat")
     * </pre>
     */
    KNIFE_TUPLE("(", "\n)"),

    /**
     * <pre>
     *     animals = (
     *         "Cat", "Dog", "Rat"
     *     )
     * </pre>
     */
    BRANCH_TUPLE("(\n", "\n)"),

    /**
     * <pre>
     *     animals = ["Cat", "Dog", "Rat"]
     * </pre>
     */
    LIST("[", "]"),

    /**
     * <pre>
     *     animals = ["Cat", "Dog",
     *                "Rat"]
     * </pre>
     */
    KNIFE_LIST("[", "\n]"),

    /**
     * <pre>
     *     animals = [
     *         "Cat", "Dog", "Rat"
     *     ]
     * </pre>
     */
    BRANCH_LIST("[\n", "\n]"),

    /**
     * <pre>
     *     animals = {"Cat", "Dog", "Rat"}
     * </pre>
     */
    SET("{", "}"),

    /**
     * <pre>
     *     animals = {"Cat", "Dog",
     *                "Rat"}
     * </pre>
     */
    KNIFE_SET("{", "\n}"),

    /**
     * <pre>
     *     animals = {
     *         "Cat", "Dog", "Rat"
     *     }
     * </pre>
     */
    BRANCH_SET("{\n", "\n}"),

    /**
     * <pre>
     *     animals = "Cat", "Dog", "Rat"
     * </pre>
     */
    BARE("", "");

    private final String head;
    private final String tail;

    BracketsStyle(String head, String tail) {
        this.head = head;
        this.tail = tail;
    }

    public @NotNull String wrap(@NotNull String string) {
        return this.head + string + this.tail;
    }
}
