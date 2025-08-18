package com.closedbrain.kobra

import com.sun.jna.Pointer
import java.io.File
import java.lang.foreign.MemorySegment
import java.nio.file.Path

class KobraNest(script: String) {
    constructor() : this("")

    private var globalDict: MemorySegment?
    private var localDict: MemorySegment?

    init {
        if (PythonAPI.Py_IsInitialized() == 0) {
            PythonAPI.Py_Initialize()
        }

        val mainModule = PythonAPI.PyImport_AddModule("__main__")
        globalDict = PythonAPI.PyModule_GetDict(mainModule)
        localDict = PythonAPI.PyDict_New()

        if (!script.isEmpty()) {
            PythonAPI.PyRun_String(
                script,
                257,
                globalDict,
                localDict
            )
        }
    }

    fun eval(script: String): KobraDynamic? {
        val resultPtr = PythonAPI.PyRun_String(
            script, 258, globalDict, localDict
        )

        if (resultPtr == null) return null

        return KobraDynamic(resultPtr)
    }

    fun execute(script: String) {
        PythonAPI.PyRun_String(
            script, 257, globalDict, localDict
        )
    }

    companion object {
        fun fromImports(vararg imports: String): KobraNest {
            return KobraNest(imports.joinToString("\n") {
                "import $it"
            })
        }

        fun fromFile(path: String): KobraNest {
            return KobraNest(File(path).readText())
        }

        fun fromFile(path: Path): KobraNest {
            return KobraNest(path.toFile().readText())
        }
    }
}