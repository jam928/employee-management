package com.tm.employeemanagement.service

import com.tm.employeemanagement.config.MinioProperties
import io.minio.MinioClient
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ImageStorageServiceTest {

    private val minioClient = mockk<MinioClient>(relaxed = true)
    private val service = ImageStorageService(
        minioClient,
        MinioProperties(
            endpoint = "http://minio:9000",
            publicUrl = "http://localhost:9000/",
            accessKey = "key",
            secretKey = "secret",
            bucket = "employee-images"
        )
    )

    @Test
    fun testDetectsSupportedFormatsByTheirBytes() {
        assertEquals(ImageFormat.JPEG, ImageFormat.detect(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte())))
        assertEquals(ImageFormat.PNG, ImageFormat.detect(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)))
        assertEquals(ImageFormat.GIF, ImageFormat.detect("GIF89a".toByteArray()))
        assertEquals(ImageFormat.WEBP, ImageFormat.detect("RIFF\u0000\u0000\u0000\u0000WEBPVP8 ".toByteArray()))
    }

    @Test
    fun testRejectsUnsupportedFormats() {
        assertNull(ImageFormat.detect("<svg xmlns=\"http://www.w3.org/2000/svg\"/>".toByteArray()))
        assertNull(ImageFormat.detect("RIFF\u0000\u0000\u0000\u0000WAVE".toByteArray()))
        assertNull(ImageFormat.detect(byteArrayOf(0xFF.toByte())))
    }

    @Test
    fun testUploadRejectsNonImageWithoutCallingMinio() {
        val file = MockMultipartFile("file", "notes.png", "image/png", "hello".toByteArray())
        assertFailsWith<IllegalArgumentException> { service.uploadEmployeeImage(1, file) }
        verify(exactly = 0) { minioClient.putObject(any()) }
    }

    @Test
    fun testObjectNameForOnlyMatchesThisBucket() {
        assertEquals("employees/1/a.png", service.objectNameFor("http://localhost:9000/employee-images/employees/1/a.png"))
        assertNull(service.objectNameFor("https://bootdey.com/img/Content/avatar/avatar1.png"))
        assertNull(service.objectNameFor("http://localhost:9000/other-bucket/employees/1/a.png"))
        assertNull(service.objectNameFor("http://localhost:9000/employee-images/"))
        assertNull(service.objectNameFor(null))
    }

    @Test
    fun testDeleteImageIgnoresExternalUrls() {
        service.deleteImage("https://bootdey.com/img/Content/avatar/avatar1.png")
        verify(exactly = 0) { minioClient.removeObject(any()) }
    }
}
