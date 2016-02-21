package com.crossbowffs.syncshot.model

data class ImageData(
        val id: String?,
        val participantId: String,
        val uploaderId: String,
        val eventId: String,
        val takenTime: Long,
        val imageUuid: String,
        val srcImageUri: String)
