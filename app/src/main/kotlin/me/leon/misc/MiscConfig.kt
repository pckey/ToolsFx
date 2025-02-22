package me.leon.misc

/**
 * @author Leon
 * @since 2023-06-02 14:04
 * @email deadogone@gmail.com
 */
const val OPTIONS = "options"
const val HINT = "hint"
const val PARAMS_HINT = "params_hint"
val MISC_CONFIG =
    mapOf(
        MiscServiceType.UUID to mapOf(HINT to "generate count"),
        MiscServiceType.TIME_STAMP to mapOf(HINT to "timestamp digit,separate by line"),
        MiscServiceType.DATE2STAMP to
            mapOf(
                HINT to
                    ("date, support format " +
                        "like 2023-02-01 12:00:00, 2023-02-01, 2023/02/01, 20230201,separate by line")
            ),
        MiscServiceType.PORT_SCAN to mapOf(HINT to "ip or domain (port from 1 to 10000)"),
        MiscServiceType.PORT_SCAN_FULL to mapOf(HINT to "ip or domain ( port from 1 to 65535)"),
        MiscServiceType.IP_SCAN to mapOf(HINT to "ip w/o last dot,like 192.168.0"),
        MiscServiceType.BATCH_PING to mapOf(HINT to "ping ip or domains,separate by line"),
        MiscServiceType.TCPING to
            mapOf(HINT to "tcp ping ip or domains,separate by line,format ip:port"),
        MiscServiceType.WHOIS to mapOf(HINT to "domain,separate by line"),
        MiscServiceType.ICP to mapOf(HINT to "domain, 工信部备案信息,separate by line"),
        MiscServiceType.IP2INT to
            mapOf(HINT to "ip, transform ip to integer, eg. 192.168.0.1,separate by line"),
        MiscServiceType.INT2IP to
            mapOf(HINT to "int, transform integer to ip,  eg. 3232235521,separate by line"),
        MiscServiceType.CIDR to mapOf(HINT to "ip, format 192.168.0.1/25,separate by line"),
        MiscServiceType.LINK_CHECK to mapOf(HINT to "url links,separate by line"),
        MiscServiceType.IP_LOCATION to mapOf(HINT to "ip/url"),
        MiscServiceType.DNS_SOLVE to mapOf(HINT to "domains,separate by line, comment by #"),
        MiscServiceType.CRON_EXPLAIN to
            mapOf(HINT to "cron expression, support crontab, quarts and normal format"),
        MiscServiceType.GITHUB_MIRROR to mapOf(HINT to "github repo or raw link"),
        MiscServiceType.ENCODING_RECOVERY to mapOf(HINT to "recover encoding"),
        MiscServiceType.FULL_WIDTH to mapOf(HINT to "transfer full/half width char"),
        MiscServiceType.ROMAN to mapOf(HINT to "roman number, like VIII or 8,separate by line"),
        MiscServiceType.ROMANJI to mapOf(HINT to "romanji for Chinese,Japanese,Korean"),
    )

val MISC_OPTIONS_CONFIG =
    mapOf(
        MiscServiceType.TIME_STAMP to
            mapOf(
                OPTIONS to
                    arrayOf(
                        "milliseconds",
                        "seconds",
                        "minutes",
                        "hours",
                        "days",
                    )
            ),
        MiscServiceType.ROMANJI to
            mapOf(OPTIONS to KawaType.values().map { it.toString() }.toTypedArray()),
        MiscServiceType.FULL_WIDTH to mapOf(OPTIONS to arrayOf("toFull", "toHalf")),
        MiscServiceType.DATE2STAMP to mapOf(PARAMS_HINT to arrayOf("optional, input date format")),
    )
