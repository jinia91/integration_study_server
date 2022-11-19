package com.integration.server.integration_study_server.tcp

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.integration.dsl.Transformers
import org.springframework.integration.ip.dsl.Tcp
import org.springframework.integration.ip.tcp.TcpInboundGateway
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel

@Configuration
class TcpClientConfigV1 {

    @Bean
    fun outBound(): IntegrationFlow {
        val nioClient = Tcp.nioServer(9191)
        val factory = nioClient.get()
        val outboundGateway = Tcp.inboundGateway(factory)
        val gateway = outboundGateway.get()
        return IntegrationFlows.from(gateway)
            .transform<ByteArray, ByteArray>{
                ("return ${String(it)}").toByteArray()
            }
            .get()
    }

    @Bean
    fun output(): MessageChannel{
        return DirectChannel()
    }
}