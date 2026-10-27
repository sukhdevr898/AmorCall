package com.example.data.avatar

data class Avatar(
    val id: String,
    val name: String,
    val category: String, // "BOY" | "GIRL"
    val imageUrl: String,
    val thumbnailUrl: String?,
    val tags: List<String> = emptyList()
)
