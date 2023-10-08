package cn.aixcyi.plugin.tinysnake.state;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;

@State(name = "DunderAllOptimizationState", storages = {@Storage(TinySnakeStorage.PREFERENCES_STORAGE_NAME)})
@Service(Service.Level.APP)
public final class DunderAllOptimizationState implements PersistentStateComponent<DunderAllOptimizationState> {
    public Order mySequenceOrder = Order.APPEARANCE;
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

    /**
     * 序列字面值中各个元素的排序方式。
     *
     * @author <a href="https://github.com/aixcyi">砹小翼</a>
     */
    public enum Order {

        /**
         * 按符号出现顺序排序。
         */
        APPEARANCE,

        /**
         * 按字母先后顺序排序（不区分大小写）。
         */
        ALPHABET,

        /**
         * 按字母先后顺序排序（区分大小写）。
         */
        CHARSET
    }
}
