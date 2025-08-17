package com.closedbrain.kobra

import com.closedbrain.dynamic.Dynamic
import com.sun.jna.Pointer

@Dynamic
value class KobraDynamic(val ptr: Pointer) {
    operator fun get(key: String): KobraDynamic {
        TODO("implement it you fucking idiot")
    }

    operator fun set(key: String, value: KobraDynamic) {
        TODO("implement it you fucking idiot")
    }

    operator fun invoke(vararg values: KobraDynamic): KobraDynamic {
        TODO("implement it you fucking idiot")
    }

    operator fun rangeTo(other: primitive<String>): String {
        if (getType() != "str") throw AssertionError()

        val strPtr = PythonAPI.INSTANCE.PyObject_Str(ptr)

        return PythonAPI.INSTANCE.PyUnicode_AsUTF8(strPtr)!!
    }

    operator fun rangeTo(other: primitive<Int>): Int {
        if (getType() != "int") throw AssertionError()

        return PythonAPI.INSTANCE.PyLong_AsLong(ptr)
    }

    operator fun rangeTo(other: primitive<Long>): Long {
        if (getType() != "int") throw AssertionError()

        return PythonAPI.INSTANCE.PyLong_AsLongLong(ptr)
    }

    operator fun rangeTo(other: primitive<Float>): Float {
        if (getType() != "float") throw AssertionError()

        return PythonAPI.INSTANCE.PyFloat_AsDouble(ptr).toFloat()
    }

    operator fun rangeTo(other: primitive<Double>): Double {
        if (getType() != "float") throw AssertionError()

        return PythonAPI.INSTANCE.PyFloat_AsDouble(ptr)
    }

    operator fun rangeTo(other: primitive<Boolean>): Boolean {
        if (getType() != "bool") throw AssertionError()

        return ptr == PythonAPI.INSTANCE.PyBool_FromLong(1)
    }

    operator fun rangeTo(other: primitive<Complex>): Complex {
        if (getType() != "complex") throw AssertionError()

        return Complex(
            PythonAPI.INSTANCE.PyComplex_RealAsDouble(ptr),
            PythonAPI.INSTANCE.PyComplex_ImagAsDouble(ptr)
        )
    }

    private fun getType(): String {
        val typePtr = PythonAPI.INSTANCE.PyObject_Type(ptr)

        val namePtr = PythonAPI.INSTANCE.PyObject_GetAttrString(typePtr, "__name__")

        return PythonAPI.INSTANCE.PyUnicode_AsUTF8(namePtr)!!
    }
}