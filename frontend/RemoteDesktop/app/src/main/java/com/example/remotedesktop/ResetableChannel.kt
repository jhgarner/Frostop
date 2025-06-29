package com.example.remotedesktop

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ChannelIterator
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield

class ResetableChannel<T> : ChannelIterator<T> {
    var channel: Channel<T> = Channel(Channel.UNLIMITED)
    var channelIterator: ChannelIterator<T> = channel.iterator()

    fun send(t: T) {
        runBlocking {
            channel.send(t)
        }
    }

    fun reset() {
        channel.close()
        channel = Channel(Channel.UNLIMITED)
        channelIterator = channel.iterator()
    }

    operator fun iterator(): ChannelIterator<T> {
        return this
    }

    override suspend fun hasNext(): Boolean {
        while (!channelIterator.hasNext()) {
            yield()
        }
        return true
    }

    override fun next(): T {
        return channelIterator.next()
    }
}