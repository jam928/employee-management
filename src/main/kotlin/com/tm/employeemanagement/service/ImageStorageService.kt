package com.tm.employeemanagement.service

import com.tm.employeemanagement.config.MinioProperties
import io.minio.BucketExistsArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.RemoveObjectArgs
import io.minio.SetBucketPolicyArgs
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.*

/** Thrown when MinIO can't be reached or rejects a request. */
class ImageStorageException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/**
 * Stores employee images in a MinIO bucket and returns the public URL to each one.
 *
 * The bucket is created on first use with an anonymous read-only policy, so the frontend can load images
 * straight from MinIO with a plain <img> tag. Only JPEG, PNG, GIF and WebP files are accepted, checked by
 * their actual bytes rather than the client-supplied content type. SVG is rejected because it can carry scripts.
 */
@Service
class ImageStorageService(private val minioClient: MinioClient, private val properties: MinioProperties) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Volatile
    private var bucketReady = false

    /** Uploads [file] as an image for the employee with [employeeId] and returns its public URL. */
    fun uploadEmployeeImage(employeeId: Long, file: MultipartFile): String {
        require(!file.isEmpty) { "Image file is empty" }
        val bytes = file.bytes
        val format = ImageFormat.detect(bytes)
            ?: throw IllegalArgumentException("Unsupported image type. Upload a JPEG, PNG, GIF or WebP file")

        val objectName = "employees/$employeeId/${UUID.randomUUID()}.${format.extension}"
        try {
            ensureBucket()
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(properties.bucket)
                    .`object`(objectName)
                    .data(bytes, bytes.size)
                    .contentType(format.contentType)
                    .build()
            )
        } catch (ex: Exception) {
            throw ImageStorageException("Could not upload image to storage", ex)
        }
        return "${publicBaseUrl()}/$objectName"
    }

    /**
     * Deletes the image at [imageUrl] if it lives in this app's bucket. URLs pointing anywhere else
     * (for example an external avatar link) are left alone. Failures are logged, not thrown, so a
     * leftover file never blocks updating or deleting an employee.
     */
    fun deleteImage(imageUrl: String?) {
        val objectName = objectNameFor(imageUrl) ?: return
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder().bucket(properties.bucket).`object`(objectName).build()
            )
        } catch (ex: Exception) {
            log.warn("Could not delete image {} from storage", objectName, ex)
        }
    }

    /** Returns the object name for a URL in this app's bucket, or null if the URL points elsewhere. */
    fun objectNameFor(imageUrl: String?): String? {
        val prefix = "${publicBaseUrl()}/"
        if (imageUrl == null || !imageUrl.startsWith(prefix)) return null
        return imageUrl.removePrefix(prefix).takeIf { it.isNotBlank() }
    }

    private fun publicBaseUrl() = "${properties.publicUrl.trimEnd('/')}/${properties.bucket}"

    private fun ensureBucket() {
        if (bucketReady) return
        synchronized(this) {
            if (bucketReady) return
            val bucket = properties.bucket
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build())
            }
            minioClient.setBucketPolicy(
                SetBucketPolicyArgs.builder().bucket(bucket).config(publicReadPolicy(bucket)).build()
            )
            bucketReady = true
        }
    }

    private fun publicReadPolicy(bucket: String) = """
        {
          "Version": "2012-10-17",
          "Statement": [{
            "Effect": "Allow",
            "Principal": {"AWS": ["*"]},
            "Action": ["s3:GetObject"],
            "Resource": ["arn:aws:s3:::$bucket/*"]
          }]
        }
    """.trimIndent()
}

/** Image formats accepted for upload, recognized by their file signature ("magic bytes"). */
enum class ImageFormat(val contentType: String, val extension: String) {
    JPEG("image/jpeg", "jpg"),
    PNG("image/png", "png"),
    GIF("image/gif", "gif"),
    WEBP("image/webp", "webp");

    companion object {
        fun detect(bytes: ByteArray): ImageFormat? = when {
            bytes.startsWith(0xFF, 0xD8, 0xFF) -> JPEG
            bytes.startsWith(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A) -> PNG
            bytes.startsWith(0x47, 0x49, 0x46, 0x38) -> GIF // "GIF8"
            // "RIFF" <4-byte size> "WEBP"
            bytes.startsWith(0x52, 0x49, 0x46, 0x46) && bytes.size >= 12 &&
                String(bytes, 8, 4, Charsets.US_ASCII) == "WEBP" -> WEBP
            else -> null
        }

        private fun ByteArray.startsWith(vararg signature: Int): Boolean =
            size >= signature.size && signature.indices.all { this[it] == signature[it].toByte() }
    }
}
