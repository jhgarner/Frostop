package com.example.remotedesktop

import kotlinx.serialization.Serializable
import java.io.DataInputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.ByteBuffer
import kotlin.math.roundToInt

@Serializable
data class ServerSettings(val host: String, val fps: Int, val bitrate_mb: Float, val scale: Float)

class StableServer(private val settings: ServerSettings, width: Int, height: Int) {
    private val width = (width / settings.scale).roundToInt().asBytes()
    private val height = (height / settings.scale).roundToInt().asBytes()

    private var socket = Socket()

    fun connect() {
        socket = Socket()
        println("Connecting")
        socket.tcpNoDelay = true
        socket.connect(InetSocketAddress(settings.host, 11101))
        val output = socket.getOutputStream()
        val bitrate = (settings.bitrate_mb * 1024 * 1024).roundToInt()
        output.write(bitrate.asBytes() + settings.fps.toByte() + width + height)
        println("Done Connecting")
    }

    fun get(): ByteArray {
        val inputStream = DataInputStream(socket.getInputStream())
        val size = inputStream.readInt()
        val output = ByteArray(size)
        inputStream.readFully(output)
        return output
    }

    private fun send(prefix: Byte, data: ByteArray) {
        if (!socket.isClosed && socket.isConnected) {
            val output = socket.getOutputStream()
            output.write(byteArrayOf(prefix) + data)
            output.flush()
        }
    }

    fun sendRelLoc(x: Int, y: Int) {
        val realX = (x / settings.scale).roundToInt().asBytes()
        val realY = (y / settings.scale).roundToInt().asBytes()
        send(LOC, byteArrayOf(1.toByte()) + realX + realY)
    }

    fun sendLoc(x: Int, y: Int) {
        val realX = (x / settings.scale).roundToInt().asBytes()
        val realY = (y / settings.scale).roundToInt().asBytes()
        send(LOC, byteArrayOf(0.toByte()) + realX + realY)
    }

    fun sendClick(button: Byte) {
        send(CLICK, byteArrayOf(button, PRESS))
    }

    fun sendRelease(button: Byte) {
        send(CLICK, byteArrayOf(button, RELEASE))
    }

    fun sendKeyPress(key: Byte) {
        send(KEY, byteArrayOf(key, KEY_PRESS))
    }

    fun sendKeyRelease(key: Byte) {
        send(KEY, byteArrayOf(key, KEY_RELEASE))
    }

    fun disconnect() {
        println("Disconnecting")
        try {
            socket.close()
            println("Disconnected")
        } catch (ex: RuntimeException) {
            println(ex);
            throw ex;
        }
    }

    fun Int.asBytes(): ByteArray {
        val buffer = ByteBuffer.allocate(Int.SIZE_BYTES)
        buffer.putInt(this)
        return buffer.array()
    }

    companion object {
        private const val LOC: Byte = 0

        private const val CLICK: Byte = 1
        private const val LEFT: Byte = 1 // XCB button 1
        private const val RIGHT: Byte = 2 // XCB button 2
        private const val MIDDLE: Byte = 3 // XCB button 3
        const val SCROLL_UP: Byte = 4 // == XCB_BUTTON_4
        const val SCROLL_DOWN: Byte = 5 // == XCB_BUTTON_5
        const val SCROLL_RIGHT: Byte = 6 // == XCB_BUTTON_5
        const val SCROLL_LEFT: Byte = 7
        private const val PRESS: Byte = 4 // == XCB_BUTTON_PRESS
        private const val RELEASE: Byte = 5 // == XCB_BUTTON_RELEASE

        private const val KEY: Byte = 2
        private const val KEY_PRESS: Byte = 2
        private const val KEY_RELEASE: Byte = 3
    }
}
