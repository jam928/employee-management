package com.tm.employeemanagement.controller

import com.tm.employeemanagement.dto.EmployeeResponse
import com.tm.employeemanagement.support.AbstractIntegrationTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.Base64
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class EmployeeImageIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    private val httpClient = HttpClient.newHttpClient()

    // A valid 1x1 transparent PNG
    private val pngBytes = Base64.getDecoder().decode(
        "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII="
    )

    private var employeeId: Long = 0

    @BeforeEach
    fun setup() {
        val result = mockMvc.perform(post("/employee/add")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"name": "Image Tester", "email": "image@tm.com", "jobTitle": "QA", "phone": "5555555555"}""")
        ).andReturn()
        employeeId = objectMapper.readValue<EmployeeResponse>(result.response.contentAsString).id!!
    }

    private fun uploadImage(id: Long, file: MockMultipartFile) =
        mockMvc.perform(multipart(HttpMethod.PUT, "/employee/$id/image").file(file))

    private fun fetch(url: String): HttpResponse<ByteArray> =
        httpClient.send(HttpRequest.newBuilder(URI.create(url)).build(), HttpResponse.BodyHandlers.ofByteArray())

    @Test
    fun testUploadImageStoresItInMinioAndUpdatesEmployee() {
        val result = uploadImage(employeeId, MockMultipartFile("file", "avatar.png", "image/png", pngBytes))
            .andExpect(status().isOk)
            .andReturn()
        val employee = objectMapper.readValue<EmployeeResponse>(result.response.contentAsString)

        val imageUrl = assertNotNull(employee.imageUrl)
        assertTrue(imageUrl.startsWith("${minio.s3URL}/employee-images/employees/$employeeId/"))
        assertTrue(imageUrl.endsWith(".png"))

        // The bucket allows anonymous reads, so a browser can load the image directly
        val image = fetch(imageUrl)
        assertEquals(200, image.statusCode())
        assertEquals("image/png", image.headers().firstValue("Content-Type").orElse(null))
        assertContentEquals(pngBytes, image.body())
    }

    @Test
    fun testReplacingImageDeletesThePreviousOne() {
        val first = objectMapper.readValue<EmployeeResponse>(
            uploadImage(employeeId, MockMultipartFile("file", "a.png", "image/png", pngBytes)).andReturn().response.contentAsString
        ).imageUrl!!
        val second = objectMapper.readValue<EmployeeResponse>(
            uploadImage(employeeId, MockMultipartFile("file", "b.png", "image/png", pngBytes)).andReturn().response.contentAsString
        ).imageUrl!!

        assertNotEquals(first, second)
        assertEquals(200, fetch(second).statusCode())
        assertEquals(404, fetch(first).statusCode())
    }

    @Test
    fun testRemoveImage() {
        val imageUrl = objectMapper.readValue<EmployeeResponse>(
            uploadImage(employeeId, MockMultipartFile("file", "a.png", "image/png", pngBytes)).andReturn().response.contentAsString
        ).imageUrl!!

        val result = mockMvc.perform(delete("/employee/$employeeId/image")).andExpect(status().isOk).andReturn()

        assertNull(objectMapper.readValue<EmployeeResponse>(result.response.contentAsString).imageUrl)
        assertEquals(404, fetch(imageUrl).statusCode())
    }

    @Test
    fun testDeletingEmployeeDeletesImage() {
        val imageUrl = objectMapper.readValue<EmployeeResponse>(
            uploadImage(employeeId, MockMultipartFile("file", "a.png", "image/png", pngBytes)).andReturn().response.contentAsString
        ).imageUrl!!

        mockMvc.perform(delete("/employee/delete/$employeeId")).andExpect(status().isOk)

        assertEquals(404, fetch(imageUrl).statusCode())
    }

    @Test
    fun testRejectsFilesThatAreNotImages() {
        // Claims to be a PNG, but the bytes say otherwise
        val result = uploadImage(employeeId, MockMultipartFile("file", "evil.png", "image/png", "<svg onload=alert(1)>".toByteArray()))
            .andExpect(status().isBadRequest)
            .andReturn()
        assertEquals("Unsupported image type. Upload a JPEG, PNG, GIF or WebP file", result.response.contentAsString)
    }

    @Test
    fun testRejectsEmptyFile() {
        uploadImage(employeeId, MockMultipartFile("file", "empty.png", "image/png", ByteArray(0)))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun testRejectsRequestWithoutFile() {
        mockMvc.perform(multipart(HttpMethod.PUT, "/employee/$employeeId/image"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun testUploadImageForUnknownEmployee() {
        uploadImage(999_999, MockMultipartFile("file", "a.png", "image/png", pngBytes))
            .andExpect(status().isNotFound)
    }
}
