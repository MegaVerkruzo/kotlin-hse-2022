package homework03

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import java.net.URL

@JsonIgnoreProperties(ignoreUnknown = true)
data class InfoWrapper (
    @JsonProperty("data")
    val topicSnapshot: TopicSnapshot
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TopicSnapshot (
    @JsonProperty("created_utc")
    val timeCreate: Int,

    @JsonProperty("active_user_count")
    val countOnlineSubscribers: Int,

    @JsonProperty("subscribers")
    val countAllSubscribers: Int,

    @JsonProperty("public_description")
    val description: String,
) {
    var topics: List<Topic> = emptyList()
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class TopicsData (
    @JsonProperty("data")
    val data: Data
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Data (
    @JsonProperty("children")
    val topics: ArrayList<TopicWrapper>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TopicWrapper (
    @JsonProperty("data")
    val topic: Topic
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Topic (
    @JsonProperty("id")
    val id: String,

    @JsonProperty("author")
    val author: String,

    @JsonProperty("created_utc")
    val timeCreate: Int,

    @JsonProperty("ups")
    val countUps: Int,

    @JsonProperty("downs")
    val countDowns: Int,

    @JsonProperty("title")
    val title: String,

    @JsonProperty("selftext")
    val text: String,

    @JsonProperty("selftext_html")
    val textHTML: String?,

    @JsonProperty("permalink")
    var commentsLink: String = ""
) {
    init {
        commentsLink = "https://www.reddit.com$commentsLink"
    }
}

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

fun getJSON(name: String, type: String): String {
    val url = URL("https://www.reddit.com/r/$name/$type")
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