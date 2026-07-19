package bg.blacksea.buoys.domain

import java.time.Instant

enum class ValueOrigin { MEASURED, FORECAST, ESTIMATED, UNAVAILABLE }
enum class DataSourceType { OBSERVATION_STATION, WEATHER_FORECAST_MODEL, MARINE_FORECAST_MODEL, MANUAL, UNKNOWN }
data class DataSourceInfo(val id:String,val displayName:String,val type:DataSourceType,val sourceUrl:String?=null,val attributionText:String?=null,val licenseName:String?=null,val observationTime:Instant?=null,val modelRunTime:Instant?=null,val fetchedAt:Instant,val isForecast:Boolean)
data class TemperatureUiModel(val label:String,val formattedValue:String,val origin:ValueOrigin,val sourceLabel:String?=null,val observationTimeLabel:String?=null)
enum class WarningAuthority { APPLICATION_ESTIMATE, OFFICIAL_AUTHORITY, STATION_OPERATOR, UNKNOWN }
