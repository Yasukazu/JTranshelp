package jp.yasukazu.transhelp

import java.util.EnumSet

import jp.yasukazu.transhelp.TransHelp.punctEnum
// 2018/8/31 : wide stop characters
import java.util.HashSet

/**
 * String with stip character like period(aka: full stop), question mark, exclamation mark
 * @author yasukazu
 */
class HasStopString2 {
    var str: String
        internal set
    var stop: Char//? = null
        internal set

    internal constructor(str: String, stop: Char) {
        this.str = str
        this.stop = stop
    }

    internal constructor(str: String, has_stop: Boolean) {
        this.str = if (has_stop) str.substring(0, str.length - 1) else str
        this.stop = if (has_stop) str[str.length - 1] else 0.toChar()//'\0'
    }

    fun nilStop() {
        this.stop = 0.toChar()
    }

    fun appendStop() {
        if (this.stop.toInt() == 0)
            return
        this.str = this.str + this.stop
        nilStop()
    }

    companion object {
        internal var fullStopPunctSet = EnumSet.of(punctEnum.WEXCL, punctEnum.WFLSTOP, punctEnum.IDGFSTOP, punctEnum.WQSTN)
        internal var fullStopCharSet: MutableSet<Char>
        internal var fullStopCharStr: String

        init {
            val sb = StringBuilder()
            fullStopPunctSet.forEach { sc -> sb.append(sc.ch) }
            fullStopCharStr = sb.toString()
            fullStopCharSet = HashSet()
            fullStopPunctSet.forEach { sc -> fullStopCharSet.add(sc.ch) }
            fullStopCharSet.remove(punctEnum.WFLSTOP.ch)
        }

        fun toHasStopString(str: String): HasStopString2 {
            var str = str
            str = str.trim { it <= ' ' }
            if (str.length == 0)
                return HasStopString2("", '\u0000')
            val lastc = str[str.length - 1]
            return if (!fullStopCharSet.contains(lastc)) HasStopString2(str, '\u0000') else HasStopString2(str.substring(0, str.length - 1), lastc)
        }
    }

}

