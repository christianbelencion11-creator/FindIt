package com.example.findit.model

data class Note(
    val id: Long = 0,
    val ownerUid: String = "",
    val title: String,
    val body: String = "",
    val remindAt: Long = 0L,
    val remindEnabled: Boolean = false,
    val pinned: Boolean = false,
    val accent: Int = 0,
    val isChecklist: Boolean = true,
    val dateCreated: Long = System.currentTimeMillis(),
    val dateUpdated: Long = System.currentTimeMillis()
)

/** Brand accent swatches for note cards / editor (index 0–4). */
object NoteAccents {
    val colors = listOf(
        0xFF16A34A.toInt(), // green
        0xFF0D9488.toInt(), // teal
        0xFF2563EB.toInt(), // blue
        0xFFD97706.toInt(), // amber
        0xFFDB2777.toInt()  // rose
    )

    fun colorInt(index: Int): Int = colors[index.coerceIn(0, colors.lastIndex)]
}
