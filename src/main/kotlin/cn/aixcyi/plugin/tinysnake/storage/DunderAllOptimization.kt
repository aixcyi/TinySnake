package cn.aixcyi.plugin.tinysnake.storage

import cn.aixcyi.plugin.tinysnake.Zoo
import com.intellij.openapi.components.*

@Service(Service.Level.APP)
@State(name = Zoo.DUNDER_ALL_OPTIMIZATION_NAME, storages = [Storage(Zoo.PLUGIN_LEVEL_STORAGE)])
class DunderAllOptimization : SimplePersistentStateComponent<DunderAllOptimization.State>(State()) {

    class State : BaseState() {
        var mySequenceOrder by property(Order.APPEARANCE) { it == Order.APPEARANCE }
        var isUseSingleQuote by property(false)
        var isEndsWithComma by property(false)
        var isLineByLine by property(false)
    }

    companion object {
        @JvmStatic
        fun getInstance(): DunderAllOptimization = service()
    }

    /**
     * 序列字面值中各个元素的排序方式。
     *
     * @author <a href="https://github.com/aixcyi">砹小翼</a>
     */
    enum class Order {
        /**
         * 按符号出现顺序排序。
         */
        APPEARANCE {
            override fun toString() = "1"
        },

        /**
         * 按字母先后顺序排序（不区分大小写）。
         */
        ALPHABET {
            override fun toString() = "2"
        },

        /**
         * 按字母先后顺序排序（区分大小写）。
         */
        CHARSET {
            override fun toString() = "3"
        }
    }
}