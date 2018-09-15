package jp.yasukazu.transhelp

// 2018/8/31 YtM @yasukazu.jp
import java.util.ArrayList
import java.util.EnumSet
import java.util.HashSet
import java.util.LinkedList

import java.text.Normalizer
import kotlin.streams.toList

class KTranshelp
//List<HasStopString> sentences;
/**
 * Constructor
 * @param lines
 */
(lines: List<String>) : ArrayList<HasStopString>() {
    internal enum class spaceCharEnum constructor(var ch: Char) {
        SPC('\u0020'),
        W_SPC('\u3000')
    }

    internal enum class punctEnum constructor(c: Char) {
        IDGCOMMA('\u3001'), //KUTEN \
        IDGFSTOP('\u3002'), //TOUTEN o
        EXCL('!'),
        QSTN('?'),
        COMMA(','),
        FLSTOP('.'),
        COLON(':'),
        SEMI(';'),
        WEXCL('\uFF01'),
        WQSTN('\uFF1F'),
        WCOMMA('\uFF0C'),
        WFLSTOP('\uFF0E'),
        WCOLON('\uFF1A'),
        WSEMI('\uFF1B');

        @JvmField
        var ch = c

    }

    init {
        val nlines = lines.stream()
                .map { line -> Normalizer.normalize(line.trim()/* { it <= ' ' }*/,
                        Normalizer.Form.NFC) }
                //.collect<List<String>, Any>(Collectors.toList())
        addAll(getYsentence(nlines.toList()))
    }

    internal fun getYsentence(lines: List<String>): List<HasStopString> {
        val nLines = LinkedList<HasStopString>()
        lines.forEach { line -> nLines.addAll(stop_split(line)) }
        return nLines
    }

    internal fun stop_split(line: String): List<HasStopString> {
        val rgx_dlms = "(?<=[" + punctEnum.IDGFSTOP.ch + "])"
        val lines = line.split(rgx_dlms.toRegex())
        return lines.map { ln -> HasStopString.toHasStopString(ln) }
    }

    /**
     * @exception
     * @return
     */
    @Throws(TranshelpException::class)
    fun editAll(): List<Editor2> {
        val editorList = ArrayList<Editor2>()
        try {
            for (snt in this) {
                val block = EnBlock(snt.str)
                val edt = Editor2(block, snt.stop)
                edt.recurEdit(Editor2.cmdEnum.REVERSE) //do_reverse();
                editorList.add(edt)
            }
        } catch (e: TranshelpException) {
            throw TranshelpException("Improper grammer: " + e.message)
        }

        return editorList
    }

    companion object {
        internal var wideSpaceCharEnum = EnumSet.of(spaceCharEnum.W_SPC)
        internal var wPunctEnumSet = EnumSet.of(
                punctEnum.IDGFSTOP,
                punctEnum.IDGCOMMA,
                punctEnum.WEXCL,
                punctEnum.WQSTN,
                punctEnum.WCOMMA,
                punctEnum.WFLSTOP,
                punctEnum.WCOLON,
                punctEnum.WSEMI
        )

        internal var punctCharSet: MutableSet<Char>
        internal var punctCharStr: String

        init {
            punctCharSet = HashSet()
            EnumSet.allOf(punctEnum::class.java).forEach { it -> punctCharSet.add(it.ch) }
            val sb = StringBuilder()
            for (ch in punctCharSet)
                sb.append(ch)
            punctCharStr = sb.toString()
        }
    }

}
