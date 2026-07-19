package bg.blacksea.buoys.domain.activity

import java.time.Instant

enum class ActivitySuitabilityLevel(val title:String){EXCELLENT("Отлични условия"),GOOD("Добри условия"),ACCEPTABLE("Приемливи условия"),POOR("Неблагоприятни условия"),DANGEROUS("Опасни условия"),NO_DATA("Недостатъчно данни")}
enum class SuitabilitySourceType{MEASUREMENT,FORECAST,MIXED,CACHED}
enum class MarineActivityType(val title:String,val icon:String){FISHING("Риболов","🎣"),KITESURFING("Кайтсърф","🪁"),WINDSURFING("Уиндсърф","🏄"),SURFING("Сърф","🏄"),DIVING("Гмуркане","🤿"),SPEARFISHING("Подводен риболов","🤿"),SUP("SUP","🏄"),KAYAK("Морски каяк","🛶"),SMALL_BOAT("Малка лодка","🚤"),SAILING("Ветроходство","⛵"),SWIMMING("Плуване","🏊"),BEACH("Плаж","🏖️")}
data class ActivityPreferences(val skillLevel:String="Начинаещ",val minimumWindMs:Double=3.0,val maximumWindMs:Double=12.0,val maximumWaveM:Double=1.0,val maximumGustMs:Double=16.0)
data class ActivitySuitability(val activityType:MarineActivityType,val destinationId:String,val evaluatedAt:Instant,val level:ActivitySuitabilityLevel,val score:Int,val title:String,val summary:String,val reasons:List<String>,val warnings:List<String>,val sourceType:SuitabilitySourceType)
data class ActivityTimeWindow(val start:Instant,val end:Instant,val minimumScore:Int,val averageScore:Int,val level:ActivitySuitabilityLevel,val warnings:List<String>)
data class ActivityHourlyEvaluation(val forecastAt:Instant,val score:Int,val level:ActivitySuitabilityLevel,val waveHeightM:Double?,val wavePeriodS:Double?,val waveDirectionDeg:Double?,val windSpeedMs:Double?,val windGustMs:Double?,val windDirectionDeg:Double?,val waterTemperatureC:Double?,val airTemperatureC:Double?,val precipitationMm:Double?,val warnings:List<String>)
data class ActivityDailyForecast(val activityType:MarineActivityType,val destinationId:String,val date:java.time.LocalDate,val score:Int,val level:ActivitySuitabilityLevel,val title:String,val summary:String,val bestTimeWindows:List<ActivityTimeWindow>,val hourlyEvaluations:List<ActivityHourlyEvaluation>,val reasons:List<String>,val warnings:List<String>,val sourceType:SuitabilitySourceType)
data class FavoriteActivity(val activityType:MarineActivityType,val addedAt:Instant,val preferredDestinationId:String?=null)
