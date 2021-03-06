package jp.yasukazu.transhelp

// 2018/8/31 YtM @yasukazu.jp
import java.io.InputStream
import java.util.ArrayList
import java.util.EnumSet
import java.util.HashSet
import java.util.LinkedList

import java.text.Normalizer
import jp.yasukazu.transhelp.HasStopString2 as HasStopString
import jp.yasukazu.transhelp.Editor2 as Editor
import jp.yasukazu.transhelp.BracketPair

class TransHelp
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

    init {
        val nlines = lines.map { line ->
            line.map { ch ->
                val wideBegin = BracketPair.wideBeginCharMap[ch]
                if (wideBegin != null)
                    wideBegin
                else {
                    val wideEnd = BracketPair.wideEndCharMap[ch]
                    if (wideEnd != null)
                        wideEnd
                    else
                        ch
                }
            }.joinToString("")
            //Normalizer.normalize(line.trim()/* { it <= ' ' }*/,                        Normalizer.Form.NFKC)
        }
                //.collect<List<String>, Any>(Collectors.toList())
        addAll(getYsentence(nlines))
    }

    internal fun getYsentence(lines: List<String>): List<HasStopString> {
        val nLines = LinkedList<HasStopString>()
        lines.forEach { line -> nLines.addAll(stop_split(line)) }
        return nLines
    }

    internal fun stop_split(line: String): List<HasStopString> {
        val rgx_dlms = "(?<=[" + punctEnum.IDGFSTOP.ch + "])"
        val lines = line.split(rgx_dlms.toRegex())
        val fLines = lines.filter { it -> it.isNotEmpty() }
        return fLines.map(HasStopString.Companion::toHasStopString)
    }

    /**
     * @exception
     * @return
     */
    @Throws(TranshelpException::class)
    fun editAll(): List<Editor> {
        val editorList = ArrayList<Editor>()
        try {
            for (snt in this) {
                val block = EnBlock(snt.str)
                val edt = Editor(block, snt.stop)
                edt.recurEdit(Editor.cmdEnum.REVERSE) //do_reverse();
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
        val cjkBracketCharMap = mapOf(
                Pair('\uff08', '('), // Parenthesis
                Pair('\uff09', ')'),
                Pair('\uff3b', '['),
                Pair('\uff3d', ']'), // Square Bracket
                Pair('\uff5b', '{'),
                Pair('\uff5d', '}') // Curly Bracket
        )
        val enclosurePairChars = arrayOf(
                arrayOf('(', ')'),
                arrayOf('[', ']'),
                arrayOf('{', '}'),
                arrayOf('\uff08', '\uff09'), // FULLWIDTH PARENTHESIS
                arrayOf('\u3014', '\u3015'), // TORTOISE SHELL BRACKET
                arrayOf('\uff3b', '\uff3d'), // Fullwidth Square Bracket
                arrayOf('\uff5b', '\uff5d'), // Fullwidth Curly Bracket
                arrayOf('\u3008', '\u3009'), // ANGLE BRACKET
                arrayOf('\u300a', '\u300b'), // DOUBLE ANGLE BRACKET
                arrayOf('\u300c', '\u300d'), // CORNER BRACKET
                arrayOf('\u300e', '\u300f'), // WHITE CORNER BRACKET
                arrayOf('\u3010', '\u3011')  // BLACK LENTICULAR BRACKET
        )
        val enclosureChars = arrayOf( '(', ')', '[', ']', '{', '}',
                '\uff08', '\uff09', '\u3014', '\u3015', '\uff3b', '\uff3d', '\uff5b', '\uff5d',
                '\u3008', '\u3009', '\u300a', '\u300b', '\u300c', '\u300d', '\u300e', '\u300f',
                '\u3010', '\u3011') // (42..59).each{|c|printf "'\\u%x', " % (c + 0xa1a0).chr(Encoding::EUC_JP).encode(Encoding::UTF_8).ord}
        val openEnclosureChars = setOf(enclosurePairChars.map {it[0]}) //enclosureChars.filterIndexed { index, c ->  (index % 1) == 0})
        val enclosureCharMap = HashMap<Char, Char>()
        var punctCharSet: MutableSet<Char>
        var punctCharStr: String
        var usageStream: InputStream
        init {
            punctCharSet = HashSet()
            EnumSet.allOf(punctEnum::class.java).forEach { it -> punctCharSet.add(it.ch) }
            val sb = StringBuilder()
            for (ch in punctCharSet)
                sb.append(ch)
            punctCharStr = sb.toString()

            val thisClass = this::class
            val javaClass = this.javaClass
            val usageFilename = "USAGE.md"
            val jClass = thisClass.java
            usageStream = jClass.getResourceAsStream(usageFilename)
            enclosureChars.indices.forEach { index ->
                if (index and 1 == 0)
                    enclosureCharMap[enclosureChars[index]] = enclosureChars[index + 1]
            }


        }
    }

}
