package jp.yasukazu.transhelp

import java.util.ArrayList
import java.util.Arrays
import java.util.Collections
import java.util.EnumSet
import java.util.HashMap

import jp.yasukazu.transhelp.KTranshelp.punctEnum

import java.util.HashSet
import java.util.LinkedList

class Editor2
/**
 * Constructor
 * @param list
 * @param stop
 */
(list: List<Any>, stop: Char) : ArrayList<Any>(list) {


    var stop: Char = ' '
        internal set

    enum class cmdEnum private constructor(ch: Char, cmd: Cmd, internal var wch: Char) {
        REVERSE('/', Reverse(), '\uff0f');

        var cmd: Cmd
            internal set
        var char: Char = ' '
            internal set

        init {
            this.char = ch
            this.cmd = cmd
        }
    }

    interface Cmd {
        @Throws(TranshelpException::class)
        fun exec(lst: MutableList<Any>, ch: cmdEnum)
    }

    internal class Void : Cmd {
        override fun exec(lst: MutableList<Any>, ch: cmdEnum) {}
    }

    internal class Reverse : Cmd {
        @Throws(TranshelpException::class)
        override fun exec(lst: MutableList<Any>, ce: cmdEnum) {
            val nList = mutableListOf<Any>()
            try {
                for (list in RunIter(lst, ce)) {
                    if (list.contains(ce)) {
                        val aList = mutableListOf<Any>(list)
                        aList.reverse() // Collections.reverse(aList)
                        aList.remove(ce)
                        nList.addAll(aList)
                    } else
                        nList.addAll(list)
                }
            } catch (e: TranshelpError) {
                throw TranshelpException("Error in Reverse: " + e.message)
            }

            lst.clear()
            lst.addAll(nList)
        }

    }

    /**
     * get run [B op C] from [A B op C D]
     * @author Yasukazu
     * @next()
     */
    internal class RunIter(aa: List<Any>, var sym: cmdEnum) : Iterator<List<Any>>, Iterable<List<Any>> {
        var queue: LinkedList<Any>

        init {
            this.queue = LinkedList(aa)
        }

        fun ispat(aa: List<Any>): Boolean {
            if (aa.size < 2)
                return false
            return if (aa[0] is cmdEnum && aa[0] as cmdEnum == sym && aa[1] !is cmdEnum) true else false
        }


        override fun iterator(): Iterator<List<Any>> {
            return this
        }

        override fun hasNext(): Boolean {
            return queue.size > 0
        }

        override fun next(): List<Any> {
            val rList: MutableList<Any>
            var ps = queue.indexOf(sym)
            if (queue.size < 3 || ps < 0) {
                rList = ArrayList(queue)
                queue.clear()
                return rList
            }
            if (ps == queue.size - 1)
                throw TranshelpError("Ends with $sym")
            when (ps) {
                0 //if (ps == 0)
                -> throw TranshelpError("No item before $sym")
                1 -> {
                    rList = ArrayList()
                    rList.add(queue.poll())
                    while (ispat(queue)) {
                        rList.add(queue.poll())
                        rList.add(queue.poll())
                    }
                    return rList
                }
                else //			if (ps > 1) {
                -> {
                    rList = ArrayList(queue.subList(0, ps - 1))
                    while (ps-- > 1) {
                        queue.poll() // drop
                    }
                    return rList
                }
            }
        }
    }

    init {
        this.stop = stop
    }

    /**
     * make all String of command character into Character
     * @param lst
     */
    internal fun enchar_cmd(list: MutableList<Any>) {
        for (i in list.indices) {
            val it = list[i]
            if (it is String && it.length == 1
                    && cmdCharSet.contains(it[0])) {
                val ch = it[0]
                list[i] = ch
            }

        }
    }

    internal inner class IterIdgComma(var list: List<Any>) : Iterable<List<Any>>, Iterator<List<Any>> {
        var cur: Int = 0
        var size: Int = 0

        init {
            cur = 0
            this.size = list.size
        }

        override fun hasNext(): Boolean {
            return cur < size
        }

        override fun next(): List<Any> {
            if (!hasNext())
                return list.subList(cur, size) // empty sublist
            val rList: List<Any>
            var idx = cur
            while (idx < size) {
                val obj = list[idx]
                if (obj === punctEnum.IDGCOMMA) {
                    rList = list.subList(cur, idx)
                    cur = idx + 2
                    return rList
                }
                ++idx
            }
            rList = list.subList(cur, size)
            cur = size
            return rList
        }

        override fun iterator(): Iterator<List<Any>> {
            return this
        }

    }

    internal fun idgcommaSplitIter(list: List<Any>): Iterable<List<Any>> {
        return IterIdgComma(list)
    }

    @Throws(TranshelpException::class)
    fun recurEdit(ck: cmdEnum) {
        class RecurEdit(var ck: cmdEnum) {
            @Throws(TranshelpException::class)
            fun recurExec(lst: MutableList<Any>, nest: Int) {
                val tmpList = ArrayList<List<Any>>()
                for (split in idgCommaSplit(lst)) {
                    val asplit = mutableListOf<Any>(split)
                    if (split.contains(ck)) {
                        try {
                            ck.cmd.exec(asplit, ck)
                            for (it in asplit) {
                                if (it is EnclosedArray2) {
                                    recurExec(it, nest + 1)
                                }
                            }
                        } catch (e: TranshelpException) {
                            throw TranshelpException(e.message)
                        }

                    }
                    tmpList.add(asplit)
                }
                lst.clear()
                val tmpList_size = tmpList.size
                for (i in 0 until tmpList_size) {
                    val iList = tmpList[i]
                    lst.addAll(iList)
                    if (i < tmpList_size - 1)
                        lst.add(punctEnum.IDGCOMMA)
                }

            }
        }

        val rc = RecurEdit(ck)
        try {
            rc.recurExec(this, 0)
        } catch (e: TranshelpException) {
            throw TranshelpException(e.message)
        }

    }

    override fun toString(): String {
        class ToString {
            var bldr: StringBuilder

            init {
                bldr = StringBuilder()
            }

            fun recur(stack: List<Any>) {
                for (obj in stack) {
                    if (obj is EnclosedArray2) {
                        val print = obj.pair == EnBlock.BracketPair.BRACKET
                        if (print)
                            bldr.append(obj.begin)
                        recur(obj)
                        if (print)
                            bldr.append(obj.end)
                    } else if (obj is punctEnum)
                        bldr.append(obj.ch)
                    else {
                        bldr.append(obj)
                    }
                }
            }

            fun toStr(): String {
                return bldr.toString()
            }
        }

        val ts = ToString()
        ts.recur(this)
        return ts.toStr()
    }

    companion object {
        internal var cmdCharSet: MutableSet<Char>
        internal var cmdCharStr: String? = null
        internal var cmdKeyMap: MutableMap<Char, Cmd>
        internal var cmdCharMap: MutableMap<Char, Char>
        internal var charCmdEnumMap: MutableMap<Char, cmdEnum>

        init {
            cmdCharSet = HashSet()
            EnumSet.allOf(cmdEnum::class.java).forEach { it ->
                cmdCharSet.add(it.char)
                cmdCharSet.add(it.wch)
            }
            cmdKeyMap = HashMap()
            EnumSet.allOf(cmdEnum::class.java).forEach { it -> cmdKeyMap[it.char] = it.cmd }
            cmdCharMap = HashMap()
            EnumSet.allOf(cmdEnum::class.java).forEach { it ->
                cmdCharMap[it.wch] = it.char
                cmdCharMap[it.char] = it.char
            }
            charCmdEnumMap = HashMap()
            EnumSet.allOf(cmdEnum::class.java).forEach { it ->
                charCmdEnumMap[it.wch] = it
                charCmdEnumMap[it.char] = it
            }
        }

        internal fun idgCommaSplit(list: List<Any>): List<List<Any>> {
            var list = list
            val rList = mutableListOf<List<Any>>()
            var idx = 0
            do {
                idx = list.indexOf(punctEnum.IDGCOMMA)
                if (idx >= 0) {
                    if (idx > 0)
                        rList += list.slice(0 until idx) //subList(0, idx))
                    if (idx >= list.size - 1)
                        return rList
                    list = list.slice(idx + 1 until list.size)
                }
                else
                    break
            } while (list.size > 0)
            if (list.size > 0)
                rList.add(list)
            return rList
        }

        fun idgcommaSplitAlloc(list: List<Any>): List<List<Any>> {
            var list = list
            val rList = ArrayList<List<Any>>()
            var idx = 0
            do {
                idx = list.indexOf(punctEnum.IDGCOMMA)
                if (idx >= 0) {
                    if (idx > 0)
                        rList.add(ArrayList(list.subList(0, idx)))
                    if (idx >= list.size - 1)
                        return rList
                    list = list.subList(idx + 1, list.size)
                }
            } while (idx >= 0 && list.size > 0)
            if (list.size > 0)
                rList.add(ArrayList(list))
            return rList
        }

        @JvmStatic
        fun main(args: Array<String>) {
            val run_iter_sample = ArrayList<Any>()
            run_iter_sample.addAll(Arrays.asList(*"a b".split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))
            run_iter_sample.add(Editor2.cmdEnum.REVERSE)
            run_iter_sample.addAll(Arrays.asList(*"cd".split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))
            run_iter_sample.add(Editor2.cmdEnum.REVERSE)
            run_iter_sample.addAll(Arrays.asList(*"e f".split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))
            val run_iter = Editor2.RunIter(run_iter_sample, Editor2.cmdEnum.REVERSE)
            val output_list = ArrayList<List<Any>>()
            for (part_list in run_iter) {
                output_list.add(part_list)
            }
            println(output_list.size)
        }
    }
}
