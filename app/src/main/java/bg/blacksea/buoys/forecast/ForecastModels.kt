package bg.blacksea.buoys.forecast

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.*

@Serializable data class ForecastDestination(val id:String,val name:String,val latitude:Double,val longitude:Double)
data class MarineForecastPoint(val destinationId:String,val forecastAt:Instant,val waveHeightM:Double?,val waveDirectionDeg:Double?,val wavePeriodS:Double?,val windWaveHeightM:Double?,val windWaveDirectionDeg:Double?,val windWavePeriodS:Double?,val swellWaveHeightM:Double?,val swellWaveDirectionDeg:Double?,val swellWavePeriodS:Double?,val seaSurfaceTemperatureC:Double?,val source:String="Open-Meteo Marine",val fetchedAt:Instant,val isCached:Boolean=false)
data class MarineForecastDay(val date:LocalDate,val minimumSeaTemperatureC:Double?,val maximumSeaTemperatureC:Double?,val maximumWaveHeightM:Double?,val dominantWaveDirectionDeg:Double?,val averageWavePeriodS:Double?,val points:List<MarineForecastPoint>)

@Serializable data class OpenMeteoMarineResponse(val latitude:Double?=null,val longitude:Double?=null,val timezone:String?=null,val hourly:OpenMeteoMarineHourly?=null)
@Serializable data class OpenMeteoMarineHourly(val time:List<String>?=null,@SerialName("wave_height")val waveHeight:List<Double?>?=null,@SerialName("wave_direction")val waveDirection:List<Double?>?=null,@SerialName("wave_period")val wavePeriod:List<Double?>?=null,@SerialName("wind_wave_height")val windWaveHeight:List<Double?>?=null,@SerialName("wind_wave_direction")val windWaveDirection:List<Double?>?=null,@SerialName("wind_wave_period")val windWavePeriod:List<Double?>?=null,@SerialName("swell_wave_height")val swellWaveHeight:List<Double?>?=null,@SerialName("swell_wave_direction")val swellWaveDirection:List<Double?>?=null,@SerialName("swell_wave_period")val swellWavePeriod:List<Double?>?=null,@SerialName("sea_surface_temperature")val seaSurfaceTemperature:List<Double?>?=null)

fun degreesToCompassDirection(degrees:Double?):String { if(degrees==null)return "Няма данни";val names=listOf("С","ССИ","СИ","ИСИ","И","ИЮИ","ЮИ","ЮЮИ","Ю","ЮЮЗ","ЮЗ","ЗЮЗ","З","ЗСЗ","СЗ","ССЗ");return names[((degrees.mod(360.0)/22.5)+.5).toInt()%16] }
fun List<MarineForecastPoint>.days(zone:ZoneId=ZoneId.of("Europe/Sofia"))=groupBy{it.forecastAt.atZone(zone).toLocalDate()}.map{(date,p)->MarineForecastDay(date,p.mapNotNull{it.seaSurfaceTemperatureC}.minOrNull(),p.mapNotNull{it.seaSurfaceTemperatureC}.maxOrNull(),p.mapNotNull{it.waveHeightM}.maxOrNull(),p.mapNotNull{it.waveDirectionDeg}.groupingBy{(it/22.5).toInt()}.eachCount().maxByOrNull{it.value}?.key?.times(22.5),p.mapNotNull{it.wavePeriodS}.average().takeUnless{it.isNaN()},p)}.sortedBy{it.date}
fun List<MarineForecastPoint>.nearestFuture(now:Instant=Instant.now())=filter{!it.forecastAt.isBefore(now)}.minByOrNull{it.forecastAt}
