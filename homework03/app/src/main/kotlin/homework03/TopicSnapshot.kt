package homework03

import com.fasterxml.jackson.core.JsonParser
import java.net.URL
import java.net.URLConnection

data class TopicSnapshot (
    val timeCreate: Int,
    val countOnlineSubscribers: Int,
    val rangSize: Int,
    val description: String
)
// TODO: you need to understand where you may get this data
suspend fun getTopic(name: String): TopicSnapshot {
    return TopicSnapshot(0, 0, 0, "")
}

fun getJSON(name: String): String {
    val url = URL("https://www.reddit.com/r/Kotlin/about.json")
    var result: String = ""
    var i = 0
    do {
        print(i++)
        try {
            result = url.readText()
        } catch (error: Exception) {
            print("noo")
        }
    } while (result.isEmpty())
    return result
}