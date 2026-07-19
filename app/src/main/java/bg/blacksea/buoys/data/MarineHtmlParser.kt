package bg.blacksea.buoys.data

import bg.blacksea.buoys.domain.BuoyObservation
import org.jsoup.Jsoup
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class MarineHtmlParser {
    private val aliases = mapOf(
        "station" to listOf("станция", "буй", "местоположение", "station", "name"),
        "date" to listOf("дата", "час", "време", "date", "time", "utc"),
        "temp" to listOf("температура", "вода", "water", "temp"),
        "hm0" to listOf("hm0", "значима", "significant"), "hmax" to listOf("hmax", "максимална", "maximum"),
        "period" to listOf("период", "period", "t02"), "waveDir" to listOf("meandir", "посока на въл", "wave direction", "dir wave"),
        "wind" to listOf("скорост на вят", "wind speed"), "windDir" to listOf("посока на вят", "wind direction")
    )
    fun parse(html: String, source: String): List<BuoyObservation> {
        val tables = Jsoup.parse(html).select("table")
        for (table in tables) {
            val rows = table.select("tr"); if (rows.size < 2) continue
            val headers = rows.first()!!.select("th,td").map { normalize(it.text()) }
            val idx = aliases.mapValues { (_, names) -> headers.indexOfFirst { h -> names.any(h::contains) } }
            if (idx["station"]!! < 0 || idx["date"]!! < 0 || (idx["temp"]!! < 0 && idx["hm0"]!! < 0)) continue
            val normalRows = rows.drop(1).map { it.select("td,th").map { cell -> cell.text().trim() } }.filter { it.size >= headers.size }
            // mm.meteo-varna.net has historically emitted later <td> groups without enclosing <tr>.
            // Grouping all body cells by the discovered header count keeps the parser useful without fixed column indexes.
            val allCells = table.select("tbody td").map { it.text().trim() }
            val dataRows = if (allCells.size >= headers.size * 2) allCells.chunked(headers.size).filter { it.size == headers.size } else normalRows
            return dataRows.mapNotNull { c ->
                fun at(key: String) = idx[key]!!.takeIf { it >= 0 && it < c.size }?.let(c::get)
                val name = at("station")?.takeIf(String::isNotBlank) ?: return@mapNotNull null
                BuoyObservation(slug(name), name, parseInstant(at("date")), number(at("temp")), number(at("hm0")), number(at("hmax")),
                    number(at("period")), number(at("waveDir")), number(at("wind")), number(at("windDir")), null, null, source, Instant.now())
            }.ifEmpty { throw MarineDataException.Structure("таблицата няма валидни редове") }
        }
        throw MarineDataException.Structure("не е намерена таблица с разпознаваеми заглавия")
    }
    internal fun number(value: String?): Double? = value?.trim()?.replace(',', '.')?.let { Regex("[-+]?\\d+(?:\\.\\d+)?").find(it)?.value?.toDoubleOrNull() }?.takeIf { it in -1000.0..1000.0 }
    internal fun parseInstant(value: String?): Instant? {
        if (value.isNullOrBlank()) return null
        runCatching { return Instant.parse(value.trim()) }
        val formats = listOf("dd.MM.yyyy HH:mm", "dd/MM/yyyy HH:mm", "dd/MM/yyyy HH:mm:ss", "yyyy-MM-dd HH:mm")
        for (f in formats) try { return LocalDateTime.parse(value.trim(), DateTimeFormatter.ofPattern(f)).toInstant(ZoneOffset.UTC) } catch (_: DateTimeParseException) {}
        return null
    }
    private fun normalize(s: String) = s.lowercase().replace(Regex("\\s+"), " ")
    private fun slug(s: String) = s.lowercase().replace(Regex("[^a-zа-я0-9]+"), "-").trim('-')
}
