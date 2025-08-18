package com.closedbrain.kobra

import java.lang.foreign.*
import java.lang.invoke.MethodHandle

object PythonAPI {
    private val linker = Linker.nativeLinker()
    private val arena = Arena.global()
    private val lookup: SymbolLookup

    // Type aliases for readability
    private val C_INT = ValueLayout.JAVA_INT
    private val C_LONG = ValueLayout.JAVA_LONG
    private val C_DOUBLE = ValueLayout.JAVA_DOUBLE
    private val C_POINTER = ValueLayout.ADDRESS

    init {
        System.load("C:\\Python\\Python313\\python313.dll")
        lookup = SymbolLookup.loaderLookup()
    }

    // Helper to create method handles
    private fun bind(name: String, returnType: MemoryLayout?, vararg params: MemoryLayout): MethodHandle {
        val descriptor = if (returnType != null)
            FunctionDescriptor.of(returnType, *params)
        else
            FunctionDescriptor.ofVoid(*params)
        return linker.downcallHandle(lookup.find(name).get(), descriptor)
    }

    // Method handle declarations
    private val pyInitialize = bind("Py_Initialize", null)
    private val pyFinalize = bind("Py_Finalize", null)
    private val pyIsInitialized = bind("Py_IsInitialized", C_INT)
    private val pyRunSimpleString = bind("PyRun_SimpleString", C_INT, C_POINTER)
    private val pyRunString = bind("PyRun_String", C_POINTER, C_POINTER, C_INT, C_POINTER, C_POINTER)
    private val pyImportAddModule = bind("PyImport_AddModule", C_POINTER, C_POINTER)
    private val pyModuleGetDict = bind("PyModule_GetDict", C_POINTER, C_POINTER)
    private val pyDictNew = bind("PyDict_New", C_POINTER)
    private val pyDictGetItemString = bind("PyDict_GetItemString", C_POINTER, C_POINTER, C_POINTER)
    private val pyDictSetItemString = bind("PyDict_SetItemString", C_INT, C_POINTER, C_POINTER, C_POINTER)
    private val pyDictGetItem = bind("PyDict_GetItem", C_POINTER, C_POINTER, C_POINTER)
    private val pyDictSetItem = bind("PyDict_SetItem", C_INT, C_POINTER, C_POINTER, C_POINTER)
    private val pyUnicodeAsUTF8 = bind("PyUnicode_AsUTF8", C_POINTER, C_POINTER)
    private val pyObjectStr = bind("PyObject_Str", C_POINTER, C_POINTER)
    private val pyErrPrint = bind("PyErr_Print", null)
    private val pyErrOccurred = bind("PyErr_Occurred", C_POINTER)
    private val pyObjectType = bind("PyObject_Type", C_POINTER, C_POINTER)
    private val pyObjectGetAttrString = bind("PyObject_GetAttrString", C_POINTER, C_POINTER, C_POINTER)
    private val pyLongAsLong = bind("PyLong_AsLong", C_INT, C_POINTER)
    private val pyLongAsLongLong = bind("PyLong_AsLongLong", C_LONG, C_POINTER)
    private val pyFloatAsDouble = bind("PyFloat_AsDouble", C_DOUBLE, C_POINTER)
    private val pyComplexRealAsDouble = bind("PyComplex_RealAsDouble", C_DOUBLE, C_POINTER)
    private val pyComplexImagAsDouble = bind("PyComplex_ImagAsDouble", C_DOUBLE, C_POINTER)
    private val pyBoolFromLong = bind("PyBool_FromLong", C_POINTER, C_INT)
    private val pyIncRef = bind("Py_IncRef", null, C_POINTER)
    private val pyDecRef = bind("Py_DecRef", null, C_POINTER)
    private val pyListSize = bind("PyList_Size", C_LONG, C_POINTER)
    private val pyListGetItem = bind("PyList_GetItem", C_POINTER, C_POINTER, C_LONG)
    private val pyListSetItem = bind("PyList_SetItem", C_POINTER, C_POINTER, C_LONG, C_POINTER)
    private val pyTupleNew = bind("PyTuple_New", C_POINTER, C_LONG)
    private val pyTupleSetItem = bind("PyTuple_SetItem", null, C_POINTER, C_LONG, C_POINTER)
    private val pyObjectCallObject = bind("PyObject_CallObject", C_POINTER, C_POINTER, C_POINTER)

    private val pyObjectIsInstance = bind("PyObject_IsInstance", C_INT, C_POINTER, C_POINTER)
    private val pyCallableCheck = bind("PyCallable_Check", C_INT, C_POINTER)

    private val listTypeObject: MemorySegment by lazy {
        lookup.find("PyList_Type").orElseThrow()
    }

    private val strTypeObject: MemorySegment by lazy {
        lookup.find("PyUnicode_Type").orElseThrow()
    }

    private val intTypeObject: MemorySegment by lazy {
        lookup.find("PyLong_Type").orElseThrow()
    }

    private val floatTypeObject: MemorySegment by lazy {
        lookup.find("PyFloat_Type").orElseThrow()
    }

    private val boolTypeObject: MemorySegment by lazy {
        lookup.find("PyBool_Type").orElseThrow()
    }

    private val complexTypeObject: MemorySegment by lazy {
        lookup.find("PyComplex_Type").orElseThrow()
    }

    // Helper functions
    private fun MemorySegment?.orNull(): MemorySegment = this ?: MemorySegment.NULL

    private fun MemorySegment.toNullable(): MemorySegment? =
        if (this.address() == 0L) null else this

    private inline fun <T> withCString(str: String, block: (MemorySegment) -> T): T {
        val cString = arena.allocateFrom(str)
        return block(cString)
    }

    // Public API
    fun Py_Initialize() {
        pyInitialize.invokeExact()
    }

    fun Py_Finalize() {
        pyFinalize.invokeExact()
    }

    fun Py_IsInitialized(): Int =
        pyIsInitialized.invokeExact() as Int

    fun PyRun_SimpleString(command: String): Int =
        withCString(command) { pyRunSimpleString.invokeExact(it) as Int }

    fun PyRun_String(string: String, start: Int, globals: MemorySegment?, locals: MemorySegment?): MemorySegment? =
        withCString(string) { cString ->
            (pyRunString.invokeExact(cString, start, globals.orNull(), locals.orNull()) as MemorySegment).toNullable()
        }

    fun PyImport_AddModule(name: String): MemorySegment? =
        withCString(name) { (pyImportAddModule.invokeExact(it) as MemorySegment).toNullable() }

    fun PyModule_GetDict(module: MemorySegment?): MemorySegment? =
        (pyModuleGetDict.invokeExact(module.orNull()) as MemorySegment).toNullable()

    fun PyDict_New(): MemorySegment? =
        (pyDictNew.invokeExact() as MemorySegment).toNullable()

    fun dictGetItem(dict: MemorySegment?, key: String): MemorySegment? =
        withCString(key) {
            (pyDictGetItemString.invokeExact(dict.orNull(), it) as MemorySegment).toNullable()
        }

    fun dictSetItem(dict: MemorySegment?, key: String, value: MemorySegment?): Int =
        withCString(key) {
            pyDictSetItemString.invokeExact(dict.orNull(), it, value.orNull()) as Int
        }

    fun dictGetItem(dict: MemorySegment?, key: MemorySegment): MemorySegment? =
        (pyDictGetItem.invokeExact(dict.orNull(), key) as MemorySegment).toNullable()

    fun dictSetItem(dict: MemorySegment?, key: MemorySegment, value: MemorySegment?): Int =
        pyDictSetItem.invokeExact(dict.orNull(), key, value.orNull()) as Int

    fun toUTF8(unicode: MemorySegment?): String? {
        val ptr = pyUnicodeAsUTF8.invokeExact(unicode.orNull()) as MemorySegment
        if (ptr.address() == 0L) return null

        val unboundedPtr = MemorySegment.ofAddress(ptr.address()).reinterpret(Long.MAX_VALUE)
        return unboundedPtr.getString(0)
    }

    fun toString(obj: MemorySegment?): MemorySegment? =
        (pyObjectStr.invokeExact(obj.orNull()) as MemorySegment).toNullable()

    fun PyErr_Print() {
        pyErrPrint.invokeExact()
    }

    fun PyErr_Occurred(): MemorySegment? =
        (pyErrOccurred.invokeExact() as MemorySegment).toNullable()

    fun PyObject_Type(obj: MemorySegment?): MemorySegment? =
        (pyObjectType.invokeExact(obj.orNull()) as MemorySegment).toNullable()

    fun PyObject_GetAttrString(obj: MemorySegment?, attr: String?): MemorySegment? {
        if (attr == null) return null
        return withCString(attr) {
            (pyObjectGetAttrString.invokeExact(obj.orNull(), it) as MemorySegment).toNullable()
        }
    }

    fun getInt(obj: MemorySegment?): Int =
        pyLongAsLong.invokeExact(obj.orNull()) as Int

    fun getLong(obj: MemorySegment?): Long =
        pyLongAsLongLong.invokeExact(obj.orNull()) as Long

    fun getDouble(obj: MemorySegment?): Double =
        pyFloatAsDouble.invokeExact(obj.orNull()) as Double

    fun getComplexRe(obj: MemorySegment?): Double =
        pyComplexRealAsDouble.invokeExact(obj.orNull()) as Double

    fun getComplexIm(obj: MemorySegment?): Double =
        pyComplexImagAsDouble.invokeExact(obj.orNull()) as Double

    fun pyBoolFromLong(value: Int): MemorySegment =
        pyBoolFromLong.invokeExact(value) as MemorySegment

    fun incRef(obj: MemorySegment?) {
        pyIncRef.invokeExact(obj.orNull())
    }

    fun decRef(obj: MemorySegment?) {
        pyDecRef.invokeExact(obj.orNull())
    }

    fun checkList(obj: MemorySegment?): Boolean =
        (pyObjectIsInstance.invokeExact(obj.orNull(), listTypeObject) as Int) != 0

    fun checkStr(obj: MemorySegment?): Boolean =
        (pyObjectIsInstance.invokeExact(obj.orNull(), strTypeObject) as Int) != 0

    fun checkInt(obj: MemorySegment?): Boolean =
        (pyObjectIsInstance.invokeExact(obj.orNull(), intTypeObject) as Int) != 0

    fun checkFloat(obj: MemorySegment?): Boolean =
        (pyObjectIsInstance.invokeExact(obj.orNull(), floatTypeObject) as Int) != 0

    fun checkBool(obj: MemorySegment?): Boolean =
        (pyObjectIsInstance.invokeExact(obj.orNull(), boolTypeObject) as Int) != 0

    fun checkComplex(obj: MemorySegment?): Boolean =
        (pyObjectIsInstance.invokeExact(obj.orNull(), complexTypeObject) as Int) != 0

    fun checkCallable(obj: MemorySegment?): Boolean =
        (pyCallableCheck.invokeExact(obj.orNull()) as Int) != 0

    fun getListSize(list: MemorySegment?): Long =
        pyListSize.invokeExact(list.orNull()) as Long

    fun getListItem(list: MemorySegment?, index: Long): MemorySegment? =
        (pyListGetItem.invokeExact(list.orNull(), index) as MemorySegment).toNullable()

    fun setListItem(list: MemorySegment?, index: Long, value: MemorySegment): MemorySegment? =
    (pyListSetItem.invokeExact(list.orNull(), index, value) as MemorySegment).toNullable()

    fun getArrayItems(list: MemorySegment?): Array<MemorySegment> {
        val size = getListSize(list)
        if (size == 0L) return emptyArray()

        val elements = Array<MemorySegment?>(size.toInt()) { null }

        for (i in 0 until size) {
            elements[i.toInt()] = pyListGetItem.invokeExact(list.orNull(), i) as MemorySegment
        }

        @Suppress("unchecked_cast")
        return elements as Array<MemorySegment>
    }

    fun newTuple(size: Long) =
        pyTupleNew.invokeExact(size) as MemorySegment

    fun setTupleParam(tuple: MemorySegment, index: Long, value: MemorySegment) {
        pyTupleSetItem.invokeExact(tuple, index, value)
    }

    fun callObject(obj: MemorySegment, params: MemorySegment) =
        (pyObjectCallObject.invokeExact(obj, params) as MemorySegment).toNullable()

}