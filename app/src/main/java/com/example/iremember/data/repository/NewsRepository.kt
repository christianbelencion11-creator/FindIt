package com.example.iremember.data.repository

import android.content.Context
import android.telephony.TelephonyManager
import com.example.iremember.model.NewsFeed
import com.example.iremember.model.NewsItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.Locale
import java.util.TimeZone

class NewsRepository {

    sealed class Result {
        data class Success(val items: List<NewsItem>) : Result()
        data class Error(val message: String) : Result()
    }

    data class LocalRegion(
        val countryCode: String,
        val displayName: String
    )

    suspend fun fetch(feed: NewsFeed, region: LocalRegion): Result =
        withContext(Dispatchers.IO) {
            val url = when (feed) {
                NewsFeed.Local -> localRssUrl(region)
                NewsFeed.International -> INTERNATIONAL_RSS_URL
            }
            fetchRss(url)
        }

    private fun fetchRss(rssUrl: String): Result {
        return try {
            val connection = (URL(rssUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 12_000
                readTimeout = 12_000
                requestMethod = "GET"
                setRequestProperty("User-Agent", "IRemember/1.0")
            }
            connection.connect()
            if (connection.responseCode !in 200..299) {
                return Result.Error("Could not load news (HTTP ${connection.responseCode}).")
            }
            val items = connection.inputStream.use { stream ->
                parseRss(stream.bufferedReader().readText())
            }
            if (items.isEmpty()) {
                Result.Error("No headlines available right now.")
            } else {
                Result.Success(items)
            }
        } catch (_: Exception) {
            Result.Error("Couldn't load news. Check your internet and try again.")
        }
    }

    private fun parseRss(xml: String): List<NewsItem> {
        val factory = XmlPullParserFactory.newInstance().apply { isNamespaceAware = true }
        val parser = factory.newPullParser().apply {
            setInput(xml.reader())
        }
        val items = mutableListOf<NewsItem>()
        var event = parser.eventType
        var inItem = false
        var title = ""
        var link = ""
        var pubDate = ""
        var source = ""
        var description = ""
        var mediaUrl = ""

        while (event != XmlPullParser.END_DOCUMENT && items.size < 40) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    val name = parser.name?.lowercase().orEmpty()
                    when {
                        name == "item" -> {
                            inItem = true
                            title = ""
                            link = ""
                            pubDate = ""
                            source = ""
                            description = ""
                            mediaUrl = ""
                        }
                        !inItem -> Unit
                        name == "title" -> title = parser.nextText().orEmpty().trim()
                        name == "link" -> link = parser.nextText().orEmpty().trim()
                        name == "pubdate" -> pubDate = parser.nextText().orEmpty().trim()
                        name == "source" -> source = parser.nextText().orEmpty().trim()
                        name == "description" -> description = parser.nextText().orEmpty().trim()
                        name == "enclosure" || name == "content" -> {
                            val urlAttr = parser.getAttributeValue(null, "url")
                                ?: parser.getAttributeValue("", "url")
                            if (!urlAttr.isNullOrBlank() && looksLikeImage(urlAttr)) {
                                mediaUrl = urlAttr.trim()
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> if (
                    parser.name.equals("item", ignoreCase = true) && inItem
                ) {
                    inItem = false
                    if (title.isNotBlank() && link.isNotBlank()) {
                        val resolvedLink = unwrapGoogleNewsLink(link)
                        val imageFromDesc = extractImgFromHtml(description)
                        val imageUrl = mediaUrl.ifBlank { imageFromDesc }
                            .ifBlank { faviconFor(resolvedLink) }
                            .ifBlank { null }
                        items += NewsItem(
                            id = resolvedLink.ifBlank { title },
                            title = cleanTitle(title),
                            source = source.ifBlank { guessSource(title) },
                            link = resolvedLink,
                            published = shortDate(pubDate),
                            imageUrl = imageUrl
                        )
                    }
                }
            }
            event = parser.next()
        }
        return items
    }

    private fun looksLikeImage(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains(".jpg") ||
            lower.contains(".jpeg") ||
            lower.contains(".png") ||
            lower.contains(".webp") ||
            lower.contains(".gif") ||
            lower.contains("image")
    }

    private fun extractImgFromHtml(html: String): String {
        if (html.isBlank()) return ""
        val regex = Regex("""src\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE)
        return regex.find(html)?.groupValues?.getOrNull(1)?.trim().orEmpty()
    }

    private fun faviconFor(articleUrl: String): String {
        val host = runCatching { URI(articleUrl).host }.getOrNull()?.removePrefix("www.")
        if (host.isNullOrBlank()) return ""
        return "https://www.google.com/s2/favicons?domain=$host&sz=128"
    }

    private fun cleanTitle(raw: String): String {
        val dash = raw.lastIndexOf(" - ")
        return if (dash > 0) raw.substring(0, dash).trim() else raw
    }

    private fun guessSource(rawTitle: String): String {
        val dash = rawTitle.lastIndexOf(" - ")
        return if (dash > 0) rawTitle.substring(dash + 3).trim() else "News"
    }

    private fun shortDate(raw: String): String {
        if (raw.isBlank()) return ""
        // e.g. "Wed, 15 Jul 2026 12:00:00 GMT" -> "Wed, 15 Jul"
        val parts = raw.split(" ")
        return if (parts.size >= 4) {
            "${parts[0].removeSuffix(",")}, ${parts[1]} ${parts[2]}"
        } else {
            raw.take(16)
        }
    }

    private fun unwrapGoogleNewsLink(link: String): String {
        val marker = "url="
        val idx = link.indexOf(marker)
        if (idx < 0) return link
        val encoded = link.substring(idx + marker.length).substringBefore("&")
        return try {
            URLDecoder.decode(encoded, Charsets.UTF_8.name())
        } catch (_: Exception) {
            link
        }
    }

    companion object {
        private const val INTERNATIONAL_RSS_URL =
            "https://news.google.com/rss/headlines/section/topic/WORLD?hl=en&gl=US&ceid=US:en"

        fun detectLocalRegion(context: Context): LocalRegion {
            val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            val networkIso = telephony?.networkCountryIso.orEmpty().trim()
            val simIso = telephony?.simCountryIso.orEmpty().trim()
            val timezoneHint = when (TimeZone.getDefault().id) {
                "Asia/Manila" -> "PH"
                else -> ""
            }
            val localeIso = Locale.getDefault().country.trim()

            val raw = sequenceOf(networkIso, simIso, timezoneHint, localeIso)
                .map { it.uppercase(Locale.US) }
                .firstOrNull { it.length == 2 }
                .orEmpty()
                .ifBlank { "PH" }

            val code = if (raw in knownCodes) raw else "PH"
            val display = Locale.Builder()
                .setRegion(code)
                .build()
                .getDisplayCountry(Locale.ENGLISH)
                .takeIf { it.isNotBlank() && !it.equals(code, ignoreCase = true) }
                ?: if (code == "PH") "Philippines" else code

            return LocalRegion(countryCode = code, displayName = display)
        }

        private val knownCodes = setOf(
            "PH", "US", "GB", "AU", "CA", "SG", "JP", "KR", "IN", "MY", "ID", "TH", "VN",
            "DE", "FR", "IT", "ES", "BR", "MX", "AE", "SA", "NZ", "CN", "HK", "TW"
        )

        private fun localRssUrl(region: LocalRegion): String {
            val code = region.countryCode.takeIf { it in knownCodes } ?: "PH"
            val name = region.displayName.ifBlank { "Philippines" }
            val geo = URLEncoder.encode(name, Charsets.UTF_8.name())
            return "https://news.google.com/rss/headlines/section/geo/$geo" +
                "?hl=en-$code&gl=$code&ceid=$code:en"
        }
    }
}
