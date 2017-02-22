package nittcprocon.glathlete

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * AndroidのContextから呼ぶ便利なメソッドたち
 * import staticして使う
 */

internal object UtilsDroid {
    /* 生リソースをテキストとして読み、Stringで返す */
    fun readRawTextFile(resId: Int): String {
        val inputStream = MyContexts.applicationContext.resources.openRawResource(resId)
        try {
            val reader = BufferedReader(InputStreamReader(inputStream))
            val sb = StringBuilder()
            var line: String? = reader.readLine()
            while (line != null) {
                sb.append(line).append("\n")
                line = reader.readLine()
            }
            reader.close()
            return sb.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        throw RuntimeException("readRawTextFile: failed")
    }
}
