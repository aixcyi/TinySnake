package cn.aixcyi.plugin.tinysnake.action;

import cn.aixcyi.plugin.tinysnake.DunderAllEntity;
import cn.aixcyi.plugin.tinysnake.dialog.DunderAllOptimizerDialog;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.jetbrains.python.psi.PyFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 该菜单用于格式化 Python 源码中 __all__ 变量的值。
 *
 * @author aixcyi
 */
public class OptimizeDunderAllAction extends PyAction {

    @Override
    public void update(@NotNull AnActionEvent event, @NotNull PyFile file) {
        List<String> list = file.getDunderAll();
        event.getPresentation().setEnabled(list != null && list.size() > 1);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event, @NotNull PyFile file) {
        DunderAllOptimizerDialog dialog = new DunderAllOptimizerDialog();
        if (dialog.showAndGet()) {
            new DunderAllEntity(file).sort(dialog.getOrdering(), dialog.getSingleLine());
        }
    }
}
