package cn.aixcyi.plugin.tinysnake;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class DunderAllOptimizerDialog extends DialogWrapper {
    private JPanel contentPanel;
    private JRadioButton radioNatureOrder;
    private JRadioButton radioAlphabetOrder;
    private JRadioButton radioCharOrder;
    private JRadioButton radioFullFill;
    private JRadioButton radioStayAlone;

    public DunderAllOptimizerDialog() {
        super(true);
        ButtonGroup groupOrdering = new ButtonGroup();
        groupOrdering.add(radioAlphabetOrder);
        groupOrdering.add(radioNatureOrder);
        groupOrdering.add(radioCharOrder);

        ButtonGroup groupNewLine = new ButtonGroup();
        groupNewLine.add(radioStayAlone);
        groupNewLine.add(radioFullFill);

        setResizable(false);
        setTitle("优化 __all__");
        init();
    }

    public SymbolsOrder getOrdering() {
        JRadioButton selected;
        if (radioAlphabetOrder.isSelected()) {
            selected = radioAlphabetOrder;
        } else if (radioCharOrder.isSelected()) {
            selected = radioCharOrder;
        } else {
            selected = radioNatureOrder;
        }
        return SymbolsOrder.fromLabel(selected.getText());
    }

    public boolean getSingleLine() {
        return radioStayAlone.isSelected();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel dialogPanel = new JPanel(new BorderLayout());
        dialogPanel.add(contentPanel, BorderLayout.CENTER);
        return dialogPanel;
    }
}
