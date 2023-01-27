package homework03

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

data class CommentsInfoWrapper(
    @JsonProperty("data")
    val data: BaseCommentsWrapper
)

data class BaseCommentsWrapper(
    @JsonProperty("children")
    val wrapperComments: List<CommentWrapper>
)

data class CommentWrapper(
    @JsonProperty("data")
    val data: Comment
)

data class Comment(
    @JsonProperty("created_utc") val timeCreate: Int,
)
