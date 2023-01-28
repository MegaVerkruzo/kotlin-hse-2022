package homework03.serializer

import java.net.URL

const val redditLink = "https://www.reddit.com/r"

fun getJSON(link: String): String {
    val url = URL(link)
    while (true) {
        try {
            return url.readText()
        } catch (_: Exception){}
    }
}

fun getCommentJSON(name: String, idWithTitle: String): String = getJSON("$redditLink/$name/comments/$idWithTitle/.json")

fun getTopicJSON(name: String, type: String): String = getJSON("$redditLink/$name/$type")
