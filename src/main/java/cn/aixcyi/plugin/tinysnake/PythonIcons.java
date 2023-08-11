package cn.aixcyi.plugin.tinysnake;

import com.intellij.icons.AllIcons;
import com.intellij.ui.LayeredIcon;
import icons.PythonPsiApiIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Python 相关图标。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 * @see <a href="https://jetbrains.design/intellij/resources/icons_list/">Icons List</a>
 * @see <a href="https://www.jetbrains.com/help/pycharm/symbols.html#common-icons">Visibility modifiers</a>
 */
public class PythonIcons {
    private static @NotNull Icon loads(Icon @NotNull ... icons) {
        LayeredIcon icon = new LayeredIcon(icons.length);
        for (int i = 0; i < icons.length; i++) {
            icon.setIcon(icons[i], i);
        }
        return icon;
    }

    public static final class Nodes {

        // 不是所有以双下划线开头的变量都是特殊变量，属性、函数同理。

        /**
         * 16x16。特殊变量（它们都以双下划线开头）。
         */
        public static final @NotNull Icon Variable = loads(
                AllIcons.Nodes.Variable,
                PythonPsiApiIcons.Nodes.CyanDot
        );
    }
}
