package cn.aixcyi.plugin.tinysnake.ui;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class TinySnakeConfigurable implements Configurable {

    @Override
    public String getDisplayName() {
        return "Tiny Snake";
    }

    @Override
    public @Nullable JComponent createComponent() {
        return null;
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() {
    }
}
