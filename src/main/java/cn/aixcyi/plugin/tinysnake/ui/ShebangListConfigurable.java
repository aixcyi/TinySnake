package cn.aixcyi.plugin.tinysnake.ui;

import cn.aixcyi.plugin.tinysnake.state.TinySnakeSettingState;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

import static cn.aixcyi.plugin.tinysnake.Translation.$message;

public class ShebangListConfigurable implements Configurable {

    private JPanel panel;
    private TinySnakeSettingState state;
    private CollectionListModel<String> model;
    private JBList<String> aList;

    @Override
    public String getDisplayName() {
        return $message("settings.shebangs.name");
    }

    @Override
    public JComponent createComponent() {
        state = TinySnakeSettingState.getInstance().getState();
        model = new CollectionListModel<>(new ArrayList<>(state.myShebangs));
        aList = new JBList<>(model);
        aList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(ToolbarDecorator.createDecorator(aList)
                .setAddAction(button -> {
                    var string = Messages.showInputDialog(
                            $message("GenerateShebang.input.message"),
                            $message("GenerateShebang.input.title"),
                            null
                    );
                    if (string == null || string.isEmpty() || string.isBlank())
                        return;
                    model.add(string);
                    aList.getSelectionModel().setLeadSelectionIndex(model.getSize() - 1);
                })
                .setEditAction(button -> {
                    var string = Messages.showInputDialog(
                            $message("GenerateShebang.input.message"),
                            $message("GenerateShebang.input.title"),
                            null,
                            model.getElementAt(aList.getSelectedIndex()),
                            null
                    );
                    if (string == null || string.isEmpty() || string.isBlank())
                        return;
                    model.setElementAt(string, aList.getSelectedIndex());
                })
                .setRemoveAction(button -> {
                    model.remove(aList.getSelectedIndex());
                    aList.getSelectionModel().setLeadSelectionIndex(aList.getLeadSelectionIndex());
                })
                .addExtraAction(new AnActionButton($message("settings.normal.reset"), AllIcons.Actions.GC) {
                    @Override
                    public @NotNull ActionUpdateThread getActionUpdateThread() {
                        return ActionUpdateThread.EDT;
                    }

                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        restoreToDefault();
                    }
                })
                .createPanel()
        );
        return panel;
    }

    @Override
    public boolean isModified() {
        return !model.toList().equals(state.myShebangs);
    }

    @Override
    public void apply() {
        state.myShebangs = List.copyOf(model.toList());
    }

    @Override
    public void reset() {
        model.removeAll();
        model.addAll(0, state.myShebangs);
    }

    public void restoreToDefault() {
        model.removeAll();
        model.addAll(0, TinySnakeSettingState.DEFAULT.myShebangs);
    }

    @Override
    public void disposeUIResources() {
        panel = null;
        state = null;
        model = null;
        aList = null;
    }
}
