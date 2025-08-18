package com.closedbrain.kobra

import org.junit.jupiter.api.Test
import java.util.*

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
        assert(KobraNest().eval("3+4j")!!..primitive<Complex>() == Complex(3.0, 4.0))
    }

    @Test
    fun testList() {
        val correctList = listOf(1, 2, 3)
        val kobraList = KobraNest().eval("[1, 2, 3]")!!..primitive<List<KobraDynamic>>()

        kobraList.forEachIndexed { index, dynamic ->
            assert(correctList[index] == dynamic..primitive<Int>())
        }

        assert(correctList.size == kobraList.size)
    }

    @Test
    fun testDictItem() {
        assert(KobraNest().eval("{\"test\": 1}")!!["test"]..primitive<Int>() == 1)
    }

    @Test
    fun testDictItemInternalString() {
        val nest = KobraNest()

        val string = nest.eval("\"test\"")!!

        assert(nest.eval("{\"test\": 1}")!![string]..primitive<Int>() == 1)
    }

    @Test
    fun testDictSetItem() {
        val nest = KobraNest()

        val one = nest.eval("1")!!

        val dict = nest.eval("{\"test\": 0}")!!

        dict["test"] = one

        assert(dict["test"]..primitive<Int>() == 1)
    }

    @Test
    fun testDictSetItemInternalString() {
        val nest = KobraNest()

        val one = nest.eval("1")!!

        val key = nest.eval("\"test\"")!!

        val dict = nest.eval("{\"test\": 0}")!!

        dict[key] = one

        assert(dict[key]..primitive<Int>() == 1)
    }

    @Test
    fun testMember() {
        assert(
            KobraNest().eval("\"test\"")!!
                .m("__class__")
                .m("__name__")..primitive<String>() == "str"
        )
    }

    @Test
    fun testGetListItem() {
        assert(KobraNest().eval("[0, 1]")!![1]..primitive<Int>() == 1)
    }

    @Test
    fun testSetListItem() {
        val nest = KobraNest()
        val list = nest.eval("[0, 0]")!!

        val one = nest.eval("1")!!

        list[1] = one

        assert(list[1]..primitive<Int>() == 1)
    }

    @Test
    fun invoke() {
        val nest = KobraNest("import math")

        val four = nest.eval("4")!!

        assert(nest.eval("math")!!.m("sqrt")(four)!!..primitive<Float>() == 2f)
    }
}