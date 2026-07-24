package com.example.findit.util

/**
 * Checklist lines stored in note body as markdown-style markers.
 * Freeform notes store plain text without requiring these markers.
 */
data class ChecklistItem(
    val text: String,
    val checked: Boolean = false
)

object NoteChecklistCodec {
    private val checkedRegex = Regex("""^\s*[-*]\s*\[x\]\s*(.*)$""", RegexOption.IGNORE_CASE)
    private val uncheckedRegex = Regex("""^\s*[-*]\s*\[\s*\]\s*(.*)$""", RegexOption.IGNORE_CASE)

    fun parse(body: String): List<ChecklistItem> {
        if (body.isBlank()) return listOf(ChecklistItem(""))
        return body.lines().map { line ->
            when {
                checkedRegex.matches(line) -> ChecklistItem(
                    text = checkedRegex.find(line)!!.groupValues[1],
                    checked = true
                )
                uncheckedRegex.matches(line) -> ChecklistItem(
                    text = uncheckedRegex.find(line)!!.groupValues[1],
                    checked = false
                )
                line.isBlank() -> ChecklistItem("")
                else -> ChecklistItem(text = line.trim(), checked = false)
            }
        }.ifEmpty { listOf(ChecklistItem("")) }
    }

    fun serialize(items: List<ChecklistItem>): String =
        items
            .filter { it.text.isNotBlank() || it.checked }
            .joinToString("\n") { item ->
                val mark = if (item.checked) "[x]" else "[ ]"
                "- $mark ${item.text.trim()}"
            }

    fun progress(items: List<ChecklistItem>): Pair<Int, Int> {
        val meaningful = items.filter { it.text.isNotBlank() }
        val done = meaningful.count { it.checked }
        return done to meaningful.size
    }

    fun preview(body: String, isChecklist: Boolean, maxLines: Int = 2): String {
        if (!isChecklist) {
            return body.lines().filter { it.isNotBlank() }.take(maxLines).joinToString("\n")
        }
        val items = parse(body).filter { it.text.isNotBlank() }
        if (items.isEmpty()) return ""
        val (done, total) = progress(items)
        if (total > 0) return "$done of $total done"
        return items.take(maxLines).joinToString("\n") { "• ${it.text}" }
    }
}
