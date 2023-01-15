package com.example.remotedesktop

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.nio.ByteBuffer

class Server {
    private val packetSize = 4096;
    private val headerSize = 9;
    private val socket = DatagramSocket()
    private val buffer = ByteArray(packetSize)
    private val packet = DatagramPacket(buffer, packetSize)
    private var expecting = 0

    init {
        socket.connect(InetSocketAddress("192.168.1.134", 9000))
        socket.send(DatagramPacket(ByteArray(1), 1))
    }

    fun get(): ByteArray {
        var output = ByteArray(0)
        var numReceived = 0;
        var lastId: Byte? = null
        var received = BooleanArray(0)
        while (true) {
            socket.receive(packet)
            val id = buffer[0]
            val numPackets = ByteBuffer.wrap(buffer, 1, 4).int
            val n = ByteBuffer.wrap(buffer, 5, 4).int
            if (id != lastId) {
                if (id.toInt() == expecting-1) {
                    continue
                }
                if (lastId != null) {
                    println("Dropped frame $lastId $id")
                }
                val size = numPackets * (packetSize - headerSize)
                output = ByteArray(size)
                numReceived = 0
                lastId = id
                received = BooleanArray(numPackets);
            }
            if (!received[n]) {
                numReceived++
            }
            received[n] = true
            val start = n * (packetSize - headerSize)
            buffer.copyInto(output, start, headerSize, packet.length)
            if (n == numPackets - 1) {
                val missing = ByteArray((numPackets - numReceived)*Int.SIZE_BYTES + 1)
                missing[0] = id
                var missingAt = 0
                for (i in received.indices) {
                    if (!received[i]) {
                        missing[missingAt * 4 + 1] = (i shr 24).toByte()
                        missing[missingAt * 4 + 2] = (i shr 16).toByte()
                        missing[missingAt * 4 + 3] = (i shr 8).toByte()
                        missing[missingAt * 4 + 4] = (i shr 0).toByte()
                        missingAt++
                    }
                }
                val missingPacket = DatagramPacket(missing, missing.size)
                socket.send(missingPacket)
                if (numReceived == numPackets) {
                    expecting = id + 1
                    return output
                }
                // println("$n $numPackets $numReceived ${missing.size}")
            }
        }
    }
}