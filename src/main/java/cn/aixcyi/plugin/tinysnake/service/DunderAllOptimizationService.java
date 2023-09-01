package cn.aixcyi.plugin.tinysnake.service;

import cn.aixcyi.plugin.tinysnake.enumeration.SequenceOrder;
import cn.aixcyi.plugin.tinysnake.enumeration.StringQuote;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;

@State(name = "DunderAllOptimizationService")
@Storage("tinySnake.xml")
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
        public SequenceOrder myOrdering;
        public StringQuote myStringStyle;
        public boolean myNewline;

        public State() {
            myOrdering = SequenceOrder.APPEARANCE;
            myStringStyle = StringQuote.SINGLE;
            myNewline = false;
        }
    }
}
