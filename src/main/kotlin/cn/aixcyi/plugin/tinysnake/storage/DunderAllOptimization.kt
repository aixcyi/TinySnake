package cn.aixcyi.plugin.tinysnake.storage

import cn.aixcyi.plugin.tinysnake.Zoo
import com.intellij.openapi.components.*

/**
 * `__all__` 格式化设置。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
@Service(Service.Level.APP)
@State(name = Zoo.DUNDER_ALL_OPTIMIZATION_NAME, storages = [Storage(Zoo.PLUGIN_LEVEL_STORAGE)])
class DunderAllOptimization : SimplePersistentStateComponent<DunderAllOptimization.State>(State()) {

    /** 序列字面值中各个元素的排序方式。 */
    enum class Order {

        /** 按符号出现顺序排序。 */
        APPEARANCE,

        /** 按字母先后顺序排序（不区分大小写）。 */
        ALPHABET,

        /** 按字母先后顺序排序（区分大小写）。 */
        CHARSET;

        override fun toString() = this.ordinal.toString()  // 从0开始
    }

    class State : BaseState() {
        var mySequenceOrder by property(Order.APPEARANCE) { it == Order.APPEARANCE }
        var isUseSingleQuote by property(false)
        var isEndsWithComma by property(false)
        var isLineByLine by property(false)
    }

    companion object {
        fun getInstance() = service<DunderAllOptimization>()
    }
}