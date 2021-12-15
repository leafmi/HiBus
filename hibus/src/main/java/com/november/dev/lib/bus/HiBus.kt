package com.november.dev.lib.bus

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.lang.NullPointerException
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * Created by jlang on 2021/12/15.
 */

object HiBus {

    private val bus: MutableMap<String, BusMutableLiveData<Any>> by lazy {
        HashMap()
    }

    @Synchronized
    fun <T> with(key: String, type: Class<T>, stick: Boolean = true): BusMutableLiveData<T> {
        if (!bus.containsKey(key)) {
            bus[key] = BusMutableLiveData(stick)
        }
        return bus[key] as BusMutableLiveData<T>
    }

    class BusMutableLiveData<T> private constructor() : MutableLiveData<T>() {
        private var stick: Boolean = false

        constructor(stick: Boolean) : this() {
            this.stick = stick
        }


        override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
            super.observe(owner, observer)
            if (!stick) {
                hook(observer)
            }
        }

        private fun hook(observer: Observer<in T>) {
            val liveDataClass: Class<LiveData<*>> = LiveData::class.java
            val mObserverField: Field = liveDataClass.getDeclaredField("mObserver")
            mObserverField.isAccessible = true
            val mObserverObject: Any = mObserverField.get(this)
            val mObserverClass: Class<*> = mObserverObject.javaClass

            //获取mObserver对象的get方法
            val get: Method = mObserverClass.getDeclaredMethod("get", Any::class.java)
            get.isAccessible = true

            val invokeEntry: Any = get.invoke(mObserverObject, observer)

            //得到entry中的value
            var observerWrapper: Any? = null
            if (invokeEntry != null && invokeEntry is Map.Entry<*, *>) {
                observerWrapper = invokeEntry.value
            }

            if (observerWrapper == null) {
                throw NullPointerException("observerWrapper is null")
            }

            val superClass: Class<*> = observerWrapper.javaClass.superclass
            val mLastVersion: Field = superClass.getDeclaredField("mLastVersion")
            mLastVersion.isAccessible = true

            val mVersion: Field = superClass.getDeclaredField("mVersion")
            mVersion.isAccessible = true

            val mVersionValue = mVersion.get(this)
            mLastVersion.set(observerWrapper, mVersionValue)
        }

    }

}