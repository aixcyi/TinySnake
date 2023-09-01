package cn.aixcyi.plugin.tinysnake.service;

import cn.aixcyi.plugin.tinysnake.enumeration.SequenceOrder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;

@State(name = "DunderAllOptimizationService", storages = {@Storage(value = "tinySnake.xml")})
@Service(Service.Level.APP)
public final class DunderAllOptimizationService implements PersistentStateComponent<DunderAllOptimizationService.State> {

    private State myState = new State();

    public static DunderAllOptimizationService getInstance() {
        return ApplicationManager.getApplication().getService(DunderAllOptimizationService.class);
    }

//    public static DunderAllOptimizationService getInstance(Project project) {
//        return project.getService(DunderAllOptimizationService.class);
//    }

    public @NotNull State getState() {
        return myState;
    }

    public void loadState(@NotNull State state) {
        myState = state;
    }

    public static class State {
        public SequenceOrder mySequenceOrder;
        public boolean isUseSingleQuote;
        public boolean isEndsWithComma;
        public boolean isLineByLine;

        public State() {
            mySequenceOrder = SequenceOrder.APPEARANCE;
            isUseSingleQuote = false;
            isEndsWithComma = false;
            isLineByLine = false;
        }
    }
}
