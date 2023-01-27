package homework03

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime
import java.time.ZoneOffset

@JsonIgnoreProperties(ignoreUnknown = true)
data class CommentsInfoWrapper(@JsonProperty("data") val data: BaseCommentsWrapper)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BaseCommentsWrapper(@JsonProperty("children") val wrapperComments: List<CommentWrapper>)

@JsonIgnoreProperties(ignoreUnknown = true)
data class CommentWrapper(@JsonProperty("data") val data: Comment)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Comment(
    @JsonProperty("created_utc") val timeCreate: Int,
    @JsonProperty("ups") val countLikes: Int,
    @JsonProperty("downs") val countDislikes: Int,
    @JsonProperty("body") val text: String?,
    @JsonProperty("author") val author: String?,
    @JsonProperty("replies") val replies: CommentsInfoWrapper?
)

class CommentsSnapshot {
    val timeCreate: Int = LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(3)).toInt()

    val comments: MutableList<MyComment> = mutableListOf()
}

class MyComment(
    val id: Int,
    val replyTo: Int = -1,
    val depth: Int,
    val timeCreate: Int,
    val countLikes: Int,
    val countDislikes: Int,
    val text: String,
    val author: String,
)