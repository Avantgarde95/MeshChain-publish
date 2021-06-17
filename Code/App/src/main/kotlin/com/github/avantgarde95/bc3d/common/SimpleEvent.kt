package com.github.avantgarde95.bc3d.common

typealias SimpleListener<T> = (T) -> Unit

class SimpleEvent<T> {
    private val listenerList = mutableListOf<SimpleListener<T>>()

    fun addListener(listener: SimpleListener<T>) {
        listenerList.add(listener)
    }

    fun removeListener(listener: SimpleListener<T>) {
        listenerList.remove(listener)
    }

    fun fire(value: T) {
        listenerList.forEach { it(value) }
    }
}
