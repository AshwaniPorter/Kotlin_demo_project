package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class NotesResponse<T>(
    val dataMessage:T,
    val success: Boolean
)
