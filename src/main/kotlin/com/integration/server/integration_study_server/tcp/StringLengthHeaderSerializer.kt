package com.integration.server.integration_study_server.tcp

import org.apache.commons.lang3.ArrayUtils
import org.apache.commons.logging.LogFactory
import org.springframework.integration.ip.tcp.serializer.AbstractByteArraySerializer
import org.springframework.integration.ip.tcp.serializer.SoftEndOfStreamException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer


class StringLengthHeaderSerializer @JvmOverloads constructor(headerSize: Int = HEADER_SIZE_INT) :
    AbstractByteArraySerializer() {
    private val headerSize: Int
    private val logger = LogFactory.getLog(this.javaClass)

    init {
        require(headerSize == HEADER_SIZE_INT) { "Illegal header size:$headerSize" }
        this.headerSize = headerSize
    }


    @Throws(IOException::class)
    override fun deserialize(inputStream: InputStream): ByteArray {
        val messageLengthPart = readHeader(inputStream)
        val messageLength = String(messageLengthPart).toInt() - HEADER_SIZE_INT
        if (this.logger.isDebugEnabled) {
            this.logger.debug("Message length is $messageLength")
        }
        var messagePart: ByteArray? = null
        return try {
            if (messageLength > maxMessageSize) {
                throw IOException(
                    "Message length " + messageLength + " exceeds max message length: " + maxMessageSize
                )
            }
            messagePart = ByteArray(messageLength)
            read(inputStream, messagePart, false)
            messagePart
        } catch (e: IOException) {
            publishEvent(e, messagePart, -1)
            throw e
        } catch (e: RuntimeException) {
            publishEvent(e, messagePart, -1)
            throw e
        }
    }


    @Throws(IOException::class)
    override fun serialize(bytes: ByteArray, outputStream: OutputStream) {
        writeHeader(outputStream, bytes.size)
        outputStream.write(bytes)
    }


    @Throws(IOException::class)
    protected fun read(inputStream: InputStream, buffer: ByteArray, header: Boolean): Int {
        var lengthRead = 0
        val needed = buffer.size
        while (lengthRead < needed) {
            var len: Int
            len = inputStream.read(buffer, lengthRead, needed - lengthRead)
            if (len < 0  &&  lengthRead == 0 && header) {
                return len
            }
            if (len < 0) {
                throw IOException("Stream closed after $lengthRead of $needed")
            }
            lengthRead += len
            if (this.logger.isDebugEnabled) {
                this.logger.debug("Read $len bytes, buffer is now at $lengthRead of $needed")
            }
        }
        return 0
    }


    @Throws(IOException::class)
    protected fun writeHeader(outputStream: OutputStream, length: Int) {
        val lengthPart = ByteBuffer.allocate(headerSize)
        when (headerSize) {
            HEADER_SIZE_INT -> lengthPart.putInt(length)
            else -> throw IllegalArgumentException("Bad header size:" + headerSize)
        }
        outputStream.write(lengthPart.array())
    }


    @Throws(IOException::class)
    protected fun readHeader(inputStream: InputStream): ByteArray {
        val lengthPart = ByteArray(headerSize)
        return try {
            val status = read(inputStream, lengthPart, true)
            if (status < 0) {
                throw SoftEndOfStreamException("Stream closed between payloads")
            }
            val messageLength: Int
            when (headerSize) {
                HEADER_SIZE_INT -> {
                    messageLength = String(lengthPart).toInt()
                    require(messageLength >= 0) { "Length header:$messageLength is negative" }
                }

                else -> throw IllegalArgumentException("Bad header size:" + headerSize)
            }
            lengthPart
        } catch (e: SoftEndOfStreamException) {
            throw e
        } catch (e: IOException) {
            publishEvent(e, lengthPart, -1)
            throw e
        } catch (e: RuntimeException) {
            publishEvent(e, lengthPart, -1)
            throw e
        }
    }

    companion object {
        /**
         * Default length-header field, allows for data up to 2**31-1 bytes.
         */
        const val HEADER_SIZE_INT = 4 // default
    }
}
