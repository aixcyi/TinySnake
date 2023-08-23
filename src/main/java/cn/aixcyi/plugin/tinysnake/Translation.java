package cn.aixcyi.plugin.tinysnake;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.NotNull;

import java.util.ResourceBundle;

/**
 * 提供本地化翻译。
 * <br>
 * A utility class that provides translated message/text.
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
public class Translation {

    /**
     * 菜单部分的翻译文本。
     */
    public final static Section Menu = new Section("messages.Menu");
    /**
     * 设置部分的翻译文本。
     */
    public final static Section Setting = new Section("messages.Setting");

    /**
     * 重新载入所有板块的翻译资源。
     */
    public static void reload() {
        Menu.reload();
    }

    public static class Section {
        private final String name;
        private ResourceBundle bundle;

        Section(String baseName) {
            name = baseName;
            reload();
        }

        void reload() {
            bundle = ResourceBundle.getBundle(name, DynamicBundle.getLocale());
        }

        public @NotNull String get(@NotNull String key) {
            return bundle.getString(key);
        }
    }
}
