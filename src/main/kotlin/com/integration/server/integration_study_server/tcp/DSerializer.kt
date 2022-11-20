package com.integration.server.integration_study_server.tcp

import org.springframework.integration.ip.tcp.serializer.AbstractByteArraySerializer
import org.springframework.integration.ip.tcp.serializer.SoftEndOfStreamException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer

class CustomHeaderDeSerializer : AbstractStrLenHeaderSerializer(){
    override val lengthPartSize: Int
        get() = 4
    override val validationPart: String
        get() = ""

}

abstract class AbstractStrLenHeaderSerializer : AbstractByteArraySerializer() {

    @Throws(IOException::class)
    override fun deserialize(inputStream: InputStream): ByteArray {
        val messageLength = readHeader(inputStream)

        var messagePart: ByteArray? = null
        return try {
            /* max message size check.
            if (messageLength > maxMessageSize) {
                throw IOException(
                    "Message length " + messageLength + " exceeds max message length: " + maxMessageSize)
            } */
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
    private fun writeHeader(outputStream: OutputStream, length: Int) {
        val headerPart = ByteBuffer.allocate(lengthPartSize + validationPart.length)
        val lengthPart = length + validationPart.length
        headerPart.put((format(lengthPart) + validationPart).toByteArray())
        outputStream.write(headerPart.array())
    }

    @Throws(IOException::class)
    private fun read(inputStream: InputStream, buffer: ByteArray, header: Boolean): Int {
        var lengthRead = 0
        val needed = buffer.size
        while (lengthRead < needed) {
            val len = inputStream.read(buffer, lengthRead, needed - lengthRead)
            if (len < 0 && header && lengthRead == 0) {
                return len
            }
            if (len < 0) {
                throw IOException("Stream closed after $lengthRead of $needed")
            }
            lengthRead += len
            if (logger.isDebugEnabled) {
                logger.debug("Read $len bytes, buffer is now at $lengthRead of $needed")
            }
        }
        return 0
    }

    @Throws(IOException::class)
    private fun readHeader(inputStream: InputStream): Int {
        val headerPartSize = lengthPartSize + validationPart.length
        val headerPart = ByteArray(headerPartSize)
        return try {
            val status = read(inputStream, headerPart, true)
            if (status < 0) {
                throw SoftEndOfStreamException("Stream closed between payloads")
            }
            val messageLength = String(headerPart.sliceArray(0 until lengthPartSize)).toInt()
            if (messageLength < 0) {
                throw IllegalArgumentException("Length header:$messageLength is negative")
            }

            val validationPartInput = String(headerPart.sliceArray(lengthPartSize until headerPartSize))
            if (validationPartInput != validationPart) {
                throw IllegalArgumentException("Check String: $validationPartInput is not correct")
            }
            messageLength - validationPart.length
        } catch (e: SoftEndOfStreamException) {
            throw e
        } catch (e: IOException) {
            publishEvent(e, headerPart, -1)
            throw e
        } catch (e: RuntimeException) {
            publishEvent(e, headerPart, -1)
            throw e
        }
    }

    abstract val lengthPartSize: Int

    abstract val validationPart: String

    protected open fun format(lengthPart: Int): String = "%0${lengthPartSize}d".format(lengthPart)
}
