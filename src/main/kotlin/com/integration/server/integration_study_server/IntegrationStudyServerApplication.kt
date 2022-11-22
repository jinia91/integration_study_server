package com.integration.server.integration_study_server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.integration.annotation.IntegrationComponentScan

@SpringBootApplication
@IntegrationComponentScan
class IntegrationStudyServerApplication

fun main(args: Array<String>) {
    runApplication<IntegrationStudyServerApplication>(*args)
}
