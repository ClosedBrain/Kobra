package com.closedbrain.kobra

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer

interface PythonAPI : Library {
    companion object {
        val INSTANCE: PythonAPI = Native.load("C:\\Python\\Python313-arm\\python313.dll", PythonAPI::class.java)
    }

    // Core Python C API functions
    fun Py_Initialize()
    fun Py_Finalize()
    fun Py_IsInitialized(): Int
    fun PyRun_SimpleString(command: String): Int
    fun PyRun_String(string: String, start: Int, globals: Pointer?, locals: Pointer?): Pointer?
    fun PyImport_AddModule(name: String): Pointer?
    fun PyModule_GetDict(module: Pointer?): Pointer?
    fun PyDict_New(): Pointer?
    fun PyDict_GetItemString(dict: Pointer?, key: String): Pointer?
    fun PyDict_SetItemString(dict: Pointer?, key: String, value: Pointer?): Int
    fun PyUnicode_AsUTF8(unicode: Pointer?): String?
    fun PyObject_Str(obj: Pointer?): Pointer?
    fun Py_DecRef(obj: Pointer?)
    fun PyErr_Print()
    fun PyErr_Occurred(): Pointer?
    fun PyObject_Type(obj: Pointer?): Pointer?
    fun PyObject_GetAttrString(obj: Pointer?, attr: String?): Pointer?
    fun PyLong_AsLong(obj: Pointer?): Int
    fun PyLong_AsLongLong(obj: Pointer?): Long
    fun PyFloat_AsDouble(obj: Pointer?): Double

    fun PyComplex_RealAsDouble(obj: Pointer?): Double
    fun PyComplex_ImagAsDouble(obj: Pointer?): Double

    fun PyBool_FromLong(int: Int): Pointer
}