package bg.blacksea.buoys.forecast

import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import java.time.*
import java.util.concurrent.TimeUnit

private const val VARIABLES="wave_height,wave_direction,wave_period,wind_wave_height,wind_wave_direction,wind_wave_period,swell_wave_height,swell_wave_direction,swell_wave_period,sea_surface_temperature"
interface OpenMeteoMarineApi{@GET("v1/marine")suspend fun forecast(@Query("latitude")latitude:Double,@Query("longitude")longitude:Double,@Query("hourly")hourly:String=VARIABLES,@Query("timezone")timezone:String="Europe/Sofia",@Query("forecast_days")days:Int=7):ResponseBody}
interface MarineForecastProvider{suspend fun getHourlyForecast(destination:ForecastDestination,days:Int):Result<List<MarineForecastPoint>>}
class OpenMeteoMarineForecastProvider:MarineForecastProvider{
    private val json=Json{ignoreUnknownKeys=true};private val api=Retrofit.Builder().baseUrl("https://marine-api.open-meteo.com/").client(OkHttpClient.Builder().connectTimeout(10,TimeUnit.SECONDS).readTimeout(20,TimeUnit.SECONDS).build()).build().create(OpenMeteoMarineApi::class.java)
    override suspend fun getHourlyForecast(destination:ForecastDestination,days:Int)=runCatching{parse(json.decodeFromString(api.forecast(destination.latitude,destination.longitude,days=days.coerceIn(1,7)).string()),destination)}
    internal fun parse(response:OpenMeteoMarineResponse,d:ForecastDestination):List<MarineForecastPoint>{val h=response.hourly?:error("Липсва почасова прогноза");val fetched=Instant.now();return h.time.orEmpty().mapIndexedNotNull{i,t->val instant=runCatching{LocalDateTime.parse(t).atZone(ZoneId.of(response.timezone?:"Europe/Sofia")).toInstant()}.getOrNull()?:return@mapIndexedNotNull null
        fun v(a:List<Double?>?):Double? = a?.getOrNull(i)
        MarineForecastPoint(d.id,instant,v(h.waveHeight),v(h.waveDirection),v(h.wavePeriod),v(h.windWaveHeight),v(h.windWaveDirection),v(h.windWavePeriod),v(h.swellWaveHeight),v(h.swellWaveDirection),v(h.swellWavePeriod),v(h.seaSurfaceTemperature),fetchedAt=fetched)}}
}
class MockMarineForecastProvider:MarineForecastProvider{override suspend fun getHourlyForecast(destination:ForecastDestination,days:Int)=Result.success((0 until days*24).map{i->MarineForecastPoint(destination.id,Instant.now().plusSeconds(i*3600L),.4+i%5*.05,70.0,4.2,.25,65.0,3.5,.2,80.0,5.0,23.0+i%6*.1,fetchedAt=Instant.now())})}
