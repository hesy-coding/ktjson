package org.stuff.ktjson

import org.stuff.ktjson.error.InvalidJSONFormatException
import java.io.InputStream
import java.nio.charset.Charset

class JSONArray constructor() : JSONValueBase() {
    private val array = ArrayList<JSONValue>()

    val size: Int
        get() = array.size

    val isEmpty: Boolean
        get() = array.isEmpty()

    init {
        type = JSONType.ARRAY
    }

    constructor(text: String) : this(JSONInputStreamReader(text), false)

    constructor(stream: InputStream, charset: Charset = Charsets.UTF_8) : this(JSONInputStreamReader(stream, charset), false)

    internal constructor(reader: JSONInputStreamReader, ignoreLeft: Boolean) : this() {
        parseJSONAndCheckLeft(reader, ignoreLeft) {
            parseArray(reader)
            reader.readNextChar()
        }
    }

    private fun parseArray(reader: JSONInputStreamReader) {
        if (reader.readFirstUnspaceChar() != '[') {
            throw InvalidJSONFormatException(reader.position, "Expect '[' at front of Array")
        }

        if (reader.readNextUnspaceChar() == ']') {
            return
        }

        do {
            val v = reader.readJSONValue()
            array.add(v)

            val ch = reader.readFirstUnspaceChar()
            if (ch == ']') {
                break
            }

            if (ch != ',') {
                throw InvalidJSONFormatException(reader.position, "Expert ',' between values in array")
            }

            reader.readNextUnspaceChar()
        } while (true)
    }

    fun add(v: Boolean) {
        add(JSONPrimitiveValue(v))
    }

    operator fun set(idx: Int, v: Boolean) {
        this[idx] = JSONPrimitiveValue(v)
    }

    fun add(v: Number) {
        add(JSONPrimitiveValue(v))
    }

    operator fun set(idx: Int, v: Number) {
        this[idx] = JSONPrimitiveValue(v)
    }

    fun add(v: String) {
        add(JSONPrimitiveValue(v))
    }

    operator fun set(idx: Int, v: String) {
        this[idx] = JSONPrimitiveValue(v)
    }

    private fun getValidValue(v: JSONValue?): JSONValue {
        return v ?: JSONPrimitiveValue()
    }

    fun add(v: JSONValue?) {
        array.add(getValidValue(v))
    }

    operator fun set(idx: Int, v: JSONValue?) {
        if(idx < 0 || idx >= size) {
            throw ArrayIndexOutOfBoundsException("$idx out of range(0, ${if (size > 0) size - 1 else 0})")
        }

        array[idx] = getValidValue(v)
    }

    operator fun get(idx: Int): JSONValue {
        return optGet(idx) ?: throw ArrayIndexOutOfBoundsException("$idx out of range(0, ${if (size > 0) size - 1 else 0})")
    }

    fun optGet(idx: Int): JSONValue? {
        return if(idx < 0 || idx >= size) null else array[idx]
    }

    override fun optToJSONArray(): JSONArray? {
        return this
    }

    override fun formatToString(): String {
        val builder = StringBuilder()
        for (value in array) {
            builder.append("$value,")
        }

        if (builder.isNotEmpty()) {
            builder.deleteCharAt(builder.lastIndex)
        }
        return "[$builder]"
    }

    override fun toString(): String {
        return formatToString()
    }

    override fun cloneValue(): JSONValue {
        val json = JSONArray()
        array.map {
            json.add(it.cloneValue())
        }
        return json
    }
}