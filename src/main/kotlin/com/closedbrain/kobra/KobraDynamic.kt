package com.closedbrain.kobra

import com.closedbrain.dynamic.Dynamic
import java.lang.foreign.MemorySegment
import java.lang.ref.Cleaner

@Dynamic
class KobraDynamic(val ptr: MemorySegment, incRef: Boolean = true) {

    private companion object {
        val cleaner: Cleaner = Cleaner.create()
    }

    init {
        if (incRef) {
            PythonAPI.incRef(ptr)
        }
        cleaner.register(this) {
            PythonAPI.decRef(ptr)
        }
    }

    operator fun get(key: String): KobraDynamic {
        return KobraDynamic(PythonAPI.dictGetItem(ptr, key)!!)
    }

    operator fun get(key: Long): KobraDynamic {
        return KobraDynamic(PythonAPI.getListItem(ptr, key)!!)
    }

    operator fun get(key: KobraDynamic): KobraDynamic {
        return when {
            PythonAPI.checkStr(key.ptr) ->
                KobraDynamic(PythonAPI.dictGetItem(ptr, key.ptr)!!)

            else -> throw IllegalArgumentException("Unknown key type")
        }
    }

    operator fun set(key: String, value: KobraDynamic) {
        PythonAPI.dictSetItem(ptr, key, value.ptr)
    }

    operator fun set(key: Long, value: KobraDynamic) {
        PythonAPI.setListItem(ptr, key, value.ptr)
    }

    operator fun set(key: KobraDynamic, value: KobraDynamic) {
        when {
            PythonAPI.checkStr(key.ptr) ->
                PythonAPI.dictSetItem(ptr, key.ptr, value.ptr)

            else -> throw IllegalArgumentException("Unknown key type")
        }
    }

    fun m(key: String): KobraDynamic {
        return KobraDynamic(PythonAPI.PyObject_GetAttrString(ptr, key)!!, false)
    }

    operator fun invoke(vararg values: KobraDynamic): KobraDynamic? {
        assert(PythonAPI.checkCallable(ptr))

        val tuple = PythonAPI.newTuple(values.size.toLong())

        values.forEachIndexed { index, value ->
            PythonAPI.setTupleParam(tuple, index.toLong(), value.ptr)
        }

        val ret = PythonAPI.callObject(ptr, tuple)

        PythonAPI.decRef(tuple)

        if (ret == null) return null

        return KobraDynamic(ret, false)
    }

    operator fun rangeTo(other: primitive<String>): String {
        if (!PythonAPI.checkStr(ptr)) throw AssertionError()

        return PythonAPI.toUTF8(ptr)!!
    }

    operator fun rangeTo(other: primitive<Int>): Int {
        if (!PythonAPI.checkInt(ptr)) throw AssertionError()

        return PythonAPI.getInt(ptr)
    }

    operator fun rangeTo(other: primitive<Long>): Long {
        if (!PythonAPI.checkInt(ptr)) throw AssertionError()

        return PythonAPI.getLong(ptr)
    }

    operator fun rangeTo(other: primitive<Float>): Float {
        if (!PythonAPI.checkFloat(ptr)) throw AssertionError()

        return PythonAPI.getDouble(ptr).toFloat()
    }

    operator fun rangeTo(other: primitive<Double>): Double {
        if (!PythonAPI.checkFloat(ptr)) throw AssertionError()

        return PythonAPI.getDouble(ptr)
    }

    operator fun rangeTo(other: primitive<Boolean>): Boolean {
        if (!PythonAPI.checkBool(ptr)) throw AssertionError()

        return ptr == PythonAPI.pyBoolFromLong(1)
    }

    operator fun rangeTo(other: primitive<Complex>): Complex {
        if (!PythonAPI.checkComplex(ptr)) throw AssertionError()

        return Complex(
            PythonAPI.getComplexRe(ptr),
            PythonAPI.getComplexIm(ptr)
        )
    }

    operator fun rangeTo(other: primitive<List<KobraDynamic>>): List<KobraDynamic> {
        if (!PythonAPI.checkList(ptr)) throw AssertionError()

        val items = PythonAPI.getArrayItems(ptr)

        return List(items.size) { i -> KobraDynamic(items[i])}
    }
}