package com.integration.server.integration_study_server.tcp

import org.springframework.integration.annotation.MessageEndpoint
import org.springframework.integration.annotation.ServiceActivator

@MessageEndpoint
class TcpHandler {

    @ServiceActivator(inputChannel = "hello")
    fun handleHello(input : ByteArray) : String{
        return "return " + String(input)
    }

    @ServiceActivator(inputChannel = "bye")
    fun handleBye(input : ByteArray) : String{
        return "return " + String(input)
    }

}