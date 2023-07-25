package cn.aixcyi.plugin.tinysnake.action;

import cn.aixcyi.plugin.tinysnake.DunderAllEntity;
import cn.aixcyi.plugin.tinysnake.dialog.DunderAllOptimizerDialog;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.PyListLiteralExpression;
import com.jetbrains.python.psi.impl.PyElementGeneratorImpl;
import com.jetbrains.python.psi.impl.PyExpressionStatementImpl;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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
        if (!dialog.showAndGet()) return;

        DunderAllEntity all = new DunderAllEntity(file);
        Project project = file.getProject();
        if (all.variable == null) return;
        if (!(all.variable.findAssignedValue() instanceof PyListLiteralExpression list)) return;

        List<String> exporting = all.sort(new ArrayList<>(all.exports), dialog.getOrdering());
        String text = all.buildValue(exporting, dialog.isFillingRow());

        PyElementGeneratorImpl generator = new PyElementGeneratorImpl(project);
        Runnable runnable = () -> list.replace(
                generator.createFromText(file.getLanguageLevel(), PyExpressionStatementImpl.class, text)
        );
        WriteCommandAction.runWriteCommandAction(
                project, "优化 __all__", "OptimizeDunderAll", runnable
        );
    }
}
