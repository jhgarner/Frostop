package com.example.remotedesktop

import android.media.MediaCodec
import android.media.MediaCodecList
import android.media.MediaFormat
import android.os.Build
import android.view.SurfaceView
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import kotlin.math.roundToInt

data class HevcReceiver(
    val scale: Float,
    val server: StableServer.Streaming,
    val view: SurfaceView
) {
    private val codec: MediaCodec

    init {
        val byteArray = server.get()
        val buffer = extractHevcParamSets(byteArray)

        val format = MediaFormat().apply {
            setString(MediaFormat.KEY_MIME, MediaFormat.MIMETYPE_VIDEO_HEVC)
            setString(MediaFormat.KEY_MIME, MediaFormat.MIMETYPE_VIDEO_HEVC)
            // Android can't find the codec if we use a nonstandard width and height
            setInteger(MediaFormat.KEY_WIDTH, 1080)
            setInteger(MediaFormat.KEY_HEIGHT, 1920)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                setInteger(MediaFormat.KEY_LOW_LATENCY, 1)
            }
            setByteBuffer("csd-0", buffer)
        }
        codec = MediaCodecList(MediaCodecList.ALL_CODECS)
            .findDecoderForFormat(format)
            .let(MediaCodec::createByCodecName)

        // But once it has found the codec, these values work fine...
        val width = (view.width / scale).roundToInt()
        val height = (view.height / scale).roundToInt()
        format.setInteger(MediaFormat.KEY_WIDTH, width)
        format.setInteger(MediaFormat.KEY_HEIGHT, height)
        codec.configure(format, view.holder.surface, null, 0)
        codec.start()
        // Draw once to get things started
        drawArray(codec, byteArray)
    }

    fun runReceiver(): Result<Unit> {
        return runCatching {
            while (view.holder.surface.isValid) {
                val byteArray = server.get()
                drawArray(codec, byteArray)
            }
        }
    }

    private fun extractHevcParamSets(bytes: ByteArray): ByteBuffer? {
        var nalBeginPos = 0
        var nalEndPos: Int
        var nalUnitType = -1
        var nlz = 0
        val output = ByteArrayOutputStream()
        var pos = 0
        while (pos < bytes.size) {
            if (2 <= nlz && bytes[pos].toInt() == 0x01) {
                nalEndPos = pos - nlz
                if ((nalUnitType == 32) || (nalUnitType == 33) || (nalUnitType == 34)) {
                    // extract VPS(32), SPS(33), PPS(34)
                    output.write(byteArrayOf(0, 0, 0, 1))
                    output.write(bytes, nalBeginPos, nalEndPos - nalBeginPos)

                }
                nalBeginPos = ++pos
                nalUnitType = bytes[pos].toInt() shr 1 and 0x2f
                if (nalUnitType in 0..31) {
                    break // VCL NAL; no more VPS/SPS/PPS
                }
            }
            nlz = if (bytes[pos].toInt() != 0x00) 0 else nlz + 1
            pos++

        }
        return ByteBuffer.wrap(output.toByteArray())
    }

    private fun drawArray(codec: MediaCodec, byteArray: ByteArray) {
        codec.apply {
            val index = dequeueInputBuffer(-1)
            getInputBuffer(index)!!.put(byteArray)
            queueInputBuffer(index, 0, byteArray.size, 0, 0)

            // This is dumb, but it removes the multiframe latency so whatever
            runRetrying {
                val output = dequeueOutputBuffer(MediaCodec.BufferInfo(), -1)
                releaseOutputBuffer(output, true)
            }
        }
    }

    private fun <T> runRetrying(f: () -> T): Result<T> {
        var result: Result<T>? = null
        var retry = 0
        while (result?.isSuccess != true && retry < 5) {
            result = runCatching(f)
            retry += 1
        }
        return result!!
    }
}