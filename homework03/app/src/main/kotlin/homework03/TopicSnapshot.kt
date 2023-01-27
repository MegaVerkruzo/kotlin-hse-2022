package homework03

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.databind.ObjectMapper
import java.net.URL

data class TopicSnapshot (
    @JsonAlias("created_utc")
    val timeCreate: Int,

    @JsonAlias("active_user_count")
    val countOnlineSubscribers: Int,

    @JsonAlias("subscribers")
    val countAllSubscribers: Int,

    @JsonAlias("public_description")
    val description: String,

    var topics: List<Topic> = listOf()
)

data class Topic (
    val author: String,

    @JsonAlias("created_utc")
    val timeCreate: Int,

    @JsonAlias("ups")
    val countUps: Int,

    @JsonAlias("downs")
    val countDowns: Int,

    val title: String,

    @JsonAlias("selftext")
    val text: String,

    @JsonAlias("selftext_html")
    val textHTML: String
)

data class TopicsData (
    val data: Data
)

data class InfoData (
    @JsonAlias("data")
    val topicSnapshot: TopicSnapshot
)

data class Data (
    @JsonAlias("children")
    val topics: List<Topic>
)

suspend fun getTopic(name: String): TopicSnapshot {
    val topics: List<Topic> = ObjectMapper().readValue(
        URL("https://www.reddit.com/r/$name/.json"),
        TopicsData::class.java
    ).data.topics
    val topicSnapshot: TopicSnapshot = ObjectMapper().readValue(
        URL("https://www.reddit.com/r/$name/about.json"),
        InfoData::class.java
    ).topicSnapshot
    topicSnapshot.topics = topics
    return topicSnapshot
}

fun getJSON(name: String, type: String): String {
    val url = URL("https://www.reddit.com/r/$name/$type")
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