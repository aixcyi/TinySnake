package cn.aixcyi.plugin.tinysnake;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class DunderAllOptimizerDialog extends JDialog {
    private JPanel contentPanel;
    private JButton buttonCancel;
    private JButton buttonOk;
    private JRadioButton radioNatureOrder;
    private JRadioButton radioAlphabetOrder;
    private JRadioButton radioCharOrder;
    private JRadioButton radioFullFill;
    private JRadioButton radioStayAlone;
    private DunderAllEntity all;

    public DunderAllOptimizerDialog() {
        ButtonGroup groupOrdering = new ButtonGroup();
        groupOrdering.add(radioAlphabetOrder);
        groupOrdering.add(radioNatureOrder);
        groupOrdering.add(radioCharOrder);

        ButtonGroup groupNewLine = new ButtonGroup();
        groupNewLine.add(radioStayAlone);
        groupNewLine.add(radioFullFill);

        buttonCancel.addActionListener((event) -> dispose());
        buttonOk.addActionListener(this::onClickOk);
        rootPane.setContentPane(contentPanel);
        rootPane.setDefaultButton(buttonOk);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle("优化 __all__");
        setResizable(false);
        setModal(true);
    }

    private void onClickOk(ActionEvent e) {
        JRadioButton selected;
        if (radioAlphabetOrder.isSelected()) {
            selected = radioAlphabetOrder;
        } else if (radioCharOrder.isSelected()) {
            selected = radioCharOrder;
        } else {
            selected = radioNatureOrder;
        }
        this.all.sort(
                SymbolsOrder.fromLabel(selected.getText()),
                radioStayAlone.isSelected()
        );
        dispose();
    }

    public void setEntity(DunderAllEntity entity) {
        this.all = entity;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("DunderAllOptimizerDialog");
        frame.setContentPane(new DunderAllOptimizerDialog().contentPanel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
