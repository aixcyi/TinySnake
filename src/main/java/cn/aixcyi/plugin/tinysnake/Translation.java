package cn.aixcyi.plugin.tinysnake;

import com.intellij.lang.LangBundle;

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

    /**
     * 菜单部分的翻译文本。
     */
    public final static BundleProxy MENU = new BundleProxy("messages.Menu");
    /**
     * 设置部分的翻译文本。
     */
    public final static BundleProxy SETTING = new BundleProxy("messages.Setting");

    public static class BundleProxy {
        private final ResourceBundle bundle;

        public BundleProxy(String baseName) {
            bundle = ResourceBundle.getBundle(baseName, new ResourceBundle.Control() {
                @Override
                public List<Locale> getCandidateLocales(String baseName, Locale locale) {
                    return List.of(
                            LangBundle.getLocale(),
                            Locale.ENGLISH  // 因为没有安装语言包时IDE默认使用英语。
                    );
                }
            });
        }

        public String get(String key) {
            return bundle.getString(key);
        }
    }
}
