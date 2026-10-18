package io.github.kotlinmania.time

import io.github.kotlinmania.serde.SerdeResult
import io.github.kotlinmania.serdecore.de.Deserialize
import io.github.kotlinmania.serdecore.de.Deserializer
import io.github.kotlinmania.serdecore.ser.Serialize
import io.github.kotlinmania.serdetest.Compact
import io.github.kotlinmania.serdetest.Readable
import io.github.kotlinmania.serdetest.compact
import io.github.kotlinmania.serdetest.readable

fun <T> Result<T>.unwrap(): T = getOrThrow()

fun <T> Result<T>.unwrapErr(): Throwable =
    exceptionOrNull() ?: error("Expected failure but was $this")

val <T> Result<T>.error: Throwable
    get() = exceptionOrNull() ?: error("Expected failure but was $this")

object TimeResult {
    fun <T> Ok(value: T): Result<T> = Result.success(value)

    fun Err(err: Throwable): Result<Nothing> = Result.failure(err)
}

fun <T : Serialize> Deserialize<T>.compactDe(): Deserialize<Compact<T>> =
    object : Deserialize<Compact<T>> {
        private val configured = this@compactDe.compact()

        override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<Compact<T>> =
            configured.deserialize(deserializer).map { Compact(it) }

        override fun <D : Deserializer> deserializeInPlace(
            deserializer: D,
            place: (Compact<T>) -> Unit,
        ): SerdeResult<Unit> =
            configured.deserializeInPlace(deserializer) { place(Compact(it)) }
    }

fun <T : Serialize> Deserialize<T>.readableDe(): Deserialize<Readable<T>> =
    object : Deserialize<Readable<T>> {
        private val configured = this@readableDe.readable()

        override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<Readable<T>> =
            configured.deserialize(deserializer).map { Readable(it) }

        override fun <D : Deserializer> deserializeInPlace(
            deserializer: D,
            place: (Readable<T>) -> Unit,
        ): SerdeResult<Unit> =
            configured.deserializeInPlace(deserializer) { place(Readable(it)) }
    }
