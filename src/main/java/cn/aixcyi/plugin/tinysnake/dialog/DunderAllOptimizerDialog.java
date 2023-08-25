package cn.aixcyi.plugin.tinysnake.dialog;

import cn.aixcyi.plugin.tinysnake.enumeration.SequenceOrder;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class DunderAllOptimizerDialog extends DialogWrapper {
    private JPanel contentPanel;
    private JRadioButton radioAlphabetOrder;
    private JRadioButton radioCharOrder;
    private JRadioButton radioLineByLine;
    private JRadioButton radioDoubleQuotesStyle;
    private JRadioButton radioSingleQuoteStyle;

    public DunderAllOptimizerDialog() {
        super(true);
        setResizable(false);
        setTitle("优化 __all__");
        init();
    }

    public SequenceOrder getOrdering() {
        return radioCharOrder.isSelected() ? SequenceOrder.CHARSET
                : radioAlphabetOrder.isSelected() ? SequenceOrder.ALPHABET
                : SequenceOrder.APPEARANCE;
    }

    public boolean isLineByLine() {
        return radioLineByLine.isSelected();
    }

    public Boolean getQuotesStyle() {
        return radioDoubleQuotesStyle.isSelected() ? Boolean.TRUE
                : radioSingleQuoteStyle.isSelected() ? Boolean.FALSE
                : null;
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel dialogPanel = new JPanel(new BorderLayout());
        dialogPanel.add(contentPanel, BorderLayout.CENTER);
        return dialogPanel;
    }
}
