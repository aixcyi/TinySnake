package cn.aixcyi.plugin.tinysnake.dialog;

import cn.aixcyi.plugin.tinysnake.enumeration.SequenceOrder;
import cn.aixcyi.plugin.tinysnake.service.DunderAllOptimizationService;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

import static cn.aixcyi.plugin.tinysnake.Translation.$message;

public class DunderAllOptimizerDialog extends DialogWrapper {

    public DunderAllOptimizationService.State state;

    private JPanel contentPanel;
    private ButtonGroup groupOrder;
    private JRadioButton radioAlphabetOrder;
    private JRadioButton radioCharOrder;
    private JRadioButton radioAppearanceButton;
    private JCheckBox checkboxUseSingleQuote;
    private JCheckBox checkboxEndsWithComma;
    private JCheckBox checkboxLineByLine;

    public DunderAllOptimizerDialog() {
        super(true);
        setResizable(false);
        setTitle($message("command.OptimizeDunderAll"));
        init();
        state = DunderAllOptimizationService.getInstance().getState();
    }

    @Override
    public boolean showAndGet() {
        switch (state.mySequenceOrder) {
            case APPEARANCE -> groupOrder.setSelected(radioAppearanceButton.getModel(), true);
            case ALPHABET -> groupOrder.setSelected(radioAlphabetOrder.getModel(), true);
            case CHARSET -> groupOrder.setSelected(radioCharOrder.getModel(), true);
        }
        checkboxUseSingleQuote.setSelected(state.isUseSingleQuote);
        checkboxEndsWithComma.setSelected(state.isEndsWithComma);
        checkboxLineByLine.setSelected(state.isLineByLine);

        if (!super.showAndGet()) {
            return false;
        }

        state.mySequenceOrder = radioCharOrder.isSelected() ? SequenceOrder.CHARSET
                : radioAlphabetOrder.isSelected() ? SequenceOrder.ALPHABET
                : SequenceOrder.APPEARANCE;
        state.isUseSingleQuote = checkboxUseSingleQuote.isSelected();
        state.isEndsWithComma = checkboxEndsWithComma.isSelected();
        state.isLineByLine = checkboxLineByLine.isSelected();
        return true;
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel dialogPanel = new JPanel(new BorderLayout());
        dialogPanel.add(contentPanel, BorderLayout.CENTER);
        return dialogPanel;
    }
}
