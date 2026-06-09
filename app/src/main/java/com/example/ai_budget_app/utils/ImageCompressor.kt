package com.example.ai_budget_app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

object ImageCompressor {
    private const val MAX_SIZE_BYTES = 500 * 1024 // 500KB
    private const val TAG = "ImageCompressor"

    suspend fun compressImage(context: Context, uri: Uri): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap == null) return@withContext null

            return@withContext compressBitmap(bitmap)
        } catch (e: Exception) {
            Log.e(TAG, "Error compressing image from URI", e)
            null
        }
    }

    suspend fun compressBitmap(bitmap: Bitmap): ByteArray = withContext(Dispatchers.IO) {
        var quality = 100
        var stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        var imageByteArray = stream.toByteArray()

        // Loop to reduce quality until it fits the size
        while (imageByteArray.size > MAX_SIZE_BYTES && quality > 10) {
            stream.reset() // Reset the stream to write again
            quality -= 10
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            imageByteArray = stream.toByteArray()
        }

        // If quality reduction is not enough, scale down the image
        if (imageByteArray.size > MAX_SIZE_BYTES) {
            val scaledBitmap = Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * 0.8).toInt(),
                (bitmap.height * 0.8).toInt(),
                true
            )
            return@withContext compressBitmap(scaledBitmap)
        }

        Log.d(TAG, "Final compressed size: ${imageByteArray.size / 1024} KB")
        return@withContext imageByteArray
    }
}
