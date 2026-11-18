package com.tm.employeemanagement.support

import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.mysql.MySQLContainer
import org.testcontainers.utility.DockerImageName

/**
 * Base class for integration tests that need a real MySQL database.
 *
 * A single container is started once per JVM and shared by every subclass,
 * so Spring's cached application contexts keep pointing at a live database.
 * @ServiceConnection points Spring's datasource at it, and Testcontainers'
 * Ryuk sidecar removes the container when the JVM exits.
 */
abstract class AbstractIntegrationTest {

    companion object {
        @JvmField
        @ServiceConnection
        val mysql = MySQLContainer(DockerImageName.parse("mysql:8.4")).apply {
            withDatabaseName("employee-management")
            start()
        }
    }
}
