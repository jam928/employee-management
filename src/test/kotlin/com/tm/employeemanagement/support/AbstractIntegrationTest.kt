package com.tm.employeemanagement.support

import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MinIOContainer
import org.testcontainers.mysql.MySQLContainer
import org.testcontainers.utility.DockerImageName

/**
 * Base class for integration tests that need a real MySQL database and MinIO image storage.
 *
 * A single container of each is started once per JVM and shared by every subclass,
 * so Spring's cached application contexts keep pointing at live services.
 * @ServiceConnection points Spring's datasource at MySQL, @DynamicPropertySource points the
 * minio.* properties at MinIO, and Testcontainers' Ryuk sidecar removes both containers when the JVM exits.
 */
abstract class AbstractIntegrationTest {

    companion object {
        @JvmField
        @ServiceConnection
        val mysql = MySQLContainer(DockerImageName.parse("mysql:8.4")).apply {
            withDatabaseName("employee-management")
            start()
        }

        // Same image as docker-compose.yml; it's a community build of minio/minio
        @JvmField
        val minio = MinIOContainer(
            DockerImageName.parse("pgsty/minio:RELEASE.2026-08-04T00-00-00Z").asCompatibleSubstituteFor("minio/minio")
        ).apply {
            start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun minioProperties(registry: DynamicPropertyRegistry) {
            registry.add("minio.endpoint", minio::getS3URL)
            registry.add("minio.public-url", minio::getS3URL)
            registry.add("minio.access-key", minio::getUserName)
            registry.add("minio.secret-key", minio::getPassword)
        }
    }
}
