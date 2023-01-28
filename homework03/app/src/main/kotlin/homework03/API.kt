package homework03

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import homework03.serializer.csvSerialize
import java.io.File
import java.io.IOException
import java.net.URL

const val redditLink = "https://www.reddit.com/r"

fun getJSON(link: String): String {
    val url = URL(link)
    var result: String = ""
    do {
        try {
            result = url.readText()
        } catch (_: Exception) {
        }
    } while (result.isEmpty())
    return result
}

fun getCommentJSON(name: String, idWithTitle: String): String = getJSON("$redditLink/$name/comments/$idWithTitle/.json")

fun getTopicJSON(name: String, type: String): String = getJSON("$redditLink/$name/$type")

suspend fun getTopic(name: String): TopicSnapshot {
    val mapper = jacksonObjectMapper()
    mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
    mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
    // Get main info of group
    val topicSnapshot: TopicSnapshot = mapper.readValue(
        getTopicJSON(name, "about.json"),
        InfoWrapper::class.java
    ).topicSnapshot

    // Getting topics
    topicSnapshot.topics = mapper.readValue(
        getTopicJSON(name, ".json"),
        TopicsData::class.java
    ).data.topics.map { it.topic }
    return topicSnapshot
}

private fun rec(result: MutableList<MyComment>, commentsInfoWrapper: CommentsInfoWrapper?, prevId: Int = -1, depth: Int = 0) {
    if (commentsInfoWrapper == null) return

    for (comment in commentsInfoWrapper.data.wrapperComments.map { it.data }) {
        result.add(
            MyComment(
                id = comment.id ?: "-",
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

suspend fun getComments(name: String, idWithTitle: String): CommentsSnapshot {
    val mapper = jacksonObjectMapper()
    mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
    mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
    val infoAndComments: List<CommentsInfoWrapper> = mapper.readValue(getCommentJSON(name, idWithTitle))

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

suspend fun saveTopic(topicSnapshot: TopicSnapshot) {
    val topicsCSV = csvSerialize(topicSnapshot.topics, Topic::class)

    saveFile(topicsCSV, "app/src/main/kotlin/resources", "--subjects.csv")
}