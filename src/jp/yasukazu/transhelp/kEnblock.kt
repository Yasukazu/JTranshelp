package jp.yasukazu.transhelp


import java.util.ArrayList
import java.util.Arrays
import java.util.HashMap
import java.util.HashSet

class EnBlock @Throws(TranshelpException::class)
constructor(txt: String) : ArrayList<Any>() {

    init {
        val list: List<Any>
        try {
            list = _load(txt, 0)
        } catch (e: TranshelpException) {
            throw TranshelpException(e.message)
        }

        addAll(list)
    }

    companion object {
        internal var dlmSet: MutableSet<Char>
        internal var dlmMap: MutableMap<Char, Char>
        val bracketPairSet = setOf(BracketPair.values().map {it.begin})
        val bracketPairMap = mutableMapOf<Char, BracketPair>()
        init {
            dlmSet = HashSet()
            for (ch in Editor2.cmdCharSet)
                dlmSet.add(ch)
            dlmSet.add(punctEnum.IDGCOMMA.ch)
            dlmMap = HashMap(Editor2.cmdCharMap)
            dlmMap[punctEnum.IDGCOMMA.ch] = punctEnum.IDGCOMMA.ch
            BracketPair.values().forEach { bracketPairMap[it.begin] = it }
        }

        fun getPair(ch: Char): BracketPair {
            val pair = bracketPairMap[ch]
            return if (pair == null) BracketPair.NUL else pair
            /*
            when (ch) {
                '(' -> return BracketPair.PAREN
                '[' -> return BracketPair.BRACKET
                '{' -> return BracketPair.BRACE
                '\u300c' -> return BracketPair.CBRKT
                '\u300e' -> return BracketPair.WCBRKT
            }*/
        }

        internal var MAX_NEST = 9
        @Throws(TranshelpException::class)
        fun bracket_content(str: String, pair: BracketPair): String {
            var nest = 1
            var ind = 0
            while (ind < str.length) {
                val ch = str[ind]
                if (ch == pair.begin) {
                    nest += 1
                    if (nest > MAX_NEST)
                        throw TranshelpException(String.format("Over maximum nest %d", MAX_NEST))
                } else if (ch == pair.end) {
                    nest -= 1
                    if (nest == 0)
                        return str.substring(0, ind)
                }
                ++ind
            }
            if (nest != 0) {
                System.err.println(str)
                throw TranshelpException(String.format("%d nest remains.", nest))
            }
            return ""
        }

        fun en_paren(s: String): String {
            return "($s)"
        }

        fun rgx_remaining(ch: Char): String {
            return String.format("(?<=%1s)|(?=%1s)", ch, ch)
        }

        fun rgx_command(ch: Char): String {
            return String.format("(?<=%1s)|(?=%1s)", ch, ch)
        }

        fun rgx_multi_remaining(vararg elel: Char): String {
            val sb = StringBuilder()
            for (elm in elel)
                sb.append(rgx_remaining(elm))
            return sb.toString()
        }

        fun rgx_set_remaining(set: Set<Char>): String {
            val sb = StringBuilder()
            for (elm in set)
                sb.append(rgx_remaining(elm))
            return sb.toString()
        }

        @Throws(TranshelpException::class)
        fun load(st: String): List<Any> {
            return _load(st, 0)
        }


        internal val spc_dlmrx = "[\\s" + TransHelp.spaceCharEnum.W_SPC.ch + "]+"
        internal val idgcomma_dlmrx = '('.toString() + rgx_remaining(punctEnum.IDGCOMMA.ch) + ')'.toString()
        internal val dlmrx = arrayOf(en_paren(spc_dlmrx), en_paren(idgcomma_dlmrx)).joinToString("|")//, en_paren(rgx_set_remaining(Editor2.cmdCharSet)));
        internal fun dlmrx_convert(buff: CharSequence): List<Any> {
            val rList = mutableListOf<Any>()
            buff.split(dlmrx.toRegex()).dropLastWhile { it.isEmpty() }.forEach {tk ->
            //for (tk in Arrays.asList(*buff.toString().split(dlmrx.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())) {
                if (tk.length == 1) {
                    val ch = tk[0]
                    if (ch == punctEnum.IDGCOMMA.ch)
                        rList.add(punctEnum.IDGCOMMA)
                    else if (Editor2.cmdCharSet.contains(ch)) {
                        val ce = Editor2.charCmdEnumMap[ch]
                        if (ce != null)
                            rList += ce
                    }
                    else
                        rList.add(tk)
                } else
                    rList.add(tk)
            }
            return rList
        }

        @Throws(TranshelpException::class)
        internal fun _load(st: String, level: Int): List<Any> {
            if (level > MAX_NEST) {
                throw TranshelpException("Too deep nest!")
            }
            val stack = ArrayList<Any>()
            val buff = StringBuilder()
            //String dlmrx = "[\\s" + TransHelp.spaceCharEnum.W_SPC.ch + "]+";
            var pos = 0
            while (pos < st.length) {
                val ch = st[pos]
                //val pair = EnBlock.getPair(ch)
                if (ch in BracketPair.beginCharMap) { //pair != BracketPair.NUL) {
                    if (buff.length > 0 && buff.toString().trim().length > 0) { //  { it <= ' ' } predicate:Unicode <= ' '
                        stack.addAll(dlmrx_convert(buff))
                        buff.setLength(0)
                    }
                    if (pos + 1 >= st.length)
                        return stack
                    val nst = st.substring(pos + 1)
                    val pair = BracketPair.beginCharMap[ch]
                    if (pair != null) {
                        try {
                            val n2st = bracket_content(nst, pair)
                            if (n2st.length > 0) {
                                stack.add(EnclosedArray2(_load(n2st, level + 1), pair))
                                pos += n2st.length + 1
                            } else {
                                stack.add(EnclosedArray2(_load(nst, level + 1), pair))
                                return stack
                            }
                        } catch (e: TranshelpException) {
                            throw TranshelpException("${e.message}:bracket_content:$nst")
                        }
                    }
                } else {
                    buff.append(ch)
                }
                ++pos
            }
            if (buff.length > 0 && buff.toString().trim().length > 0)
                stack.addAll(dlmrx_convert(buff))
            return stack
        }
    }

}
