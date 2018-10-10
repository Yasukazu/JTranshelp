package jp.yasukazu.transhelp

enum class BracketPair (str: CharSequence) {
    PAREN("()"), // maru-kakko
    BRACKET("[]"), // kaku-kakko
    BRACE("{}"), // nami-kakko
    CBRKT("\u300c\u300d"), // kagi-kakko
    WCBRKT("\u300e\u300f"), // niju-kagi-kakko

    NUL("  ");

    var set: CharArray
    val begin: Char
        get() = set[0]
    val end: Char
        get() = set[1]

    init {
        set = CharArray(2)
        set[0] = str[0]
        set[1] = str[1]
    }
}