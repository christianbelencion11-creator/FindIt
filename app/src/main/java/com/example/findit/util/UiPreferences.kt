package com.example.findit.util

import android.content.Context
import android.content.SharedPreferences
import com.example.findit.model.NewsFeed

class UiPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun hasFabOffset(): Boolean =
        prefs.contains(KEY_FAB_OFFSET_X) || prefs.contains(KEY_FAB_OFFSET_Y)

    fun getFabOffsetX(): Float = prefs.getFloat(KEY_FAB_OFFSET_X, 0f)

    fun getFabOffsetY(): Float = prefs.getFloat(KEY_FAB_OFFSET_Y, 0f)

    fun setFabOffset(x: Float, y: Float) {
        prefs.edit()
            .putFloat(KEY_FAB_OFFSET_X, x)
            .putFloat(KEY_FAB_OFFSET_Y, y)
            .apply()
    }

    fun getNewsFeed(): String = prefs.getString(KEY_NEWS_FEED, NewsFeed.Local.name) ?: NewsFeed.Local.name

    fun setNewsFeed(feed: NewsFeed) {
        prefs.edit().putString(KEY_NEWS_FEED, feed.name).apply()
    }

    fun isLocalDataWarningDismissed(): Boolean =
        prefs.getBoolean(KEY_LOCAL_DATA_WARNING_DISMISSED, false)

    fun setLocalDataWarningDismissed(dismissed: Boolean) {
        prefs.edit().putBoolean(KEY_LOCAL_DATA_WARNING_DISMISSED, dismissed).apply()
    }

    companion object {
        private const val PREFS_NAME = "findit_ui_prefs"
        private const val KEY_FAB_OFFSET_X = "fab_offset_x"
        private const val KEY_FAB_OFFSET_Y = "fab_offset_y"
        private const val KEY_NEWS_FEED = "news_feed"
        private const val KEY_LOCAL_DATA_WARNING_DISMISSED = "local_data_warning_dismissed"
    }
}
