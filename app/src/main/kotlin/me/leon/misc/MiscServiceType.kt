package me.leon.misc

import java.text.SimpleDateFormat
import java.util.Date
import me.leon.C1
import me.leon.P1
import me.leon.ext.*
import me.leon.misc.net.*

val SDF_TIME = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
val SDF_DATE = SimpleDateFormat("yyyy-MM-dd")
val SDF_DATE2 = SimpleDateFormat("yyyyMMdd")
private const val NOW = "NOW"
val ALL_PORTS = (1..65_535).toList()

enum class MiscServiceType(val type: String) : MiscService {
    UUID("uuid") {
        override fun process(raw: String, params: Map<String, String>) =
            (0 until runCatching { raw.toInt() }.getOrDefault(1)).joinToString(
                System.lineSeparator()
            ) {
                java.util.UUID.randomUUID().toString()
            }
    },
    TIME_STAMP("stamp2date") {
        override fun process(raw: String, params: Map<String, String>): String {
            val factor =
                when (requireNotNull(params[C1])) {
                    "seconds" -> 1000L
                    "milliseconds" -> 1L
                    "minutes" -> 60_000L
                    "hours" -> 3_600_000L
                    "days" -> 86_400_000L
                    else -> 1L
                }
            return raw.lineAction { SDF_TIME.format(Date(it.toLong() * factor)) }
                .joinToString(System.lineSeparator())
        }
    },
    DATE2STAMP("date2stamp") {
        override fun process(raw: String, params: Map<String, String>): String {
            val format = requireNotNull(params[P1])
            val sdf =
                if (format.isNotEmpty()) {
                    SimpleDateFormat(format)
                } else {
                    null
                }
            return raw.lineAction {
                    if (NOW.equals(it, true)) {
                        System.currentTimeMillis()
                    } else {
                        sdf?.parse(it)?.time
                            ?: when (it.length) {
                                10 -> {
                                    SDF_DATE.parse(it.replace("/", "-")).time
                                }
                                8 -> {
                                    SDF_DATE2.parse(it).time
                                }
                                else -> {
                                    SDF_TIME.parse(it.replace("/", "-")).time
                                }
                            }
                    }
                }
                .joinToString(System.lineSeparator())
        }
    },
    PORT_SCAN("port scan") {
        override fun process(raw: String, params: Map<String, String>) =
            raw.portScan().joinToString(System.lineSeparator())
    },
    PORT_SCAN_FULL("full port scan") {
        override fun process(raw: String, params: Map<String, String>) =
            raw.portScan(ALL_PORTS).joinToString(System.lineSeparator())
    },
    IP_SCAN("ip scan") {
        override fun process(raw: String, params: Map<String, String>) =
            raw.lanScan().joinToString(System.lineSeparator())
    },
    BATCH_PING("ping") {
        override fun process(raw: String, params: Map<String, String>) = raw.batchPing()
    },
    TCPING("tcping") {
        override fun process(raw: String, params: Map<String, String>) = raw.batchTcPing()
    },
    WHOIS("whois(online)") {
        override fun process(raw: String, params: Map<String, String>) =
            runCatching { Whois.parse(raw)?.showInfo ?: raw.whoisSocket() }
                .getOrElse { it.stacktrace() }
    },
    ICP("ICP备案(online)") {
        override fun process(raw: String, params: Map<String, String>) =
            raw.lineAction2String {
                "$it:\n\n" +
                    runCatching { MiitInfo.domainInfo(it).showInfo }.getOrElse { it.stacktrace() } +
                    "\n"
            }
    },
    IP2INT("ip2Int") {
        override fun process(raw: String, params: Map<String, String>) =
            raw.lineAction { runCatching { it.ip2Uint().toString() }.getOrElse { it.stacktrace() } }
                .joinToString(System.lineSeparator())
    },
    INT2IP("int2Ip") {
        override fun process(raw: String, params: Map<String, String>) =
            raw.lineAction { runCatching { it.toUInt().toIp() }.getOrElse { it.stacktrace() } }
                .joinToString(System.lineSeparator())
    },
    CIDR("CIDR") {
        override fun process(raw: String, params: Map<String, String>) =
            raw.lineAction { runCatching { it.cidr() }.getOrElse { it.stacktrace() } }
                .joinToString(System.lineSeparator())
    },
    IP_LOCATION("ip location(online)") {
        override fun process(raw: String, params: Map<String, String>) =
            raw.lineAction { runCatching { it.ipLocation() }.getOrElse { it.stacktrace() } }
                .joinToString(System.lineSeparator())
    },
    DNS_SOLVE("DNS hosts") {
        override fun process(raw: String, params: Map<String, String>) =
            dnsSolve(raw.lines().filterNot { it.startsWith("#") || it.isEmpty() })
    },
    CRON_EXPLAIN("Cron Explain") {
        override fun process(raw: String, params: Map<String, String>) =
            raw.lines()
                .filterNot { it.startsWith("#") || it.isEmpty() }
                .joinToString(System.lineSeparator()) { CronExpression(it).explain() }
    },
    LINK_CHECK("Link Check") {
        override fun process(raw: String, params: Map<String, String>) = raw.linkCheck()
    },
    GITHUB_MIRROR("Github Mirror") {
        override fun process(raw: String, params: Map<String, String>) = raw.githubMirror()
    },
    ENCODING_RECOVERY("recover encoding") {
        override fun process(raw: String, params: Map<String, String>) = raw.recoverEncoding()
    },
    FULL_WIDTH("half/full width") {
        override fun process(raw: String, params: Map<String, String>): String {
            val type = requireNotNull(params[C1])
            return if (type == "toFull") {
                raw.toFullWidth()
            } else {
                raw.toHalfWidth()
            }
        }
    },
    ROMAN("roman number") {
        override fun process(raw: String, params: Map<String, String>) =
            raw.lineAction2String { runCatching { it.roman() }.getOrElse { it.stacktrace() } }
    },
    ROMANJI("romanji(CJK)") {
        override fun process(raw: String, params: Map<String, String>) =
            raw.lineAction2String {
                runCatching { it.kawa(KawaType.valueOf(requireNotNull(params[C1]))).pretty() }
                    .getOrElse { it.stacktrace() }
            }
    },
    ;

    override fun hint(): String {
        return MISC_CONFIG[this]!![HINT].orEmpty()
    }

    override fun options(): Array<out String> = MISC_OPTIONS_CONFIG[this]?.get(OPTIONS).orEmpty()

    override fun paramsHints(): Array<out String> =
        MISC_OPTIONS_CONFIG[this]?.get(PARAMS_HINT).orEmpty()
}

val miscServiceTypeMap = MiscServiceType.values().associateBy { it.type }

fun String.miscServiceType() = miscServiceTypeMap[this] ?: MiscServiceType.UUID
