package homework03

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import java.io.IOException
import java.net.URL

fun getJSON(link: String): String {
    val url = URL(link)
    var result: String = ""
    do {
        try {
            result = url.readText()
        } catch (_: Exception) {
        }
    } while (result.isEmpty())
    println(result)
    return result
}

fun getJSON(name: String, type: String): String = getJSON("https://www.reddit.com/r/$name/$type")

suspend fun getTopic(name: String): TopicSnapshot {
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
    }
}

suspend fun getComments(commentsLink: String): CommentsSnapshot {
    val mapper = jacksonObjectMapper()
    mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
    mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
    val infoAndComments: List<CommentsInfoWrapper> = mapper.readValue(getJSON(commentsLink))

    val result = CommentsSnapshot()

    rec(result.comments, infoAndComments[1])

    return result
}

suspend fun saveFile(text: String, path: String, name: String) {
    val fileName = "$path/$name"
    val myFile = File(fileName)

    myFile.createNewFile()

    try {
        myFile.printWriter().use { out ->
            out.println(text)
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
}
