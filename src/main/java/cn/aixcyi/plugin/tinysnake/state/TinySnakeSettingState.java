package cn.aixcyi.plugin.tinysnake.state;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@State(name = "TinySnakeSettingState", storages = {@Storage(TinySnakeStorage.PREFERENCES_STORAGE_NAME)})
@Service(Service.Level.APP)
public final class TinySnakeSettingState implements PersistentStateComponent<TinySnakeSettingState> {

    public static final TinySnakeSettingState DEFAULT = new TinySnakeSettingState();

    public List<String> myShebangs = List.of(
            "/usr/bin/python3",
            "/usr/bin/env python3",
            "/usr/local/bin/python",
            "./venv/Scripts/python.exe"
    );

    public static TinySnakeSettingState getInstance() {
        return ApplicationManager.getApplication().getService(TinySnakeSettingState.class);
    }

    public @NotNull TinySnakeSettingState getState() {
        return this;
    }

    public void loadState(@NotNull TinySnakeSettingState state) {
        this.myShebangs = List.copyOf(state.myShebangs);
    }
}
