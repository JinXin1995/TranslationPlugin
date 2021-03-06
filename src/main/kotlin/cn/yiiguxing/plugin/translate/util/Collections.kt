/*
 * Collections
 *
 * Created by Yii.Guxing on 2017-09-16 0016.
 */
package cn.yiiguxing.plugin.translate.util

import java.util.*

/**
 * Trims the [MutableList] to [maxSize]
 */
fun <T> MutableList<T>.trimToSize(maxSize: Int): Boolean {
    var size = this.size
    val trim = size > 0 && size > maxSize
    when {
        trim && maxSize <= 0 -> clear()
        trim -> while (size > maxSize) removeAt(--size)
    }

    return trim
}

/**
 * Returns a read only list for JVM.
 */
fun <T> List<T>.toJVMReadOnlyList(): List<T> = Collections.unmodifiableList(this)

/**
 * Creates an [Enumeration] for an [Iterator].
 */
fun <T> Iterable<T>.enumeration(): Enumeration<T> = object : Enumeration<T> {
    private val iterator = this@enumeration.iterator()
    override fun hasMoreElements(): Boolean = iterator.hasNext()
    override fun nextElement(): T = iterator.next()
}