package cn.aixcyi.plugin.tinysnake.action

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction

class GotoHomepageAction : DumbAwareAction() {
    override fun actionPerformed(event: AnActionEvent) {
        BrowserUtil.browse("https://github.com/aixcyi/TinySnake/releases")
    }
}