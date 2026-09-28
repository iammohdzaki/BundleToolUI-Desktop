package data.model

data class ClickableText(
    val text: String,
    val url: String? = null,
    val onClick: (() -> Unit)? = null
)
