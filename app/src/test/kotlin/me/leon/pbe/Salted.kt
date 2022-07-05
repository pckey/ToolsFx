package me.leon.pbe

import java.security.SecureRandom
import java.security.Security
import javax.crypto.Cipher
import me.leon.encode.base.base64
import me.leon.encode.base.base64Decode
import me.leon.ext.crypto.makeCipher
import me.leon.ext.hex2ByteArray
import me.leon.hash
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.Test

/** for PBE process comprehend */
class Salted {
    init {
        Security.addProvider(BouncyCastleProvider())
    }

    private val saltSize = 8

    @Test
    fun saltAes() {
        val d = "-85297962_172051801"
        val e = "U2FsdGVkX192df0Gxgia8s93zZp85f9m2nU1VIGU+RZQDtViB1LPBnE0CBWgVDBj"
        val password = "583a01a9ba901a3adda7252ebca42c09"
        val keySize = 32
        val ivSize = 16
        var salt = ByteArray(saltSize)
        SecureRandom().nextBytes(salt)
        //        salt = "ef9b329a95241506".hex2ByteArray()
        // 随机生成
        salt = "7675fd06c6089af2".hex2ByteArray()
        // md5 openssl
        kdf(password.toByteArray(), salt, keySize + ivSize).also {
            makeCipher(
                "AES/CBC/PKCS5Padding",
                it.sliceArray(0 until keySize),
                it.sliceArray(keySize..it.lastIndex),
                Cipher.ENCRYPT_MODE
            )
                .also {
                    println(
                        ("Salted__".toByteArray() + salt + it.doFinal(d.toByteArray())).base64()
                    )
                }
        }

        decryptKeyIv(e, password.toByteArray()).also {
            makeCipher(
                "AES/CBC/PKCS5Padding",
                it.sliceArray(0 until 32).also { println(it.contentToString()) },
                it.sliceArray(32..it.lastIndex).also { println(it.contentToString()) },
                Cipher.DECRYPT_MODE
            )
                .also {
                    val base64Decode = e.base64Decode()
                    println(
                        it.doFinal(base64Decode.sliceArray((8 + saltSize)..base64Decode.lastIndex))
                            .decodeToString()
                    )
                }
        }
    }

    @Test
    fun saltDes() {
        val e = "U2FsdGVkX1/a0jOebm4TjoQUIxsRyRm88opg+LmNUFQ="
        val password = "modern"
        val keySize = 8
        val ivSize = 8
        val d = "hello"

        var salt = ByteArray(saltSize)
        // 随机生成
        SecureRandom().nextBytes(salt)
        salt = "ef9b329a95241506".hex2ByteArray()

        salt = "7675fd06c6089af2".hex2ByteArray()
        // md5
        kdf(password.toByteArray(), salt, keySize + ivSize).also {
            makeCipher(
                "DES/CBC/PKCS5Padding",
                it.sliceArray(0 until keySize),
                it.sliceArray(keySize..it.lastIndex),
                Cipher.ENCRYPT_MODE
            )
                .also {
                    println(
                        ("Salted__".toByteArray() + salt + it.doFinal(d.toByteArray())).base64()
                    )
                }
        }

        // md2
        kdf(password.toByteArray(), salt, keySize + ivSize, "MD2").also {
            makeCipher(
                "DES/CBC/PKCS5Padding",
                it.sliceArray(0 until keySize),
                it.sliceArray(keySize..it.lastIndex),
                Cipher.ENCRYPT_MODE
            )
                .also {
                    println(
                        ("Salted__".toByteArray() + salt + it.doFinal(d.toByteArray())).base64()
                    )
                }
        }

        // sha1
        kdf(password.toByteArray(), salt, keySize + ivSize, "SHA1").also {
            makeCipher(
                "DES/CBC/PKCS5Padding",
                it.sliceArray(0 until keySize),
                it.sliceArray(keySize..it.lastIndex),
                Cipher.ENCRYPT_MODE
            )
                .also {
                    println(
                        ("Salted__".toByteArray() + salt + it.doFinal(d.toByteArray())).base64()
                    )
                }
        }

        decryptKeyIv(e, password.toByteArray(), keySize + saltSize).also {
            makeCipher(
                "DES/CBC/PKCS5Padding",
                it.sliceArray(0 until keySize).also { println(it.contentToString()) },
                it.sliceArray(keySize..it.lastIndex).also { println(it.contentToString()) },
                Cipher.DECRYPT_MODE
            )
                .also {
                    val base64Decode = e.base64Decode()
                    println(
                        it.doFinal(
                                base64Decode.sliceArray(
                                    (keySize + saltSize)..base64Decode.lastIndex
                                )
                            )
                            .decodeToString()
                    )
                }
        }
    }

    private fun kdf(
        pass: ByteArray,
        salt: ByteArray,
        outputSize: Int = 48,
        hash: String = "MD5"
    ): ByteArray {
        val tmpKey = pass + salt
        var key = tmpKey.hash(hash)

        var resultKey = key
        while (resultKey.size < outputSize) {
            key = (key + tmpKey).hash(hash)
            resultKey += key
        }
        println(resultKey.size)
        return resultKey.sliceArray(0 until outputSize)
    }

    private fun decryptKeyIv(d: String, pass: ByteArray, outputSize: Int = 48): ByteArray {
        // Salted__
        val salt = d.base64Decode().sliceArray(8 until (8 + saltSize))
        return kdf(pass, salt, outputSize)
    }
}
