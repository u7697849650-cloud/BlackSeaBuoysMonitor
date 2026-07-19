package bg.blacksea.buoys.forecast

import bg.blacksea.buoys.domain.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import java.time.*
import java.util.concurrent.TimeUnit

data class HourlyAtmosphericForecast(val time:Instant,val airTemperatureCelsius:Double?,val apparentTemperatureCelsius:Double?,val windSpeedMetersPerSecond:Double?=null,val windGustMetersPerSecond:Double?=null,val precipitationMillimeters:Double?=null,val thunderstormProbabilityPercent:Double?=null,val uvIndex:Double?=null)
data class DailyAtmosphericForecast(val date:LocalDate,val minimumAirTemperatureCelsius:Double?,val maximumAirTemperatureCelsius:Double?,val minimumApparentTemperatureCelsius:Double?,val maximumApparentTemperatureCelsius:Double?,val sunrise:Instant?=null,val sunset:Instant?=null,val maximumUvIndex:Double?=null)
data class AtmosphericForecast(val currentAirTemperatureCelsius:Double?,val currentApparentTemperatureCelsius:Double?,val hourly:List<HourlyAtmosphericForecast>,val daily:List<DailyAtmosphericForecast>,val source:DataSourceInfo,val generatedAt:Instant?,val fetchedAt:Instant)
data class AtmosphericForecastState(val forecast:AtmosphericForecast?=null,val loading:Boolean=false,val error:String?=null)

@Serializable data class WeatherResponse(val timezone:String?=null,val current:WeatherCurrent?=null,val hourly:WeatherHourly?=null,val daily:WeatherDaily?=null)
@Serializable data class WeatherCurrent(val time:String?=null,@SerialName("temperature_2m")val temperature:Double?=null,@SerialName("apparent_temperature")val apparent:Double?=null)
@Serializable data class WeatherHourly(val time:List<String>?=null,@SerialName("temperature_2m")val temperature:List<Double?>?=null,@SerialName("apparent_temperature")val apparent:List<Double?>?=null,@SerialName("wind_speed_10m")val windSpeed:List<Double?>?=null,@SerialName("wind_gusts_10m")val windGust:List<Double?>?=null,val precipitation:List<Double?>?=null,@SerialName("precipitation_probability")val precipitationProbability:List<Double?>?=null,@SerialName("uv_index")val uvIndex:List<Double?>?=null)
@Serializable data class WeatherDaily(val time:List<String>?=null,@SerialName("temperature_2m_min")val minimum:List<Double?>?=null,@SerialName("temperature_2m_max")val maximum:List<Double?>?=null,@SerialName("apparent_temperature_min")val apparentMinimum:List<Double?>?=null,@SerialName("apparent_temperature_max")val apparentMaximum:List<Double?>?=null,val sunrise:List<String?>?=null,val sunset:List<String?>?=null,@SerialName("uv_index_max")val uvIndexMaximum:List<Double?>?=null)
interface OpenMeteoWeatherApi{@GET("v1/forecast")suspend fun forecast(@Query("latitude")latitude:Double,@Query("longitude")longitude:Double,@Query("current")current:String="temperature_2m,apparent_temperature",@Query("hourly")hourly:String="temperature_2m,apparent_temperature,wind_speed_10m,wind_gusts_10m,precipitation,precipitation_probability,uv_index",@Query("daily")daily:String="temperature_2m_max,temperature_2m_min,apparent_temperature_max,apparent_temperature_min,sunrise,sunset,uv_index_max",@Query("wind_speed_unit")windSpeedUnit:String="ms",@Query("timezone")timezone:String="auto",@Query("forecast_days")days:Int=7):ResponseBody}
interface AtmosphericForecastProvider{suspend fun forecast(destination:ForecastDestination,days:Int=7):Result<AtmosphericForecast>}
class OpenMeteoAtmosphericForecastProvider:AtmosphericForecastProvider{
 private val json=Json{ignoreUnknownKeys=true};private val api=Retrofit.Builder().baseUrl("https://api.open-meteo.com/").client(OkHttpClient.Builder().connectTimeout(10,TimeUnit.SECONDS).readTimeout(20,TimeUnit.SECONDS).build()).build().create(OpenMeteoWeatherApi::class.java)
 override suspend fun forecast(destination:ForecastDestination,days:Int)=runCatching{parse(json.decodeFromString(api.forecast(destination.latitude,destination.longitude,days=days.coerceIn(1,7)).string()))}
 internal fun parse(r:WeatherResponse):AtmosphericForecast{
  val fetched=Instant.now()
  val zone=runCatching{ZoneId.of(r.timezone?:"Europe/Sofia")}.getOrDefault(ZoneId.of("Europe/Sofia"))
  fun toInstant(value:String?):Instant?=value?.let{runCatching{LocalDateTime.parse(it).atZone(zone).toInstant()}.getOrNull()}
  val hourly=r.hourly?.time.orEmpty().mapIndexedNotNull{i,t->toInstant(t)?.let{HourlyAtmosphericForecast(it,r.hourly?.temperature?.getOrNull(i),r.hourly?.apparent?.getOrNull(i),r.hourly?.windSpeed?.getOrNull(i),r.hourly?.windGust?.getOrNull(i),r.hourly?.precipitation?.getOrNull(i),r.hourly?.precipitationProbability?.getOrNull(i),r.hourly?.uvIndex?.getOrNull(i))}}
  val daily=r.daily?.time.orEmpty().mapIndexedNotNull{i,t->runCatching{LocalDate.parse(t)}.getOrNull()?.let{date->DailyAtmosphericForecast(date,r.daily?.minimum?.getOrNull(i),r.daily?.maximum?.getOrNull(i),r.daily?.apparentMinimum?.getOrNull(i),r.daily?.apparentMaximum?.getOrNull(i),toInstant(r.daily?.sunrise?.getOrNull(i)),toInstant(r.daily?.sunset?.getOrNull(i)),r.daily?.uvIndexMaximum?.getOrNull(i))}}
  val source=DataSourceInfo("open-meteo-weather","Open-Meteo Weather",DataSourceType.WEATHER_FORECAST_MODEL,"https://open-meteo.com/",fetchedAt=fetched,isForecast=true)
  return AtmosphericForecast(r.current?.temperature,r.current?.apparent,hourly,daily,source,toInstant(r.current?.time),fetched)
 }
}
class AtmosphericForecastRepository(private val provider:AtmosphericForecastProvider){private val states=mutableMapOf<String,MutableStateFlow<AtmosphericForecastState>>();fun observe(id:String)=states.getOrPut(id){MutableStateFlow(AtmosphericForecastState())}.asStateFlow();suspend fun refresh(d:ForecastDestination){val s=states.getOrPut(d.id){MutableStateFlow(AtmosphericForecastState())};s.update{it.copy(loading=true,error=null)};provider.forecast(d).fold({s.value=AtmosphericForecastState(it)},{s.value=s.value.copy(loading=false,error="Температурата на въздуха временно не е налична.")})}}
data class CombinedMarineWeatherUiState(val marine:ForecastState,val atmospheric:AtmosphericForecastState,val swellAssessment:bg.blacksea.buoys.domain.swell.SwellConditionAssessment?,val isRefreshing:Boolean,val partialDataMessage:String?)
