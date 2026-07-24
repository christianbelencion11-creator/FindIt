package com.example.findit.navigation

object Routes {
    const val SPLASH = "splash"
    const val GET_STARTED = "get_started"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password?username={username}"
    const val SETUP_PIN = "setup_pin"
    const val UNLOCK = "unlock"
    const val MAIN = "main"
    const val HOME = "home"
    const val ADD_ITEM = "add_item"
    const val SEARCH = "search"
    const val PROFILE = "profile"
    const val EDIT_PROFILE = "edit_profile"
    const val CHANGE_PASSWORD = "change_password"
    const val NEWS = "news"
    const val HISTORY = "history"
    const val NOTES = "notes"
    const val NOTE_EDITOR = "note_editor?noteId={noteId}"
    const val ABOUT = "about"
    const val PRIVACY_POLICY = "privacy_policy"
    const val ALL_ITEMS = "all_items"
    const val ALL_CATEGORIES = "all_categories"
    const val RECENT_ITEMS = "recent_items"
    const val PROFILE_CROP = "profile_crop?uri={uri}"
    const val ITEM_DETAIL = "item_detail/{itemId}"
    const val EDIT_ITEM = "edit_item/{itemId}"

    fun itemDetail(itemId: Long) = "item_detail/$itemId"

    fun editItem(itemId: Long) = "edit_item/$itemId"

    fun profileCrop(uri: String): String {
        val encoded = java.net.URLEncoder.encode(uri, Charsets.UTF_8.name())
        return "profile_crop?uri=$encoded"
    }

    fun forgotPassword(username: String = ""): String {
        val encoded = java.net.URLEncoder.encode(username, Charsets.UTF_8.name())
        return "forgot_password?username=$encoded"
    }

    fun noteEditor(noteId: Long = 0L): String = "note_editor?noteId=$noteId"
}
