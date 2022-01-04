package Data

data class ResultYoutubeVideoItem(
    val etag: String,
    val items: List<YoutubeVideoItem>,
    val kind: String,
    val pageInfo: PageInfo
)

data class YoutubeVideoItem(
    val contentDetails: ContentDetails,
    val etag: String,
    val id: String,
    val kind: String,
    val snippet: Snippet
)

// contentRating 은 평소에는 결과없는데 혹시나 결과값이 들어오면 에러 생기니까
// 그때는 https://developers.google.com/youtube/v3/docs/videos?hl=en 여기 참조하셈
// duration 정보 때문에 불러옴
data class ContentDetails(
    val caption: String,
    val contentRating: ContentRating,
    val definition: String,
    val dimension: String,
    val duration: String,
    val licensedContent: Boolean,
    val projection: String
)

// title 때문에 불러옴
data class Snippet(
    val categoryId: String,
    val channelId: String,
    val channelTitle: String,
    val defaultAudioLanguage: String,
    val description: String,
    val liveBroadcastContent: String,
    val localized: Localized,
    val publishedAt: String,
    val tags: List<String>,
    val thumbnails: Thumbnails,
    val title: String
)

data class PageInfo(
    val resultsPerPage: Int,
    val totalResults: Int
)

class ContentRating

data class Localized(
    val description: String,
    val title: String
)

data class Thumbnails(
    val default: Default,
    val high: High,
    val maxres: Maxres,
    val medium: Medium,
    val standard: Standard
)

data class Default(
    val height: Int,
    val url: String,
    val width: Int
)

data class High(
    val height: Int,
    val url: String,
    val width: Int
)

data class Maxres(
    val height: Int,
    val url: String,
    val width: Int
)

data class Medium(
    val height: Int,
    val url: String,
    val width: Int
)

data class Standard(
    val height: Int,
    val url: String,
    val width: Int
)

data class ResultChannelItem(
    val etag: String,
    val items: List<Item>,
    val kind: String,
    val pageInfo: PageInfo
)

data class Item(
    val etag: String,
    val id: String,
    val kind: String,
    val snippet: SnippetForChannel
)

data class SnippetForChannel(
    val country: String,
    val description: String,
    val localized: Localized,
    val publishedAt: String,
    val thumbnails: Thumbnails,
    val title: String
)

data class ThumbnailsForChannel(
    val default: Default,
    val high: High,
    val medium: Medium
)