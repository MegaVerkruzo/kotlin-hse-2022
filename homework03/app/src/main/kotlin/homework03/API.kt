package homework03

import com.fasterxml.jackson.databind.DeserializationFeature
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

fun rec(result: MutableList<MyComment>, commentsInfoWrapper: CommentsInfoWrapper?, prevId: Int = -1, depth: Int = 0) {
    if (commentsInfoWrapper == null) return

    for (comment in commentsInfoWrapper.data.wrapperComments.map { it.data }) {
        println("----------------------------------------------")
        println("author: ${comment.author} with text ${comment.text} and with count replies ${comment.replies?.data?.wrapperComments?.size ?: 0}")
        println("Next replies")
        result.add(
            MyComment(
                id = result.size,
                replyTo = prevId,
                depth = depth,
                timeCreate = comment.timeCreate,
                countLikes = comment.countLikes,
                countDislikes = comment.countDislikes,
                author = comment.author ?: "",
                text = comment.text ?: ""
            )
        )
        rec(result, comment.replies, result.size - 1, depth + 1)
        println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^")
    }
}

fun getComments(commentsLink: String): CommentsSnapshot {
    val mapper = jacksonObjectMapper()
    mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
    mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
    val infoAndComments: List<CommentsInfoWrapper> = mapper.readValue(getJSON(commentsLink))

    val result = CommentsSnapshot()

    rec(result.comments, infoAndComments[1])

    return result
}
