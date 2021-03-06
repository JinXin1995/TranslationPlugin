package cn.yiiguxing.plugin.translate

import cn.yiiguxing.plugin.translate.trans.Lang
import cn.yiiguxing.plugin.translate.util.toJVMReadOnlyList
import cn.yiiguxing.plugin.translate.util.trimToSize
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.messages.Topic
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.CollectionBean
import com.intellij.util.xmlb.annotations.Transient
import java.util.*
import kotlin.properties.Delegates

/**
 * 应用数据存储
 */
@State(name = "AppStorage", storages = [(Storage("yiiguxing.translation.xml"))])
class AppStorage : PersistentStateComponent<AppStorage> {

    @CollectionBean private val histories: MutableList<String> = ArrayList(DEFAULT_HISTORY_SIZE)

    /**
     * 最大历史记录长度
     */
    var maxHistorySize by Delegates.vetoable(DEFAULT_HISTORY_SIZE) { _, oldValue: Int, newValue: Int ->
        if (oldValue == newValue || newValue < 0) {
            return@vetoable false
        }

        trimHistoriesSize(newValue)
        true
    }

    /**
     * 记录最后一次的源语言
     */
    var lastSourceLanguage: Lang? = null
    /**
     * 记录最后一次的目标语言
     */
    var lastTargetLanguage: Lang? = null

    @Transient private val dataChangePublisher: HistoriesChangedListener =
            ApplicationManager.getApplication().messageBus.syncPublisher(HistoriesChangedListener.TOPIC)

    override fun getState(): AppStorage = this

    override fun loadState(state: AppStorage) {
        XmlSerializerUtil.copyBean(state, this)
    }

    private fun trimHistoriesSize(maxSize: Int) {
        if (histories.trimToSize(maxSize)) {
            dataChangePublisher.onHistoriesChanged()
        }
    }

    /**
     * @return 历史上记录列表
     */
    fun getHistories(): List<String> = histories.toJVMReadOnlyList()

    /**
     * 添加历史记录
     *
     * @param query 查询
     */
    fun addHistory(query: String) {
        val maxSize = maxHistorySize
        if (maxSize <= 0) {
            return
        }

        histories.run {
            val index = indexOf(query)
            if (index != 0) {
                if (index > 0) {
                    removeAt(index)
                }

                add(0, query)
                trimToSize(maxSize)
                dataChangePublisher.onHistoryItemChanged(query)
            }
        }
    }

    /**
     * 清除历史启启记录
     */
    fun clearHistories() {
        if (!histories.isEmpty()) {
            histories.clear()
            dataChangePublisher.onHistoriesChanged()
        }
    }

    override fun toString(): String = "AppStorage(histories=$histories, dataChangePublisher=$dataChangePublisher)"

    companion object {
        private const val DEFAULT_HISTORY_SIZE = 50

        /**
         * Get the instance of this service.
         *
         * @return the unique [AppStorage] instance.
         */
        val instance: AppStorage
            get() = ServiceManager.getService(AppStorage::class.java)
    }

}

interface HistoriesChangedListener {

    fun onHistoriesChanged()

    fun onHistoryItemChanged(newHistory: String)

    companion object {
        val TOPIC: Topic<HistoriesChangedListener> = Topic.create(
                "TranslateHistoriesChanged",
                HistoriesChangedListener::class.java
        )
    }
}
