package homework03

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
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

fun rec(commentsInfoWrapper: CommentsInfoWrapper?) {
    if (commentsInfoWrapper == null) return

    for (comment in commentsInfoWrapper.data.wrapperComments.map { it.data }) {
        println("author: ${comment.author} with text ${comment.text} and with count replies ${comment.replies?.data?.wrapperComments?.size ?: 0}")
        println("Next replies")
        rec(comment.replies)
    }
}

fun getComments(commentsLink: String) {
    val infoAndComments: List<CommentsInfoWrapper> = jacksonObjectMapper().readValue(getJSON(commentsLink))

    rec(infoAndComments[1])
}
