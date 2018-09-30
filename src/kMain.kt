import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.io.IOException
import jp.yasukazu.transhelp.*
import org.apache.commons.cli.Options
import org.apache.commons.cli.DefaultParser
import kotlin.streams.toList


fun main(args: Array<String >) {
    var options = Options()
    options.addOption("f", true, "Input text file")
    val parser = DefaultParser()
    val cmd = parser.parse(options, args)
    val usage_filename = "USAGE.md"
    val usageStream = TransHelp::class.java.getResourceAsStream(usage_filename)
    val filename = if (cmd.hasOption("f") && cmd.getOptionValue("f") != null) cmd.getOptionValue("f") else "input.txt"
    try { var path = Paths.get(filename)
        Files.lines(path, StandardCharsets.UTF_8).use { stream -> //
            //Files.lines(Paths.get(usage_filename), StandardCharsets.UTF_8).use { usage_stream ->
            println("Usage:")
            usageStream.bufferedReader().forEachLine(::println)
                println()
                val lines = stream.map<String> { it -> it.trim()} //.toList()
                val thelp = TransHelp(lines.toList())

                val editorList = thelp.editAll()
                for (i in thelp.indices) {
                    val hstr = thelp[i]
                    println("Orig: " + hstr.str + hstr.stop)
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
