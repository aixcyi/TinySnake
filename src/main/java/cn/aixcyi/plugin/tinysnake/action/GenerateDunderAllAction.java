package cn.aixcyi.plugin.tinysnake.action;

import cn.aixcyi.plugin.tinysnake.DunderAllEntity;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.components.JBList;
import com.jetbrains.python.psi.PyFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;

/**
 * 该菜单用于生成或更新 Python 源码中的 __all__ 变量。
 *
 * @author aixcyi
 */
public class GenerateDunderAllAction extends PyAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event, @NotNull PyFile file) {
        DunderAllEntity all = new DunderAllEntity(file);  // 遍历所有顶层表达式获取所有符号

        JBList<String> options = new JBList<>(new CollectionListModel<>(all.symbols));
        JBPopup popup = new PopupChooserBuilder<>(options)
                .setSelectionMode(MULTIPLE_INTERVAL_SELECTION)
                .setRenderer(new ColoredListCellRenderer<>() {
                    @Override
                    protected void customizeCellRenderer(@NotNull JList<? extends String> list,
                                                         String value,
                                                         int index,
                                                         boolean selected,
                                                         boolean hasFocus) {
                        this.append(value);
                        this.setIcon(all.icons.get(index));
                        this.setEnabled(!all.exports.contains(value));
                    }
                })
                .setItemsChosenCallback(all::patch)
                .setAdText("Ctrl+A全选／Ctrl+单击多选／Shift+单击连选")
                .setTitle("选择导出到 __all__ 的符号")
                .setMovable(true)
                .createPopup();  // options 的 EmptyText 在这一步会被覆盖掉

        options.getEmptyText().setText("没有可公开的顶级符号");  // 所以只能在这里设置 EmptyText
        popup.showInBestPositionFor(event.getDataContext());
    }
}
