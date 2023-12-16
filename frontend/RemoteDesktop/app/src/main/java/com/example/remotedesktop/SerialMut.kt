package com.example.remotedesktop

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class MutSerializer<T>(private val dataSerializer: KSerializer<T>) : KSerializer<Mut<T>> {
    override val descriptor: SerialDescriptor = dataSerializer.descriptor
    override fun serialize(encoder: Encoder, value: Mut<T>) =
        dataSerializer.serialize(encoder, value.value)

    override fun deserialize(decoder: Decoder): Mut<T> =
        Mut(mutableStateOf(dataSerializer.deserialize(decoder)))
}


@Serializable(with = MutSerializer::class)
class Mut<T>(state: MutableState<T>) : MutableState<T> by state

fun <T> mutOf(t: T): Mut<T> = Mut(mutableStateOf(t))

@Serializable(with = MutListSerializer::class)
class MutList<T>(val state: MutableList<T>) : MutableList<T> by state

fun <T> mutList(): MutList<T> = MutList(mutableStateListOf())

class MutListSerializer<T>(dataSerializer: KSerializer<T>) : KSerializer<MutList<T>> {
    private val delegateSerializer = ListSerializer(dataSerializer)

    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor =
        SerialDescriptor("MutList", delegateSerializer.descriptor)

    override fun serialize(encoder: Encoder, value: MutList<T>) {
        encoder.encodeSerializableValue(delegateSerializer, value.state)
    }

    override fun deserialize(decoder: Decoder): MutList<T> {
        val list = decoder.decodeSerializableValue(delegateSerializer)
        val newList: MutableList<T> = mutableStateListOf()
        newList.addAll(list)
        return MutList(newList)
    }
}
