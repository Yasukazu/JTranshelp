package jp.yasukazu.transhelp

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.Locale
import java.util.PropertyResourceBundle
import java.util.ResourceBundle

/**
 * UTF-8 エンコーディングされたプロパティファイルを [ResourceBundle] クラスで取り扱う。
 */
object ResourceBundleWithUtf8 {
    val UTF8_ENCODING_CONTROL = object : ResourceBundle.Control() {
        /**
         * UTF-8 エンコーディングのプロパティファイルから ResourceBundle オブジェクトを生成します。
         *
         *
         * 参考 :
         * [
 * http://jgloss.sourceforge.net/jgloss-core/jacoco/jgloss.util/UTF8ResourceBundleControl.java.html
](http://jgloss.sourceforge.net/jgloss-core/jacoco/jgloss.util/UTF8ResourceBundleControl.java.html) *
         *
         *
         * @throws IllegalAccessException
         * @throws InstantiationException
         * @throws IOException
         */
        @Throws(IllegalAccessException::class, InstantiationException::class, IOException::class)
        override fun newBundle(baseName: String, locale: Locale, format: String, loader: ClassLoader, reload: Boolean): ResourceBundle {
            val bundleName = toBundleName(baseName, locale)
            val resourceName = toResourceName(bundleName, "properties")

            loader.getResourceAsStream(resourceName).use { `is` -> InputStreamReader(`is`, "UTF-8").use { isr -> BufferedReader(isr).use { reader -> return PropertyResourceBundle(reader) } } }
        }
    }

    /*
    @JvmStatic
    fun main(args: Array<String>) {
        val bundle = ResourceBundle.getBundle("utf8", UTF8_ENCODING_CONTROL)

        println(bundle.getString("いろはにほへと"))
    }*/
}