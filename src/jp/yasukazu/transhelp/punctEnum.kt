package jp.yasukazu.transhelp

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