package cn.aixcyi.plugin.tinysnake.action;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class GotoTinySnakePageAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        BrowserUtil.browse("https://github.com/aixcyi/TinySnake/releases");
    }
}
