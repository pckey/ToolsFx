package me.leon

import me.leon.encode.base.*
import me.leon.ext.*
import me.leon.ext.crypto.parsePublicKeyFromCerFile
import org.junit.Test
import java.io.File
import java.net.URLDecoder
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.zip.CRC32
import kotlin.system.measureNanoTime
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class MyTest {

    @Test
    fun crc32Test() {
        CRC32().apply { update("hello".toByteArray()) }.value.also {
            assertEquals("3610a686", it.toString(16))
        }
    }

    @Test
    fun cerParse() {
        File(TEST_DATA_DIR, "rsa/public.cer").parsePublicKeyFromCerFile().also {
            println(it.base64())
        }
        File(TEST_DATA_DIR, "rsa/pub_cer_2048.pem").parsePublicKeyFromCerFile().also {
            println(it.base64())
        }
    }

    @Test
    fun exceptionTest() {
        println(NullPointerException().stacktrace())
    }

    @Test
    fun decodeUnicode() {
        val u = "&#20320;&#22909;&#20013;&#22269;&#x4e2d;&#x56fd;&#X56FD;"
        println(u.unicode2String())
        assertEquals("🗻", "🗻".toUnicodeString().unicode2String())
        assertEquals("🗻", "🗻".toUnicodeString().unicode2String())

        assertContentEquals(
            arrayOf("🗾", "🗾"),
            arrayOf("&#128510;".unicode2String(), "128510".toInt().toUnicodeChar())
        )

        assertContentEquals(
            arrayOf(128510, 128507),
            arrayOf("\uD83D\uDDFE".unicodeCharToInt(), "🗻".unicodeCharToInt())
        )
        println("🗾".unicodeCharToInt())
    }

    @Test
    fun hex2Base64() {
        "e4bda0e5a5bd4c656f6e21".hex2ByteArray().base64().also {
            assertEquals("5L2g5aW9TGVvbiE=", it)
        }
    }

    @Test
    fun baseNEncode() {

        val msg = "开发工具集合 by leon406@52pojie.cn"
        val base58 = "CR58UvatBfMNr917q5LwvMbAtrpuA5s3iCQe5eDivFqEz8LN1Ytu6aH"
        assertEquals(base58, msg.base58())

        measureNanoTime {
            msg.toByteArray().baseCheck().also {
                assertEquals(msg, String(it.baseCheckDecode()))
            }
        }
            .also { println("total $it") }

        measureNanoTime {
            msg.base58Check().also {
                assertEquals(msg, it.base58CheckDecode2String())
            }
        }
            .also { println("total2 $it") }
    }

    @Test
    fun urlDecodeTest() {
        val raw =
            "https://subcon.dlj.tf/sub?target=clash&new_name=true&url=" +
                    "ss://YWVzLTI1Ni1nY206NTRhYTk4NDYtN2YzMS00MzdmLTgxNjItOGNiMzc1" +
                    "MjBiNTRlQGd6bS5taXNha2EucmVzdDoxMTQ1MQ==#%E9%A6%99%E6%B8%AF%E" +
                    "F%BC%9ATG%E5%AE%98%E7%BD%91%40freeyule|ss://YWVzLTI1Ni1nY206NTRhY" +
                    "Tk4NDYtN2YzMS00MzdmLTgxNjItOGNiMzc1MjBiNTRlQGd6bS5taXNha2EucmVzdDoxM" +
                    "TQ1Mg==#%E6%97%A5%E6%9C%AC%EF%BC%9ATG%E5%AE%98%E7%BD%91%40freeyule&inse" +
                    "rt=false&config=https://raw.githubusercontent.com/ACL4SSR/ACL4SSR/mas" +
                    "er/Clash/config/ACL4SSR_Online.ini"

        URLDecoder.decode(raw).also { println(it) }
    }

    @Test
    fun localDate() {

        val now = LocalDateTime.now()
        println(now)
        LocalDateTime.parse(
            "2020-10-11 10:00:00",
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        )
            .toInstant(ZoneOffset.of("+8"))
            .toEpochMilli()
            .also { println(it) }

        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_hh-mm-ss")).also {
            println(it)
        }
    }


    @Test
    fun charset() {
        val r = "中国China 666"
        val uft8Bytes = r.toByteArray()
        val gbkBytes = r.toByteArray(Charset.forName("gb2312"))
        val big5Bytes = r.toByteArray(Charset.forName("BIG5"))
        val iso88591 = r.toByteArray(Charset.forName("ISO8859-1"))
        uft8Bytes.contentToString().also { println(it) }
        println(gbkBytes.charsetChange("gbk", "utf-8").contentToString())
        println(big5Bytes.charsetChange("BIG5", "utf-8").contentToString())
        println(iso88591.charsetChange("ISO8859-1", "utf-8").contentToString())
        gbkBytes.contentToString().also { println(it) }
        big5Bytes.contentToString().also { println(it) }
        iso88591.contentToString().also { println(it) }

        String(iso88591).also { println(it) }
        String(uft8Bytes).also { println(it) }
        uft8Bytes.toString(Charset.forName("gb2312")).also { println(it) }
        gbkBytes.toString(Charset.forName("gbk")).also { println(it) }
        big5Bytes.toString(Charset.forName("big5")).also { println(it) }
        iso88591.toString(Charset.forName("utf-8")).also { println(it) }
    }

    @Test
    fun collectionSpit() {
        val l = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        l.sliceList(mutableListOf(1, 2, 3, 4)).also { println(it) }
        val l2 = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
        l2.sliceList(mutableListOf(4, 2, 3, 1)).also { println(it) }
    }

    @Test
    fun sss() {
        val map =
            mapOf(
                0 to arrayOf('目', '口', '凹', '凸', '田'),
                1 to arrayOf('由'),
                2 to arrayOf('中'),
                3 to arrayOf('人', '入', '古'),
                4 to arrayOf('工', '互'),
                5 to arrayOf('果', '克', '尔', '土', '大'),
                6 to arrayOf('木', '王'),
                7 to arrayOf('夫', '主'),
                8 to arrayOf('井', '关', '丰', '并'),
                9 to arrayOf('圭', '羊'),
            )

        map.values.zip(map.keys).flatMap { (array, key) -> array.map { it to key } }.toMap().also {
            println(it)
        }
    }

    @Test
    fun updateJsonParse() {
        File("${TEST_PRJ_DIR.absolutePath}/update.json").readText()
            .fromJson(Map::class.java)
            .also {
                println(it["info"])
            }
    }
}
