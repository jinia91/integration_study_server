package com.integration.server.integration_study_server.tcp

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.integration.ip.dsl.Tcp
import org.springframework.integration.ip.tcp.TcpInboundGateway
import org.springframework.integration.ip.tcp.TcpOutboundGateway
import org.springframework.integration.router.HeaderValueRouter
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.GenericMessage

@Configuration
class TcpServerConfig {

    @Bean
    fun inbound(gateway: TcpInboundGateway): IntegrationFlow {
        return IntegrationFlows.from(gateway)
            .transform<ByteArray, ByteArray> { message ->
               message
            }.route(
                HeaderValueRouter("MESSAGE_TYPE").also {
                    it.setChannelMapping("0001", "hello")
                    it.setChannelMapping("0002", "bye")
                }
            )
            .get()
    }

    @Bean
    fun gateway(): TcpInboundGateway {
        val nioServer = Tcp.nioServer(9191)
        val factory = nioServer.get()
            .apply {
                deserializer = CustomHeaderDeSerializer()
                serializer = CustomHeaderDeSerializer()
            }
        val inboundGateway = Tcp.inboundGateway(factory)
        return inboundGateway.get()
    }

    @Bean
    fun hello() =  DirectChannel()

    @Bean
    fun output() =  DirectChannel()
}