package cn.aixcyi.plugin.tinysnake.action;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.jetbrains.python.psi.PyFile;
import org.jetbrains.annotations.NotNull;

public class GotoTinySnakePageAction extends PyAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event, @NotNull PyFile file, @NotNull Editor editor) {
        BrowserUtil.browse("https://github.com/aixcyi/TinySnake/releases");
    }
}
