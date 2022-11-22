package com.integration.server.integration_study_server.tcp

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.integration.ip.dsl.Tcp
import org.springframework.integration.ip.tcp.TcpInboundGateway
import org.springframework.integration.router.HeaderValueRouter
import org.springframework.integration.support.MessageBuilder
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.GenericMessage

@Configuration
class TcpServerConfig{

    @Bean
    fun inbound(gateway: TcpInboundGateway): IntegrationFlow {

        return IntegrationFlows.from(gateway)
            .filter<ByteArray>{
                String(it)
                true
            }

                // header -> default 정보  'replyChannel(response())'

            .transform(Message::class.java) { message ->
                val reqMsg = String(message.payload as ByteArray)
                println("____active transformer $reqMsg")
                val substringHeader = reqMsg.substring(0, 4)
                println(substringHeader)
                val map = mutableMapOf<String, Any>()
                map.putAll(message.headers)

//                MessageBuilder.fromMessage(message).copyHeaders(message.headers).setHeader("type", "hello").build()
                when (substringHeader) {
                    "0001" -> map["type"] = "hello"
                    else -> map["type"] = "bye"
                }
                GenericMessage(message.payload, MessageHeaders(map))
            }
            .route(
                HeaderValueRouter("type").apply {
                    setChannelMapping("hello", "helloChannel")
                    setChannelMapping("bye", "byeChannel")
                }
            )
//            .channel(service())
            .get()
    }

//    요청 1 : g.w -> integration flows -> reply
//    g.w -> deserialize -> 포메팅 -> (라우팅) -> service1, service2 -> reply -> g.w ->


    @Bean
    fun gateway(): TcpInboundGateway {
        val nioServer = Tcp.netServer(9191)
        val factory = nioServer.get()
        val inboundGateway = Tcp.inboundGateway(factory)
            .apply {
//                requestChannel(service())
                replyChannel(response())
            }
        return inboundGateway.get()
    }

    @Bean
    fun service() = DirectChannel()

    @Bean
    fun response() = DirectChannel()
}