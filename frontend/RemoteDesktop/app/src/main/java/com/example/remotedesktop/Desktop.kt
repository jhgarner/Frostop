package com.example.remotedesktop

import android.media.MediaCodec
import android.media.MediaCodecList
import android.media.MediaFormat
import android.os.Build
import android.util.Log
import android.view.SurfaceView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.example.remotedesktop.LifecycleObserver.observeAsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import kotlin.math.roundToInt

class Desktop(
    private val settings: ServerSettings,
    private val sessionInfo: SessionInfo,
    private val channel: Channel<PointerOrKeyEvent>,
    private val panels: List<Panel>
) {
    @Composable
    fun Create(modes: Modes) {
        var view: SurfaceView? by remember { mutableStateOf(null, referentialEqualityPolicy()) }
        val lifeState by LocalLifecycleOwner.current.lifecycle.observeAsState()
        val stylusMenu = remember {
            StylusMenu(mutableStateOf(false), mutableStateOf(StylusMenu.Selected.LEFT))
        }

        DropdownMenu(
            expanded = stylusMenu.open,
            onDismissRequest = { stylusMenu.open = false }
        ) {
            for (button in StylusMenu.Selected.entries) {
                DropdownMenuItem(
                    text = { Text(button.name) },
                    onClick = {
                        stylusMenu.selected = button
                        stylusMenu.open = false
                    }
                )
            }
        }

        AndroidView(
            modifier = Modifier
                .fillMaxSize(),
            factory = {
                view = SurfaceView(it)
                view!!
            },
        )

        val currentView = view
        val currentLifestate = lifeState
        LaunchedEffect(view, lifeState) {
            withContext(Dispatchers.IO) {
                if (currentLifestate == LifecycleObserver.SystemState.Running) {
                    currentView?.let { view ->
                        val server =
                            StableServer.connect(settings, view.width, view.height, sessionInfo)
                        val fingerHandler = FingerHandler(panels, modes, server)
                        val mouseHandler = MouseHandler(server, stylusMenu)
                        val pointerHandler = PointerHandler(fingerHandler, mouseHandler)
                        val inputHandler = InputHandler(pointerHandler, server)
                        val streamer = launch {
                            catchingServerExceptions { spawnStreamer(server, view) }
                        }
                        val inputter = launch {
                            for (event in channel) {
                                catchingServerExceptions { inputHandler.handleInput(event) }
                            }
                        }
                        joinAll(streamer, inputter)
                    }
                }
            }
        }
    }

    private fun spawnStreamer(server: StableServer.Streaming, view: SurfaceView) {
        var byteArray = server.get()
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
        val codec = MediaCodecList(MediaCodecList.ALL_CODECS)
            .findDecoderForFormat(format)
            .let(MediaCodec::createByCodecName)

        // But once it has found the codec, these values work fine...
        val width = (view.width / settings.scale).roundToInt()
        val height = (view.height / settings.scale).roundToInt()
        format.setInteger(MediaFormat.KEY_WIDTH, width)
        format.setInteger(MediaFormat.KEY_HEIGHT, height)
        codec.configure(format, view.holder.surface, null, 0)
        codec.start()

        while (view.holder.surface.isValid) {
            drawArray(codec, byteArray)
            byteArray = server.get()
        }
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

    private fun catchingServerExceptions(action: () -> Unit) {
        try {
            action()
        } catch (ex: Exception) {
            Log.e("Frostop", ex.message, ex)
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