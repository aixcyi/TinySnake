package cn.aixcyi.plugin.tinysnake;

import com.intellij.icons.AllIcons.Nodes;
import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.components.JBList;
import com.jetbrains.python.psi.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.*;

import static javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;

/**
 * 在 "生成..." 菜单里添加的 "__all__"。
 * <p>
 * 该菜单用于生成或更新 Python 源码文件中的 __all__ 变量。
 *
 * @author aixcyi
 */
public class GenerateDunderAllAction extends AnAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        PsiFile psi = event.getData(CommonDataKeys.PSI_FILE);
        if (psi == null) return;

        Language language = psi.getLanguage();
        Presentation presentation = event.getPresentation();
        presentation.setVisible(language == Language.findLanguageByID("Python"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        PsiFile psi = event.getData(CommonDataKeys.PSI_FILE);
        if (psi == null) return;
        PyFile file = (PyFile) psi;

        List<Pair<String, Icon>> symbols = new ArrayList<>();
        Set<String> exports = new HashSet<>();

        // 遍历所有顶层表达式获取：类名、函数名、变量名
        file.getStatements().forEach(statement -> {
            if (statement instanceof PyClass) symbols.add(new Pair<>(statement.getName(), Nodes.Class));
            if (statement instanceof PyFunction) symbols.add(new Pair<>(statement.getName(), Nodes.Method));
            if (statement instanceof PyAssignmentStatement assignment) {

                // 变量名需要从赋值表达式 PyAssignmentStatement 中获取
                // 因为存在元组解包，所以需要用循环来提取普通变量
                for (Pair<PyExpression, PyExpression> target : assignment.getTargetsToValuesMapping()) {
                    String varName = target.first.getName();

                    // 顶层的普通变量
                    if (!Objects.equals(varName, "__all__")) {
                        symbols.add(new Pair<>(varName, Nodes.Variable));
                        continue;
                    }

                    // 所有通过 __all__ 公开的符号
                    for (PsiElement child : target.second.getChildren())
                        if (child instanceof PyStringLiteralExpression string)
                            exports.add(string.getStringValue());
                }
            }
            // TODO: if (statement instanceof PyIfStatement) {}
        });

        // TODO:
        //  JBList<Pair<String, Icon>> choices = new JBList<>(symbols);
        //  choices.setEmptyText("没有可公开的顶级符号");

        JBPopup popup = JBPopupFactory.getInstance()
                .createPopupChooserBuilder(symbols)
                .setSelectionMode(MULTIPLE_INTERVAL_SELECTION)
                .setRenderer(new ColoredListCellRenderer<>() {
                    @Override
                    protected void customizeCellRenderer(@NotNull JList<? extends Pair<String, Icon>> list,
                                                         Pair<String, Icon> value,
                                                         int index,
                                                         boolean selected,
                                                         boolean hasFocus) {
                        this.append(value.first);
                        this.setIcon(value.second);
                        this.setEnabled(!exports.contains(value.first));
                    }
                })
                .addListener(new JBPopupListener() {
                    @Override
                    public void beforeShown(@NotNull LightweightWindowEvent e) {
                        JBPopupListener.super.beforeShown(e);
                    }

                    @Override
                    public void onClosed(@NotNull LightweightWindowEvent e) {
                        JBPopupListener.super.onClosed(e);
                    }
                })
                .setAdText("Ctrl+单击 选择多个，Shift+单击 批量选择")
                .setTitle("选择导出到 __all__ 的符号")
                .createPopup();

        popup.showInBestPositionFor(event.getDataContext());
    }
}
