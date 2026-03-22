package com.trackgod.app.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ImageCropper {
    fun cropToSquare(context: Context, sourceUri: Uri): Uri? {
        val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return null
        val original = BitmapFactory.decodeStream(inputStream) ?: run {
            inputStream.close()
            return null
        }
        inputStream.close()

        val size = minOf(original.width, original.height)
        val x = (original.width - size) / 2
        val y = (original.height - size) / 2
        val cropped = Bitmap.createBitmap(original, x, y, size, size)

        val scaled = Bitmap.createScaledBitmap(cropped, 512, 512, true)

        val file = File(context.filesDir, "avatar_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            scaled.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }

        if (cropped !== original) cropped.recycle()
        if (scaled !== cropped) scaled.recycle()
        original.recycle()

        return Uri.fromFile(file)
    }
}
