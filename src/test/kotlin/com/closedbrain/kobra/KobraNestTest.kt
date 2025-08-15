package com.closedbrain.kobra

import org.junit.jupiter.api.Test
import java.util.Random

class KobraNestTest {

    @Test
    fun createEmpty() {
        KobraNest()
    }

    @Test
    fun createFromScript() {
        KobraNest("print(\"This is a test string\")")
    }

    @Test
    fun createFromImports() {
        KobraNest.fromImports("math")
    }

    fun getRandomString(length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    @Test
    fun stringPrimitive() {
        for (i in 1..10) {
            val rs = getRandomString(i)
            assert(KobraNest().eval("\"$rs\"")!!..primitive<String>() == rs)
        }
    }

    @Test
    fun intPrimitive() {
        for (i in Int.MIN_VALUE..Int.MAX_VALUE step (UInt.MAX_VALUE / 100u).toInt()) {
            assert(KobraNest().eval("$i")!!..primitive<Int>() == i)
        }
    }

    @Test
    fun longPrimitive() {
        for (i in Long.MIN_VALUE..Long.MAX_VALUE step (ULong.MAX_VALUE / 100uL).toLong()) {
            assert(KobraNest().eval("$i")!!..primitive<Long>() == i)
        }
    }

    @Test
    fun testExecute() {
        val nest = KobraNest()
        nest.execute("variable = 5")
        assert(nest.eval("variable")!!..primitive<Int>() == 5)
    }

    @Test
    fun testFloat() {
        val rand = Random()
        (1..10).forEach { _ ->
            val r = rand.nextFloat()
            assert(KobraNest().eval("$r")!!..primitive<Float>() == r)
        }
    }

    @Test
    fun testDouble() {
        val rand = Random()
        (1..10).forEach { _ ->
            val r = rand.nextDouble()
            assert(KobraNest().eval("$r")!!..primitive<Double>() == r)
        }
    }

    @Test
    fun testBoolean() {
        listOf(true to "True", false to "False").forEach {
            assert(KobraNest().eval(it.second)!!..primitive<Boolean>() == it.first)
        }
    }

    @Test
    fun testComplex() {
        KobraNest().eval("3+4j")!!..primitive<Complex>() == Complex(3.0, 1.0)
        print(KobraNest().eval("4")!!)
    }
}