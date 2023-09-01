package cn.aixcyi.plugin.tinysnake.dialog;

import cn.aixcyi.plugin.tinysnake.enumeration.SequenceOrder;
import cn.aixcyi.plugin.tinysnake.service.DunderAllOptimizationService;
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
    private JRadioButton radioAppearanceButton;
    private JRadioButton radioInOneLine;
    private ButtonGroup groupOrder;

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

    public boolean isSingleQuote() {
        return radioSingleQuoteStyle.isSelected();
    }

    @Override
    public boolean showAndGet() {
        var state = DunderAllOptimizationService.getInstance().getState();
        switch (state.mySequenceOrder) {
            case APPEARANCE -> groupOrder.setSelected(radioAppearanceButton.getModel(), true);
            case ALPHABET -> groupOrder.setSelected(radioAlphabetOrder.getModel(), true);
            case CHARSET -> groupOrder.setSelected(radioCharOrder.getModel(), true);
        }
        (state.isSingleQuote ? radioSingleQuoteStyle : radioDoubleQuotesStyle).setSelected(true);
        (state.isLineByLine ? radioLineByLine : radioInOneLine).setSelected(true);

        boolean result = super.showAndGet();

        if (result) {
            state.mySequenceOrder = getOrdering();
            state.isSingleQuote = isSingleQuote();
            state.isLineByLine = isLineByLine();
        }
        return result;
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel dialogPanel = new JPanel(new BorderLayout());
        dialogPanel.add(contentPanel, BorderLayout.CENTER);
        return dialogPanel;
    }
}
