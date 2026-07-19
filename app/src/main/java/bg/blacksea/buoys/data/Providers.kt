package bg.blacksea.buoys.data

import android.content.Context
import bg.blacksea.buoys.domain.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.Instant
import java.util.concurrent.TimeUnit

const val SOURCE_URL = "http://mm.meteo-varna.net/en/?lang=Bg"

class MockMarineDataProvider(private val context: Context) : MarineDataProvider {
    private val json = Json { ignoreUnknownKeys = true }
    private fun asset(name: String) = context.assets.open(name).bufferedReader().use { it.readText() }
    override suspend fun getStations() = runCatching { json.decodeFromString<List<MarineStation>>(asset("mock_stations.json")) }
    override suspend fun getLatestObservations() = runCatching {
        json.decodeFromString<List<RawObservation>>(asset("mock_observations.json")).map { it.domain(SOURCE_URL) }
    }
}

class HtmlMarineDataProvider(
    private val parser: MarineHtmlParser = MarineHtmlParser(),
    private val client: OkHttpClient = OkHttpClient.Builder().connectTimeout(20, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).callTimeout(75,TimeUnit.SECONDS).retryOnConnectionFailure(true).build()
) : MarineDataProvider {
    private suspend fun load() = withContext(Dispatchers.IO) {
        var lastFailure:Exception?=null
        repeat(2){attempt->
            try {
                return@withContext client.newCall(Request.Builder().url(SOURCE_URL).header("User-Agent", "BlackSeaBuoysMonitor/1.0").header("Accept-Language","bg,en;q=0.8").build()).execute().use { response ->
                    if (!response.isSuccessful) throw MarineDataException.Http(response.code)
                    val body = response.body?.string().orEmpty()
                    if (body.isBlank()) throw MarineDataException.Empty()
                    parser.parse(body, SOURCE_URL)
                }
            } catch(e:MarineDataException){throw e}catch(e:java.net.SocketTimeoutException){lastFailure=e;if(attempt==1)throw MarineDataException.Network(e)}catch(e:Exception){throw MarineDataException.Network(e)}
        }
        throw MarineDataException.Network(lastFailure?:IllegalStateException("request failed"))
    }
    override suspend fun getStations() = runCatching { load().map { MarineStation(it.stationId, it.stationName, it.latitude, it.longitude) }.distinctBy { it.id } }
    override suspend fun getLatestObservations() = runCatching { load() }
}

fun RawObservation.domain(source: String) = BuoyObservation(stationId, stationName, measuredAtUtc?.let(Instant::parse), waterTemperatureC,
    significantWaveHeightM, maximumWaveHeightM, wavePeriodS, waveDirectionDeg, windSpeedMs, windDirectionDeg,
    latitude, longitude, source, Instant.now())
