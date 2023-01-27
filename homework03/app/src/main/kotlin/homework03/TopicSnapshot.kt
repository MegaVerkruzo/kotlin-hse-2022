package homework03

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import kotlin.collections.ArrayList

@JsonIgnoreProperties(ignoreUnknown = true)
data class InfoWrapper(@JsonProperty("data") val topicSnapshot: TopicSnapshot)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TopicSnapshot(
    @JsonProperty("created_utc") val timeCreate: Int,
    @JsonProperty("active_user_count") val countOnlineSubscribers: Int,
    @JsonProperty("subscribers") val countAllSubscribers: Int,
    @JsonProperty("public_description") val description: String
) {
    var topics: List<Topic> = emptyList()

    val pickUpTime: Int = LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(3)).toInt()
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class TopicsData(@JsonProperty("data") val data: Data)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Data(@JsonProperty("children") val topics: ArrayList<TopicWrapper>)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TopicWrapper(@JsonProperty("data") val topic: Topic)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Topic(
    @JsonProperty("id") val id: String,
    @JsonProperty("author") val author: String,
    @JsonProperty("created_utc") val timeCreate: Int,
    @JsonProperty("ups") val countUps: Int,
    @JsonProperty("downs") val countDowns: Int,
    @JsonProperty("title") val title: String,
    @JsonProperty("selftext") val text: String,
    @JsonProperty("selftext_html") val textHTML: String?,
    @JsonProperty("permalink") val commentsLink: String
) {
    fun getCorrectLink(): String = "https://www.reddit.com$commentsLink"
}
