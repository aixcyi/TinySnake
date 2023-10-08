package cn.aixcyi.plugin.tinysnake.state;

import cn.aixcyi.plugin.tinysnake.enumeration.SequenceOrder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;

@State(name = "DunderAllOptimizationState", storages = {@Storage(TinySnakeStorage.PREFERENCES_STORAGE_NAME)})
@Service(Service.Level.APP)
public final class DunderAllOptimizationState implements PersistentStateComponent<DunderAllOptimizationState> {
    public SequenceOrder mySequenceOrder = SequenceOrder.APPEARANCE;
    public boolean isUseSingleQuote = false;
    public boolean isEndsWithComma = false;
    public boolean isLineByLine = false;

    public static DunderAllOptimizationState getInstance() {
        return ApplicationManager.getApplication().getService(DunderAllOptimizationState.class);
        // return project.getService(DunderAllOptimizationState.class);
    }

    public @NotNull DunderAllOptimizationState getState() {
        return this;
    }

    public void loadState(@NotNull DunderAllOptimizationState state) {
        this.mySequenceOrder = state.mySequenceOrder;
        this.isUseSingleQuote = state.isUseSingleQuote;
        this.isEndsWithComma = state.isEndsWithComma;
        this.isLineByLine = state.isLineByLine;
    }
}
