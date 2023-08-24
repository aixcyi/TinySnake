package cn.aixcyi.plugin.tinysnake;

import com.intellij.lang.LangBundle;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * 提供本地化翻译。
 * <br>
 * A utility class that provides translated message/text.
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
public class Translation {

    public final static ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "messages.TinySnakeBundle",
            new ResourceBundle.Control() {
                @Override
                public List<Locale> getCandidateLocales(String baseName, Locale locale) {
                    return List.of(
                            LangBundle.getLocale(),
                            Locale.ENGLISH  // 因为没有安装语言包时IDE默认使用英语。
                    );
                }
            }
    );

    /**
     * 获取本地化翻译。
     *
     * @param key properties文件中的键。
     * @return properties文件中键对应的值。
     */
    public static @NotNull String $message(@NotNull String key) {
        return BUNDLE.getString(key);
    }
}
