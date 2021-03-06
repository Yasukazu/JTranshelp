package jp.yasukazu.transhelp

import jp.yasukazu.transhelp.ResourceBundleWithUtf8.UTF8_ENCODING_CONTROL
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.io.IOException
import java.lang.NumberFormatException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.streams.toList
import kotlin.system.exitProcess


class Main(val args: Array<String >) {
    var inputFileName = ""
    init {
        if (args.isEmpty()) {
            usage().forEach(::println)
            throw TranshelpException("Needs input text or command 'f' : input file")
        }
        if (args[0][0] == 'f' && args.size >= 2) {
            inputFileName = args[1]
        }
    }
    fun usage():List<CharSequence> {
        val bundle = ResourceBundle.getBundle("jp.yasukazu.transhelp.usage", UTF8_ENCODING_CONTROL)
        var key = ""
        try {
            //val total = bundle.getString(key).toInt()
            val keyList = bundle.keys.toList().sortedBy {it}//.sortedBy { it.takeLastWhile { c -> c.isDigit() }.toInt() }
            val list = ArrayList<String>()
            keyList.forEach {// (1..total)
                val s = bundle.getString(it)
                list += " ".repeat(it.length - 1) + s
            }
            return list
        }
        catch (mre: MissingResourceException) {
            throw TranshelpException("${mre.message}:No key:$key")
        }
        catch (nfe: NumberFormatException) {
            throw TranshelpException("$nfe : TOTAL is not a number")
        }
        return emptyList()
    }
    fun run() {
        /*
        var optionMap = mutableMapOf(Pair('f', ""))
        args.filter { it.length >= 3 && it.contains('=') }
                .forEach {
                    val optArg = it.split('=', ignoreCase = true, limit = 2)
                    val opt = optArg[0]
                    val arg = optArg[1]
                    val optChar = opt[0].toLowerCase()
                    if (optionMap.contains(optChar))
                        optionMap[optChar] = arg
                }
        val filename = if (optionMap.contains('f') && optionMap['f']!!.isNotEmpty()) optionMap['f'] else "input.txt"//if (cmd.hasOption("f") && cmd.getOptionValue("f") != null) cmd.getOptionValue("f") else "input.txt"
        val startDir = System.getProperty("user.dir") */
        var inputLines = listOf(args[0])
        if (inputFileName.isNotEmpty()) {
            var path = Paths.get(inputFileName)//startDir, filename)
            if (!Files.exists(path)) {
                println("$path does not exits.")
                exitProcess(1)
            }
            try {
                Files.lines(path, StandardCharsets.UTF_8).use { stream ->
                    inputLines = stream.toList()
                }
            } catch (e: IOException) {
                print("IOException occured: ")
                e.printStackTrace()
            }
        }//
                //Files.lines(Paths.get(usage_filename), StandardCharsets.UTF_8).use { usage_stream ->
                //println("Usage:")
                //usage().forEach(::println)
                //usageStream.bufferedReader().forEachLine(::println)
                //println()
         //       val lines = stream.map<String> { it -> it.trim() } //.toList()
        try {
            val tHelp = TransHelp(inputLines)
            val editorList = tHelp.editAll()
            for (i in tHelp.indices) {
                val hsStr = tHelp[i]
                println("Orig: " + hsStr.str + hsStr.stop)
                val edt = editorList[i]
                println("Edit: " + edt.toString() + edt.stop)
            }
        }
        catch (e: TranshelpException) {
            println("Sentence is not proper format.")
            e.printStackTrace()
        }
    }
}
