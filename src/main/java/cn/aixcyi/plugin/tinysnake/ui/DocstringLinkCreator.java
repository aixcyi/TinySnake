package cn.aixcyi.plugin.tinysnake.ui;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class DocstringLinkCreator extends DialogWrapper {
    private JPanel     contentPane;
    private JTextField textField;
    private JTextField linkField;

    public DocstringLinkCreator(String title) {
        super(true);
        setResizable(true);
        setTitle(title);
        init();
    }

    public DocstringLinkCreator setText(String text) {
        textField.setText(text);
        return this;
    }

    public DocstringLinkCreator setLink(String link) {
        linkField.setText(link);
        return this;
    }

    @Nullable
    public String showThenGet() {
        if (showAndGet())
            // 前后都额外加一个空格，是为了避免用户没有添加空格，导致渲染失败。
            return " `" + textField.getText() + " <" + linkField.getText() + ">`_ ";
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