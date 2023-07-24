package cn.aixcyi.plugin.tinysnake;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public enum SymbolsOrder {
    /**
     * 按符号出现顺序排序。
     */
    APPEARANCE("声明顺序"),

    /**
     * 按字母先后顺序排序（不区分大小写）。
     */
    ALPHABET("字母顺序"),

    /**
     * 按字母先后顺序排序（区分大小写）。
     */
    CHARSET("字符编码顺序");

    public final @NotNull String label;

    SymbolsOrder(@NotNull String label) {
        this.label = label;
    }

    public static @NotNull List<String> getLabels() {
        return Arrays.stream(SymbolsOrder.values())
                .map((enumeration) -> enumeration.label).toList();
    }

    public static @Nullable SymbolsOrder fromLabel(@NotNull String label) {
        for (SymbolsOrder enumeration : SymbolsOrder.values())
            if (enumeration.label.equals(label))
                return enumeration;
        return null;
    }
}
