package com.integration.server.integration_study_server.tcp

import java.io.Serializable

data class Mock(
    val value1 : String = "defualt",
    val value2 : String = "default"
) : Serializable