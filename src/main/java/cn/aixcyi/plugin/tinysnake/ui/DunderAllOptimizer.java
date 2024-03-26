package cn.aixcyi.plugin.tinysnake.ui;

import cn.aixcyi.plugin.tinysnake.storage.DunderAllOptimization;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

import static cn.aixcyi.plugin.tinysnake.Zoo.message;

public class DunderAllOptimizer extends DialogWrapper {

    public DunderAllOptimization.State state;

    private JPanel       contentPanel;
    private ButtonGroup  groupOrder;
    private JRadioButton radioAlphabetOrder;
    private JRadioButton radioCharOrder;
    private JRadioButton radioAppearanceButton;
    private JCheckBox    checkboxUseSingleQuote;
    private JCheckBox    checkboxEndsWithComma;
    private JCheckBox    checkboxLineByLine;

    public DunderAllOptimizer() {
        super(true);
        setResizable(false);
        setTitle(message("command.OptimizeDunderAll"));
        init();
        state = DunderAllOptimization.getInstance().getState();
    }

    @Override
    public void show() {
        switch (state.getMySequenceOrder()) {
            case APPEARANCE -> groupOrder.setSelected(radioAppearanceButton.getModel(), true);
            case ALPHABET -> groupOrder.setSelected(radioAlphabetOrder.getModel(), true);
            case CHARSET -> groupOrder.setSelected(radioCharOrder.getModel(), true);
        }
        checkboxUseSingleQuote.setSelected(state.isUseSingleQuote());
        checkboxEndsWithComma.setSelected(state.isEndsWithComma());
        checkboxLineByLine.setSelected(state.isLineByLine());

        super.show();
        if (!isOK()) return;

        state.setMySequenceOrder(radioCharOrder.isSelected()
                ? DunderAllOptimization.Order.CHARSET
                : radioAlphabetOrder.isSelected()
                ? DunderAllOptimization.Order.ALPHABET
                : DunderAllOptimization.Order.APPEARANCE
        );
        state.setUseSingleQuote(checkboxUseSingleQuote.isSelected());
        state.setEndsWithComma(checkboxEndsWithComma.isSelected());
        state.setLineByLine(checkboxLineByLine.isSelected());
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel dialogPanel = new JPanel(new BorderLayout());
        dialogPanel.add(contentPanel, BorderLayout.CENTER);
        return dialogPanel;
    }
}