package com.example.remotedesktop

import com.example.remotedesktop.App.Companion.host
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.DataInputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.ByteBuffer
import kotlin.math.roundToInt

@Serializable
data class ServerSettings(val fps: Int, val bitrate_mb: Float, val scale: Float)

data object StableServer {

    private var socket = Socket()

    @OptIn(ExperimentalSerializationApi::class)
    fun query(): List<SessionInfo> {
        socket = Socket()
        socket.connect(InetSocketAddress(host, 11101))
        Json.encodeToStream(Params.Query as Params, socket.getOutputStream())
        return Json.decodeFromStream(socket.getInputStream())
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun stop(sessionInfo: SessionInfo): Unit? {
        socket = Socket()
        socket.connect(InetSocketAddress(host, 11101))
        Json.encodeToStream(Params.Stop(sessionInfo) as Params, socket.getOutputStream())
        return Json.decodeFromStream<Unit?>(socket.getInputStream())
    }

    class Streaming(
        private val socket: Socket,
        private val scale: Float,
        val width: Int,
        val height: Int
    ) {
        fun get(): ByteArray {
            val inputStream = DataInputStream(socket.getInputStream())
            val size = inputStream.readInt()
            val output = ByteArray(size)
            inputStream.readFully(output)
            return output
        }

        fun sendRelLoc(xRaw: Int, yRaw: Int) {
            sendCursor(xRaw, yRaw, true)
        }

        fun sendLoc(xRaw: Int, yRaw: Int) {
            sendCursor(xRaw, yRaw, false)
        }

        private fun sendCursor(xRaw: Int, yRaw: Int, relative: Boolean) {
            val x = (xRaw / scale).roundToInt()
            val y = (yRaw / scale).roundToInt()
            sendInput(ControlInput.Cursor(x, y, relative))
        }

        fun sendClick(button: Int) {
            sendButton(button, PRESS)
        }

        fun sendRelease(button: Int) {
            sendButton(button, RELEASE)
        }

        private fun sendButton(button: Int, type: Int) {
            sendInput(ControlInput.MouseButton(button, type))
        }

        fun sendKeyPress(key: Int) {
            sendKey(key, KEY_PRESS)
        }

        fun sendKeyRelease(key: Int) {
            sendKey(key, KEY_RELEASE)
        }

        fun sendKey(key: Int, type: Int) {
            sendInput(ControlInput.Key(key, type))
        }

        @OptIn(ExperimentalSerializationApi::class)
        private fun sendInput(input: ControlInput) {
            Json.encodeToStream(input, socket.getOutputStream())
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
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun connect(
        settings: ServerSettings,
        widthRaw: Int,
        heightRaw: Int,
        sessionInfo: SessionInfo
    ): Streaming {
        val width = (widthRaw / settings.scale).roundToInt()
        val height = (heightRaw / settings.scale).roundToInt()
        socket = Socket()
        socket.tcpNoDelay = true
        socket.connect(InetSocketAddress(host, 11101))
        val bitrate = (settings.bitrate_mb * 1024 * 1024).roundToInt()
        val videoParams = VideoParams(bitrate, settings.fps, width, height)
        Json.encodeToStream(
            Params.Connect(videoParams, sessionInfo) as Params,
            socket.getOutputStream()
        )
        return Streaming(socket, settings.scale, width, height)
    }


    fun Int.asBytes(): ByteArray {
        val buffer = ByteBuffer.allocate(Int.SIZE_BYTES)
        buffer.putInt(this)
        return buffer.array()
    }

    private const val LOC: Byte = 0

    private const val CLICK: Byte = 1
    private const val LEFT: Byte = 1 // XCB button 1
    private const val RIGHT: Byte = 2 // XCB button 2
    private const val MIDDLE: Byte = 3 // XCB button 3
    const val SCROLL_UP: Byte = 4 // == XCB_BUTTON_4
    const val SCROLL_DOWN: Byte = 5 // == XCB_BUTTON_5
    const val SCROLL_RIGHT: Byte = 6 // == XCB_BUTTON_5
    const val SCROLL_LEFT: Byte = 7
    private const val PRESS: Int = 4 // == XCB_BUTTON_PRESS
    private const val RELEASE: Int = 5 // == XCB_BUTTON_RELEASE

    private const val KEY: Byte = 2
    private const val KEY_PRESS: Int = 2
    private const val KEY_RELEASE: Int = 3
}
