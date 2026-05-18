package com.bhanu.aegis.core.data.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object ChakuliSettingsSerializer : Serializer<ChakuliSettings> {
    override val defaultValue: ChakuliSettings = ChakuliSettings.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): ChakuliSettings {
        try {
            return ChakuliSettings.parseFrom(input)
        } catch (e: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", e)
        }
    }

    override suspend fun writeTo(t: ChakuliSettings, output: OutputStream) {
        t.writeTo(output)
    }
}
