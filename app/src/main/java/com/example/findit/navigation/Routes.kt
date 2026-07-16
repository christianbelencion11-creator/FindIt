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
    const val CHANGE_PASSWORD = "change_password"
    const val NEWS = "news"
    const val ITEM_DETAIL = "item_detail/{itemId}"

    fun itemDetail(itemId: Long) = "item_detail/$itemId"

    fun forgotPassword(username: String = ""): String {
        val encoded = java.net.URLEncoder.encode(username, Charsets.UTF_8.name())
        return "forgot_password?username=$encoded"
    }
}
