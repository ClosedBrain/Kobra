package com.closedbrain.kobra

import com.sun.jna.Pointer
import java.io.File
import java.nio.file.Path

class KobraNest(script: String) {
    constructor() : this("")

    private var globalDict: Pointer?
    private var localDict: Pointer?

    init {
        if (PythonAPI.INSTANCE.Py_IsInitialized() == 0) {
            PythonAPI.INSTANCE.Py_Initialize()
        }

        val mainModule = PythonAPI.INSTANCE.PyImport_AddModule("__main__")
        globalDict = PythonAPI.INSTANCE.PyModule_GetDict(mainModule)
        localDict = PythonAPI.INSTANCE.PyDict_New()

        if (!script.isEmpty()) {
            PythonAPI.INSTANCE.PyRun_String(
                script,
                257,
                globalDict,
                localDict
            )
        }
    }

    fun eval(script: String): KobraDynamic? {
        val resultPtr = PythonAPI.INSTANCE.PyRun_String(
            script, 258, globalDict, localDict
        )

        if (resultPtr == null) return null

        return KobraDynamic(resultPtr)
    }

    fun execute(script: String) {
        PythonAPI.INSTANCE.PyRun_String(
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