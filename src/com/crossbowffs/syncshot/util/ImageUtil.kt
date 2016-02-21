package com.crossbowffs.syncshot.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.crossbowffs.syncshot.MyApp
import com.crossbowffs.syncshot.model.EventData
import com.crossbowffs.syncshot.model.ImageData
import com.crossbowffs.syncshot.model.ParticipationData
import com.microsoft.azure.storage.CloudStorageAccount
import com.microsoft.azure.storage.blob.BlobContainerPermissions
import com.microsoft.azure.storage.blob.BlobContainerPublicAccessType
import java.io.File
import java.util.*

// Ehhhh, not too much point in obfuscating this since any determined
// hacker can easily deobfuscate it anyways
private val STORAGE_CONN_STR =
        "DefaultEndpointsProtocol=https;" +
        "AccountName=syncshot;" +
        "AccountKey=i0kGsMgl6r5jrFbP9qs+Try840g2MeVZlV9mnru7rfO9UvtfL9k6yC6w0YosEkGPeyZQjAqWis1CaKp56EHDSg==;" +
        "BlobEndpoint=https://syncshot.blob.core.windows.net/;" +
        "TableEndpoint=https://syncshot.table.core.windows.net/;" +
        "QueueEndpoint=https://syncshot.queue.core.windows.net/;" +
        "FileEndpoint=https://syncshot.file.core.windows.net/"


private val DCIM_BUCKET_ID: String by lazy {
    val bucketName = Environment.getExternalStorageDirectory().toString() + "/DCIM/Camera"
    bucketName.toLowerCase().hashCode().toString()
}

fun getRelevantPhotos(context: Context, startTime: Long, endTime: Long): List<Pair<Uri, Long>> {
    val cursor = context.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.MediaColumns.DATA, MediaStore.Images.Media.DATE_TAKEN),
            MediaStore.Images.Media.BUCKET_ID + "=?", arrayOf(DCIM_BUCKET_ID), MediaStore.Images.Media.DATE_TAKEN + " DESC")
    val dataCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
    val takenCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
    val photoList = mutableListOf<Pair<Uri, Long>>()
    while (cursor.moveToNext()) {
        val uri = Uri.parse(cursor.getString(dataCol))
        val takenAt = cursor.getLong(takenCol) / 1000
        if (takenAt in startTime..endTime) {
            photoList.add(Pair(uri, takenAt))
        } else if (takenAt < startTime) {
            break
        }
    }
    return photoList
}

fun downloadImage(imageData: ImageData): String {
    val storageAccount = CloudStorageAccount.parse(STORAGE_CONN_STR)
    val blobClient = storageAccount.createCloudBlobClient()
    val container = blobClient.getContainerReference(imageData.participantId)
    val blob = container.getBlockBlobReference(imageData.imageUuid)
    return blob.uri.toString()
}

fun uploadImages(application: MyApp, event: EventData, photos: List<Pair<Uri, Long>>, callback: (Int, Int)->Unit): ParticipationData {
    val serviceClient = application.serviceClient
    val userId = serviceClient.currentUser.userId

    val participationTable = serviceClient.getTable(ParticipationData::class.java)
    val participationData = participationTable.where()
            .field("userId").eq(userId).and()
            .field("eventId").eq(event.id!!)
            .execute().get().first()

    // Setup directory
    val participantId = participationData.id!!
    val storageAccount = CloudStorageAccount.parse(STORAGE_CONN_STR)
    val blobClient = storageAccount.createCloudBlobClient()
    val container = blobClient.getContainerReference(participantId)
    container.createIfNotExists()
    val permissions = BlobContainerPermissions()
    permissions.publicAccess = BlobContainerPublicAccessType.BLOB
    container.uploadPermissions(permissions)

    // Upload photos
    var count = 0
    for (photo in photos) {
        val uuid = UUID.randomUUID().toString()
        val uri = photo.first
        val takenTime = photo.second
        val cloudBlobRef = container.getBlockBlobReference(uuid)
        val file = File(uri.toString())
        file.inputStream().use { input ->
            cloudBlobRef.upload(input, file.length())
        }

        val data = ImageData(null, participantId, userId, event.id, takenTime, uuid, uri.toString())
        val imageTable = serviceClient.getTable(ImageData::class.java)
        imageTable.insert(data).get()

        callback(++count, photos.size)
    }

    return participationTable.update(ParticipationData(participantId, userId, event.id, count)).get()
}
