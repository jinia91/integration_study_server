package com.integration.server.integration_study_server.tcp

import java.io.Serializable

data class Mock(
    val body : String = "default"
) : Serializable