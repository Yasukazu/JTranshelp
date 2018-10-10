package jp.yasukazu.transhelp

import jp.yasukazu.transhelp.BracketPair
import java.util.ArrayList

class EnclosedArray2 : ArrayList<Any> {

    var pair: BracketPair
    val begin: Char
        get() = pair.begin
    val end: Char
        get() = pair.end

    constructor(list: List<Any>, pair: BracketPair) : super(list) {
        this.pair = pair
    }

    constructor(list: List<Any>) : super(list) {
        this.pair = BracketPair.NUL
    }

    constructor() : super() {
        this.pair = BracketPair.NUL
    }

    fun insert() {
        if (pair != BracketPair.NUL) {
            this.add(0, "" + pair.begin)
            this.add("" + pair.end)
            pair = BracketPair.NUL
        }
    }

    companion object {
        private val serialVersionUID = 1002001L
    }
}
