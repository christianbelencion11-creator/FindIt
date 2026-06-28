package com.example.findit.navigation

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val SETUP_PIN = "setup_pin"
    const val UNLOCK = "unlock"
    const val MAIN = "main"
    const val HOME = "home"
    const val ADD_ITEM = "add_item"
    const val SEARCH = "search"
    const val PROFILE = "profile"
    const val ITEM_DETAIL = "item_detail/{itemId}"

    fun itemDetail(itemId: Long) = "item_detail/$itemId"
}
