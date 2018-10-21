package jp.yasukazu.transhelp

enum class BracketPair (val set: CharSequence, val wide: CharSequence?=null) {
    PAREN("()", "\uff08\uff09"), // maru-kakko
    BRACKET("[]", "\uff3b\uff3d"), // kaku-kakko
    BRACE("{}", "\uff5b\uff5d"), // nami-kakko
    CORNER_BRACKET("\u300c\u300d"), // kagi-kakko　CBRKT
    WHITE_CORNER_BRACKET("\u300e\u300f"), // niju-kagi-kakko
    TORTOISE_SHELL_BRACKET("\u3014\u3015"), //kikko-kakko
    ANGLE_BRACKET("〈〉"),
    DOUBLE_ANGLE_BRACKET("《》"),
    BLACK_LENTICULAR_BRACKET("\u3010\u3011"), // 【】
    NUL("" + 0.toChar() + (-1).toChar());

    val begin: Char
        get() = set[0]
    val end: Char
        get() = set[1]

    companion object {
        //val beginCharSet = BracketPair.values().filterNot { it == NUL }.map {it.begin}.toSet()
        val beginCharMap = BracketPair.values().filterNot { it == NUL }.map {Pair(it.begin, it)}.toMap()
        val wideBeginCharMap = BracketPair.values().filter { it.wide != null}.map {Pair(it.wide!![0], it.begin)}.toMap()
        val wideEndCharMap = BracketPair.values().filter { it.wide != null}.map {Pair(it.wide!![1], it.end)}.toMap()
    }
}