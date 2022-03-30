package me.leon.ext.crypto

import java.io.File
import java.util.zip.CRC32
import java.util.zip.CheckedInputStream

fun String.crc32() = CRC32().apply { update(this@crc32.toByteArray()) }.value.toULong().toString(16)

fun ByteArray.crc32() = CRC32().apply { update(this@crc32) }.value.toULong().toString(16)

fun String.crc32File() =
    File(this).inputStream().use {
        val crc = CRC32()
        CheckedInputStream(it, crc).use {
            while (it.read() != -1) {
                // if needed
            }
        }
        crc.value.toULong().toString(16)
    }

fun String.crc64() = CRC64().apply { update(this@crc64.toByteArray()) }.value.toULong().toString(16)

fun ByteArray.crc64() = CRC64().apply { update(this@crc64) }.value.toULong().toString(16)

fun String.crc64File() =
    File(this).inputStream().use {
        val crc = CRC64()
        CheckedInputStream(it, crc).use {
            while (it.read() != -1) {
                // if needed
            }
        }
        crc.value.also { println(it) }.toULong().toString(16)
    }
