package com.integration.server.integration_study_server.tcp

import org.springframework.integration.annotation.MessageEndpoint
import org.springframework.integration.annotation.MessagingGateway
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.integration.ip.tcp.TcpInboundGateway
import org.springframework.messaging.Message
import org.springframework.messaging.support.GenericMessage
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller

@MessageEndpoint
@Controller
class TcpHandler(
//    private val sevice : Service
) {

    @ServiceActivator(
        inputChannel = "helloChannel",
        outputChannel = "response",
    )
    fun handle(input : String) : String{
        println("_____active hello hander ${input}______")
        return "return $input hello"
        }

    @ServiceActivator(
            inputChannel = "byeChannel",
            outputChannel = "response"
        )
    fun handle2(input : String) : String{
        println("_____active hello hander ${input}______")
        return "return $input bye"
    }
}



//    @ServiceActivator(
//        inputChannel = "byeInputChannel",
//    )
//    fun handleBye(input : String) {
//        println("_____active bye hander______")
//        return "return " + input
//    }




//@MessagingGateway(name = "responseGateway", defaultRequestChannel = "response", defaultReplyChannel = "outboundChannel")
//interface ResponseGateway