package homework03

import com.fasterxml.jackson.databind.ObjectMapper
import java.net.URL

fun getJSON(link: String): String {
    val url = URL(link)
    var result: String = ""
    do {
        try {
            result = url.readText()
        } catch (error: Exception) {
            println("Unsuccess try to get json by url: $url")
        }
    } while (result.isEmpty())
    println(result)
    return result
}

fun getJSON(name: String, type: String): String = getJSON("https://www.reddit.com/r/$name/$type")

fun getTopic(name: String): TopicSnapshot {
    // Get main info of group
    val topicSnapshot: TopicSnapshot = ObjectMapper().readValue(
        getJSON(name, "about.json"),
        InfoWrapper::class.java
    ).topicSnapshot

    // Getting topics
    topicSnapshot.topics = ObjectMapper().readValue(
        getJSON(name, ".json"),
        TopicsData::class.java
    ).data.topics.map { it.topic }
    return topicSnapshot
}

//fun getComments(commentsLink: String): CommentsSnapshot {
//    val commentsSnapshot: CommentsSna
//}
