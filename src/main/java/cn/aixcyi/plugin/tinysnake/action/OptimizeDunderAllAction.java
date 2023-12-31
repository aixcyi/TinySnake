package cn.aixcyi.plugin.tinysnake.action;

import cn.aixcyi.plugin.tinysnake.DunderAllEntity;
import cn.aixcyi.plugin.tinysnake.SnippetBuilder;
import cn.aixcyi.plugin.tinysnake.SnippetGenerator;
import cn.aixcyi.plugin.tinysnake.ui.DunderAllOptimizer;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.jetbrains.python.psi.PyFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static cn.aixcyi.plugin.tinysnake.Translation.$message;

/**
 * 优化 Python 源码中已经存在的 __all__ 变量的值。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
public class OptimizeDunderAllAction extends PyAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event, @NotNull PyFile file, @NotNull Editor editor) {
        // 准备基础设施
        var hint = HintManager.getInstance();

        // 查找 __all__ 相关
        var all = new DunderAllEntity(file);
        if (all.variable == null) {
            hint.showInformationHint(editor, $message("hint.OptimizeDunderAll.missing"));
            return;
        }

        // 选择优化方式
        var dialog = new DunderAllOptimizer();
        if (!dialog.showAndGet()) return;

        // 执行优化
        var project = file.getProject();
        var list = all.getVariableValue();
        if (list != null) {
            // 构造优化后的代码
            var exporting = all.sort(new ArrayList<>(all.exports), dialog.state.mySequenceOrder);
            var sequences = SnippetBuilder.createSequence(
                    exporting,
                    "[\n",
                    "\n]",
                    dialog.state.isLineByLine,
                    dialog.state.isEndsWithComma,
                    dialog.state.isUseSingleQuote
            );
            var statement = new SnippetGenerator(file).createListLiteral(sequences);
            // 写入编辑器并产生一个撤销选项
            WriteCommandAction.runWriteCommandAction(
                    project,
                    $message("command.OptimizeDunderAll"),
                    null,
                    () -> list.replace(statement)
            );
            hint.showInformationHint(editor, $message("hint.OptimizeDunderAll.done"));
        } else {
            hint.showErrorHint(editor, $message("hint.OptimizeDunderAll.invalid"));
        }
    }
}
