package cn.aixcyi.plugin.tinysnake.action

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class TinySnakeHomepageAction : AnAction() {
    override fun actionPerformed(p0: AnActionEvent) {
        BrowserUtil.browse("https://github.com/aixcyi/TinySnake/releases")
    }
}