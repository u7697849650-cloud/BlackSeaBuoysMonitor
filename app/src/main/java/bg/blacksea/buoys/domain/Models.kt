package bg.blacksea.buoys.domain

import kotlinx.serialization.Serializable
import java.time.Duration
import java.time.Instant

@Serializable data class MarineStation(val id: String, val name: String, val latitude: Double? = null, val longitude: Double? = null)
@Serializable data class RawObservation(
    val stationId: String, val stationName: String, val measuredAtUtc: String? = null,
    val waterTemperatureC: Double? = null, val significantWaveHeightM: Double? = null,
    val maximumWaveHeightM: Double? = null, val wavePeriodS: Double? = null,
    val waveDirectionDeg: Double? = null, val windSpeedMs: Double? = null,
    val windDirectionDeg: Double? = null, val latitude: Double? = null, val longitude: Double? = null
)
data class BuoyObservation(
    val stationId: String, val stationName: String, val measuredAtUtc: Instant?,
    val waterTemperatureC: Double?, val significantWaveHeightM: Double?,
    val maximumWaveHeightM: Double?, val wavePeriodS: Double?, val waveDirectionDeg: Double?,
    val windSpeedMs: Double?, val windDirectionDeg: Double?, val latitude: Double?, val longitude: Double?,
    val sourceUrl: String, val receivedAtUtc: Instant, val isCached: Boolean = false
)
data class MarineDestination(val id: String, val name: String, val stationId: String?, val latitude: Double?, val longitude: Double?, val isFavorite: Boolean)

enum class Freshness(val label: String) { CURRENT("Актуални"), STALE("Остарели"), VERY_OLD("Много стари"), NO_DATA("Няма данни"), CACHED("Кеширани данни") }
fun freshness(measured: Instant?, now: Instant = Instant.now(), cached: Boolean = false): Freshness {
    if (measured == null) return Freshness.NO_DATA
    if (cached) return Freshness.CACHED
    val hours = Duration.between(measured, now).toHours()
    return when { hours <= 3 -> Freshness.CURRENT; hours <= 24 -> Freshness.STALE; else -> Freshness.VERY_OLD }
}
fun compass(deg: Double?): String = if (deg == null) "—" else listOf("С", "СИ", "И", "ЮИ", "Ю", "ЮЗ", "З", "СЗ")[(deg.mod(360.0) / 45.0 + .5).toInt() % 8]
