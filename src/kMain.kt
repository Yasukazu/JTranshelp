import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.io.IOException
import jp.yasukazu.transhelp.*
import kotlin.streams.toList

fun main(args: Array<String >) {
    val usage_filename = "README.md"
    val filename = "input.txt"
    try {
        Files.lines(Paths.get(filename), StandardCharsets.UTF_8).use { stream ->
            Files.lines(Paths.get(usage_filename), StandardCharsets.UTF_8).use { usage_stream ->
                usage_stream.forEach { it -> println(it) }
                val lines = stream.map<String> { it -> it.trim()} .toList()
                val thelp = KTranshelp(lines)

                val editorList = thelp.editAll()
                for (i in thelp.indices) {
                    val hstr = thelp[i]
                    println("Orig: " + hstr.str + hstr.stop)
                    val edt = editorList[i]
                    println("Edit: " + edt.toString() + edt.stop)
                }
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
