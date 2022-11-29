package homework03

import java.net.URL

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
    do {
        try {
            result = url.readText()
        } catch (error: Exception) {
            println("Unsuccess try to get json")
        }
    } while (result.isEmpty())
    return result
}