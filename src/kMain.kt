import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.io.IOException
import jp.yasukazu.transhelp.*
//import org.apache.commons.cli.Options
//import org.apache.commons.cli.DefaultParser
import kotlin.streams.toList
import kotlin.system.exitProcess


fun main(args: Array<String >) {
    var optionMap = mutableMapOf(Pair('f', ""))
    args.filter {it.length >= 3 && it.contains('=')   }
            .forEach {
                val optArg = it.split('=', ignoreCase = true, limit = 2)
                val opt = optArg[0]
                val arg = optArg[1]
                val optChar = opt[0].toLowerCase()
                if (optionMap.contains(optChar))
                    optionMap[optChar] = arg
            }
    //var options = Options()
    //options.addOption("f", true, "Input text file")
    //val parser = DefaultParser()
    //val cmd = parser.parse(options, args)
    val usage_filename = "USAGE.md"
    val usageStream = TransHelp::class.java.getResourceAsStream(usage_filename)
    val filename = if (optionMap.contains('f') && optionMap['f']!!.isNotEmpty()) optionMap['f'] else "input.txt"//if (cmd.hasOption("f") && cmd.getOptionValue("f") != null) cmd.getOptionValue("f") else "input.txt"
    val startDir = System.getProperty("user.dir")
    var path = Paths.get(startDir, filename)
    if (!Files.exists(path)) {
        println("$path does not exits.")
        exitProcess(1)
    }
    try {
        Files.lines(path, StandardCharsets.UTF_8).use { stream -> //
            //Files.lines(Paths.get(usage_filename), StandardCharsets.UTF_8).use { usage_stream ->
            println("Usage:")
            usageStream.bufferedReader().forEachLine(::println)
            println()
            val lines = stream.map<String> { it -> it.trim()} //.toList()
            val tHelp = TransHelp(lines.toList())

            val editorList = tHelp.editAll()
            for (i in tHelp.indices) {
                val hsStr = tHelp[i]
                println("Orig: " + hsStr.str + hsStr.stop)
                val edt = editorList[i]
                println("Edit: " + edt.toString() + edt.stop)
            }
        }
    } catch (e: IOException) {
        print("IOException occured: ")
        e.printStackTrace()
    } catch (e: TranshelpException) {
        println("Sentence is not proper format.")
        e.printStackTrace()
    }

}
