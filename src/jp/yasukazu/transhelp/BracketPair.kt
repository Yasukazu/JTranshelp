package jp.yasukazu.transhelp

enum class BracketPair (val set: CharSequence) {
    PAREN("()"), // maru-kakko
    BRACKET("[]"), // kaku-kakko
    BRACE("{}"), // nami-kakko
    CBRKT("\u300c\u300d"), // kagi-kakko
    WCBRKT("\u300e\u300f"), // niju-kagi-kakko
    TORTOISE_SHELL_BRACKET("\u3014\u3015"), //kikko-kakko
    ANGLE_BRACKET("〈〉"),
    DOUBLE_ANGLE_BRACKET("《》"),
    //CORNER_BRACKET("\u300c\u300d"),
    //WHITE_CORNER_BRACKET("\u300e\u300f"),
    BLACK_LENTICULAR_BRACKET("\u3010\u3011"), // 【】
    NUL("  ");

    //var set: CharArray
    val begin: Char
        get() = set[0]
    val end: Char
        get() = set[1]
    companion object {
        val beginCharSet = BracketPair.values().filterNot { it == NUL }.map {it.begin}.toSet()
        val beginCharMapValue = BracketPair.values().filterNot { it == NUL }.map {Pair(it.begin, it)}.toMap()
    }

/*
    val str = "                arrayOf('\\u3008', '\\u3009'), // ANGLE BRACKET\n"
val split = str.split("//")
val key = split[1].trim().replace(' ', '_')
val param = split[0].split(',')
val p0s = param[0].split("'")
val p0 = p0s[p0s.size - 2]
val p1s = param[1].split("'")
val p1 = p1s[1]
val out = "${key}(\"${p0}${p1}\"),"
println(out)
    init {
        set = CharArray(2)
        set[0] = str[0]
        set[1] = str[1]
    }*/
}