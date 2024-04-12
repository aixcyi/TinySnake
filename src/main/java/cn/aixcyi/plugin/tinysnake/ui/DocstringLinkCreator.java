package cn.aixcyi.plugin.tinysnake.ui;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Docstring 超链接生成窗口。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
public class DocstringLinkCreator extends DialogWrapper {
    private JPanel     contentPane;
    private JTextField textField;
    private JTextField linkField;

    /**
     * @param title 窗口标题。
     */
    public DocstringLinkCreator(String title) {
        super(true);
        setResizable(true);
        setTitle(title);
        init();
    }

    /**
     * 预设超链接的文本。
     *
     * @param text 任意字符串。
     * @return 自身。
     */
    public DocstringLinkCreator setText(String text) {
        textField.setText(text);
        return this;
    }

    /**
     * 预设超链接的指向链接。
     *
     * @param link 任意字符。
     * @return 自身。
     */
    public DocstringLinkCreator setLink(String link) {
        linkField.setText(link);
        return this;
    }

    /**
     * 弹出窗口让用户编辑，并在确认后返回可以放在 docstring 中展示的超链接代码。
     * <p>
     * 注意，如果链接不在 docstring 的开头或结尾（也就是紧邻 {@code """}）的话，那么需要与空格相邻，否则 PyCharm 无法正确渲染。
     *
     * @return 如果用户取消编辑，将返回 {@code null} 。
     */
    @Nullable
    public String showThenGet() {
        if (showAndGet())
            return "`" + textField.getText() + " <" + linkField.getText() + ">`_";
        else
            return null;
    }

    @Override
    @Nullable
    protected JComponent createCenterPanel() {
        JPanel dialogPanel = new JPanel(new BorderLayout());
        dialogPanel.add(contentPane, BorderLayout.CENTER);
        return dialogPanel;
    }
}