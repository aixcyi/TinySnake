package cn.aixcyi.plugin.tinysnake.ui;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class DocstringLinkCreator extends DialogWrapper {
    private JPanel contentPane;
    private JTextField textField;
    private JTextField linkField;

    public DocstringLinkCreator(String title) {
        super(true);
        setResizable(true);
        setTitle(title);
        init();
    }

    public String getText() {
        return textField.getText();
    }

    public String getLink() {
        return linkField.getText();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel dialogPanel = new JPanel(new BorderLayout());
        dialogPanel.add(contentPane, BorderLayout.CENTER);
        return dialogPanel;
    }
}
