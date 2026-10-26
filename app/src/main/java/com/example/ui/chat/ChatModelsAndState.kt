package com.example.ui.chat

data class ReplyContext(
    val id: String,
    val sender: String,
    val text: String,
    val type: String = "text"
)

enum class ChatAttachmentType(val label: String) {
    GALLERY("Gallery"),
    CAMERA("Camera"),
    VIDEO("Video"),
    AUDIO("Audio"),
    DOCUMENT("Document"),
    VOICE_NOTE("Voice Note"),
    LOCATION("Love Pin"),
    CONTACT("Contact"),
    STICKER("Love Note")
}
