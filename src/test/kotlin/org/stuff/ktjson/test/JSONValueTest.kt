package org.stuff.ktjson.test

import org.junit.Test
import org.stuff.ktjson.CastFailedException
import org.stuff.ktjson.InvalidJSONFormatException
import org.stuff.ktjson.JSONType
import org.stuff.ktjson.JSONValue
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

fun invalidJSONValue(invalid : Collection<String>) {
    for (str in invalid) {
        val msg = "\"$str\" should be invalid json value"
        assertFailsWith<InvalidJSONFormatException>(msg){ JSONValue(str) }
    }
}

fun<T> perform(list: Collection<T>, block: (T) -> Unit) {
    for (str in list) {
        block(str)
    }
}

fun<T> perform(map: Map<String, T>, block: (String, T) -> Unit) {
    for ((k, v) in map) {
        block(k, v)
    }
}

class JSONValueTest {
    @Test
    fun emptyTest() {
        invalidJSONValue(listOf(""))
    }

    @Test
    fun nullTest() {
        perform(listOf("null", "  null  ")) {
            val v = JSONValue(it)
            assertEquals(v.type, JSONType.NULL)
            assertEquals(v.toString(), "null")
            assertEquals(v.convertToString(), "null")

            assertFailsWith<CastFailedException> { v.toBooleanValue() }
            assertFailsWith<CastFailedException> { v.toDoubleValue() }
            assertFailsWith<CastFailedException> { v.toIntegerValue() }
            assertFailsWith<CastFailedException> { v.toStringValue() }
        }

        invalidJSONValue(listOf("null null", "Null", "NULL"))
    }

    @Test
    fun boolTest() {
        perform(mapOf("true" to true, "false" to false)) { k, expect ->
            val v = JSONValue(k)
            assertEquals(JSONType.BOOL, v.type)
            assertEquals(expect, v.toBooleanValue())

            val str = if (expect) "true" else "false"
            assertEquals(str, v.toString())
            assertEquals(str, v.convertToString())

            assertFailsWith<CastFailedException> { v.toDoubleValue() }
            assertFailsWith<CastFailedException> { v.toIntegerValue() }
            assertFailsWith<CastFailedException> { v.toStringValue() }
        }

        invalidJSONValue(listOf("TRUE", "FALSE"))
    }

    @Test
    fun integerTest() {
        perform(mapOf("0" to 0, "-1" to -1, "-1024" to -1024, "1024" to 1024,
                "10.0" to 10, "-10.0" to -10,
                "10.01e2" to 1001, "0.1e2" to 10)) { k, expect ->
            val v = JSONValue(k)
            assertEquals(JSONType.INTEGER, v.type)

            assertEquals(expect, v.toIntegerValue())
            assertEquals(expect.toDouble(), v.toDoubleValue())

            assertEquals("$expect", v.toString())
            assertEquals("$expect", v.convertToString())

            assertFailsWith<CastFailedException> { v.toBooleanValue() }
            assertFailsWith<CastFailedException> { v.toStringValue() }
        }
    }

    @Test
    fun doubleTest() {
        perform(mapOf("0.1" to 0.1, "0.10" to 0.1, "0.1e0" to 0.1,
                "230.0012E2" to 23000.12, "230.0012E+2" to 23000.12, "230.0012E-2" to 2.300012)) { k, expect ->
            val v = JSONValue(k)
            assertEquals(JSONType.DOUBLE, v.type)

            assertEquals(expect, v.toDoubleValue(), "str: ($k)")

            assertEquals("$expect", v.toString())
            assertEquals("$expect", v.convertToString())

            assertFailsWith<CastFailedException> { v.toIntegerValue() }
            assertFailsWith<CastFailedException> { v.toBooleanValue() }
            assertFailsWith<CastFailedException> { v.toStringValue() }
        }

    }

    @Test
    fun invalidNumberTest() {
        invalidJSONValue(listOf("01", "+1", "--1", "0.e1", "0.1e++0", "0.1e", "0x0a"))
    }

    class StringExpect(val expect: String, val escaped: String = "\"$expect\"")

    @Test
    fun stringTest() {
        perform(mapOf("\"\"" to StringExpect(""),
                "\"Hello World\"" to StringExpect("Hello World"),
                """"Hello \r\nWorld"""" to StringExpect("Hello \r\nWorld", "\"Hello \\r\\nWorld\""),
                """"Hello \"World"""" to StringExpect("Hello \"World", "\"Hello \\\"World\""),
                """"Hello \u002AWorld"""" to StringExpect("Hello *World"),
                """"Hello \u002aWorld"""" to StringExpect("Hello *World")))
        { k, expect ->
            val v = JSONValue(k)
            assertEquals(JSONType.STRING, v.type)

            assertEquals(expect.expect, v.toStringValue(), "str: ($k)")
            assertEquals(expect.expect, v.convertToString())

            assertEquals(expect.escaped, v.toString())

            assertFailsWith<CastFailedException> { v.toIntegerValue() }
            assertFailsWith<CastFailedException> { v.toDoubleValue() }
            assertFailsWith<CastFailedException> { v.toBooleanValue() }
        }

        invalidJSONValue(listOf("\"Hello \\U002AWorld\"", "\"Hello \\u2AWorld\"", "\"Hello World", "\"Hello \\World\""))
    }
}