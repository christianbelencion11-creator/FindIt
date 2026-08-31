package com.example.iremember.model

data class NewsItem(
    val id: String,
    val title: String,
    val source: String,
    val link: String,
    val published: String,
    val imageUrl: String? = null
)

enum class NewsFeed {
    Local,
    International
}
