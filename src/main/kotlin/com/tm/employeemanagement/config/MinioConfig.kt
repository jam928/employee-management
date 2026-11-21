package com.tm.employeemanagement.config

import io.minio.MinioClient
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Connection settings for the MinIO server that stores employee images.
 *
 * [endpoint] is where this app reaches MinIO (e.g. http://minio:9000 inside Docker Compose), while
 * [publicUrl] is the base URL browsers use to load the images (e.g. http://localhost:9000).
 */
@ConfigurationProperties(prefix = "minio")
data class MinioProperties(
    val endpoint: String,
    val publicUrl: String,
    val accessKey: String,
    val secretKey: String,
    val bucket: String = "employee-images"
)

@Configuration
@EnableConfigurationProperties(MinioProperties::class)
class MinioConfig {
    @Bean
    fun minioClient(properties: MinioProperties): MinioClient = MinioClient.builder()
        .endpoint(properties.endpoint)
        .credentials(properties.accessKey, properties.secretKey)
        .build()
}
