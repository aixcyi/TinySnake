package cn.aixcyi.plugin.tinysnake.action;

import cn.aixcyi.plugin.tinysnake.DunderAllEntity;
import cn.aixcyi.plugin.tinysnake.dialog.DunderAllOptimizerDialog;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.impl.PyElementGeneratorImpl;
import com.jetbrains.python.psi.impl.PyExpressionStatementImpl;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * 优化 Python 源码中已经存在的 __all__ 变量的值。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
public class OptimizeDunderAllAction extends PyAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event, @NotNull PyFile file) {
        // 准备基础设施
        var instance = HintManager.getInstance();
        var editor = event.getData(LangDataKeys.EDITOR_EVEN_IF_INACTIVE);
        if (editor == null) return;

        // 查找 __all__ 相关
        var all = new DunderAllEntity(file);
        if (all.variable == null) {
            instance.showInformationHint(editor, "没有 __all__ 变量");
            return;
        }

        // 选择优化方式
        var dialog = new DunderAllOptimizerDialog();
        if (!dialog.showAndGet()) return;

        // 执行优化
        var project = file.getProject();
        var list = all.getVariableValue();
        if (list != null) {
            // 构造优化后的代码（字符串）
            var exporting = all.sort(new ArrayList<>(all.exports), dialog.getOrdering());
            var text = all.buildValue(exporting, dialog.isFillingRow());

            // 构造优化后的代码（对象）
            var generator = new PyElementGeneratorImpl(project);
            Runnable runnable = () -> list.replace(
                    generator.createFromText(file.getLanguageLevel(), PyExpressionStatementImpl.class, text)
            );

            // 写入编辑器并产生一个撤销选项
            WriteCommandAction.runWriteCommandAction(
                    project, "优化 __all__", "OptimizeDunderAll", runnable
            );
            instance.showInformationHint(editor, "__all__ 优化完毕");
        } else {
            instance.showErrorHint(editor, "变量 __all__ 缺少合法值");
        }
    }
}
