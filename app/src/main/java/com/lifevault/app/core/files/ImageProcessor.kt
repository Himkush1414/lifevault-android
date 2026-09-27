package com.lifevault.app.core.files

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Build
import androidx.core.graphics.scale
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

data class ProcessedImage(
    val fullBytes: ByteArray,
    val thumbBytes: ByteArray,
    val widthPx: Int,
    val heightPx: Int,
    val sha256Hex: String,
    val mimeType: String,
)

/**
 * Section 6.6: decode (downsampled, never at full resolution), EXIF-rotate then strip,
 * resize to the long-edge cap, encode WebP (falling back to JPEG), a separate thumbnail,
 * SHA-256 of the processed bytes, bounded to 2 concurrent pages (`Semaphore(2)`) so a
 * batch import can't blow the memory budget on a low-RAM device.
 */
@Singleton
class ImageProcessor @Inject constructor() {

    private val semaphore = Semaphore(permits = 2)

    /** [rawBytes] is the original, undecoded file (e.g. a scanner/gallery JPEG). */
    suspend fun process(rawBytes: ByteArray): ProcessedImage = semaphore.withPermit {
        val orientation = readExifOrientation(rawBytes)
        val bounds = decodeBounds(rawBytes)
        val sampleSize = ImageSizing.computeInSampleSize(bounds.first, bounds.second, ImageSizing.MAX_LONG_EDGE_PX)

        val decoded = decode(rawBytes, sampleSize)
        val rotated = applyOrientation(decoded, orientation)
        if (rotated !== decoded) decoded.recycle()

        val (targetWidth, targetHeight) = ImageSizing.targetDimensions(
            rotated.width,
            rotated.height,
            ImageSizing.MAX_LONG_EDGE_PX,
        )
        val resized = if (targetWidth != rotated.width || targetHeight != rotated.height) {
            rotated.scale(targetWidth, targetHeight).also { rotated.recycle() }
        } else {
            rotated
        }

        val fullBytes = encodeWithBudget(resized, ImageSizing.INITIAL_WEBP_QUALITY)

        val (thumbWidth, thumbHeight) = ImageSizing.targetDimensions(
            resized.width,
            resized.height,
            ImageSizing.THUMBNAIL_LONG_EDGE_PX,
        )
        val thumbBitmap = resized.scale(thumbWidth, thumbHeight)
        val thumbBytes = encode(thumbBitmap, ImageSizing.THUMBNAIL_WEBP_QUALITY)
        thumbBitmap.recycle()
        resized.recycle()

        ProcessedImage(
            fullBytes = fullBytes,
            thumbBytes = thumbBytes,
            widthPx = targetWidth,
            heightPx = targetHeight,
            sha256Hex = sha256Hex(fullBytes),
            mimeType = "image/webp",
        )
    }

    private fun decodeBounds(bytes: ByteArray): Pair<Int, Int> {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        return options.outWidth to options.outHeight
    }

    private fun decode(bytes: ByteArray, sampleSize: Int): Bitmap {
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        return requireNotNull(BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)) {
            "Unable to decode image"
        }
    }

    private fun readExifOrientation(bytes: ByteArray): Int =
        ExifInterface(ByteArrayInputStream(bytes)).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL,
        )

    /** Rotating into a fresh bitmap and re-encoding from scratch is what strips the EXIF
     * block (GPS, device info, ...) — the WebP/JPEG encoder never writes one back. */
    private fun applyOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
        val degrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        if (degrees == 0f) return bitmap
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun encodeWithBudget(bitmap: Bitmap, quality: Int): ByteArray {
        val firstPass = encode(bitmap, quality)
        if (!ImageSizing.shouldRetryAtLowerQuality(firstPass.size.toLong())) return firstPass
        val retried = encode(bitmap, ImageSizing.BUDGET_RETRY_WEBP_QUALITY)
        // Section 6.6: if still over budget, legibility wins — keep it anyway.
        return if (retried.size < firstPass.size) retried else firstPass
    }

    private fun encode(bitmap: Bitmap, quality: Int): ByteArray {
        val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Bitmap.CompressFormat.WEBP_LOSSY
        } else {
            @Suppress("DEPRECATION")
            Bitmap.CompressFormat.WEBP
        }
        val output = ByteArrayOutputStream()
        val ok = bitmap.compress(format, quality, output)
        if (!ok) {
            output.reset()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, output)
        }
        return output.toByteArray()
    }

    private fun sha256Hex(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
}
