package bg.blacksea.buoys.domain.activity

import java.time.Instant
import java.time.LocalTime
import kotlin.math.round

enum class CriterionEditorType{RANGE,MINIMUM,MAXIMUM}
data class ActivityCriterionDefinition(val parameter:MarineParameter,val titleBg:String,val titleEn:String,val editorType:CriterionEditorType,val controlMinimum:Double,val controlMaximum:Double,val step:Double,val unit:String,val factoryRange:NumericRange?,val supportingBg:String?=null,val supportingEn:String?=null,val required:Boolean=false)
data class UserActivityProfile(val activityType:MarineActivityType,val experienceLevel:ExperienceLevel=ExperienceLevel.INTERMEDIATE,val timeMode:ActivityTimeMode=ActivityTimeMode.DAYLIGHT_ONLY,val customStartTime:LocalTime?=null,val customEndTime:LocalTime?=null,val includeNightHours:Boolean=false,val thresholds:ActivityThresholds=ActivityProfileFactory.factoryDefault(activityType).thresholds,val useFactoryDefaults:Boolean=true,val updatedAt:Instant=Instant.now())

fun calculateSliderSteps(min:Double,max:Double,step:Double)=((max-min)/step).toInt().minus(1).coerceAtLeast(0)
fun snapToStep(value:Double,min:Double,max:Double,step:Double)=(min+round((value-min)/step)*step).coerceIn(min,max)
fun parseLocalizedDecimal(value:String):Double?=value.trim().replace(',','.').toDoubleOrNull()

object ActivityCriterionDefinitions{
 fun forActivity(type:MarineActivityType):List<ActivityCriterionDefinition>{val t=ActivityProfileFactory.default(type).thresholds;return buildList{
  fun add(p:MarineParameter,bg:String,en:String,min:Double,max:Double,step:Double,unit:String,r:NumericRange?){if(r!=null)add(ActivityCriterionDefinition(p,bg,en,CriterionEditorType.RANGE,min,max,step,unit,r,required=p in ActivityProfileFactory.default(type).requiredParameters))}
  add(MarineParameter.AIR_TEMPERATURE,"Температура на въздуха","Air temperature",-10.0,45.0,.5,"°C",t.airTemperatureCelsius)
  add(MarineParameter.SEA_TEMPERATURE,"Температура на морето","Sea temperature",0.0,35.0,.5,"°C",t.seaTemperatureCelsius)
  add(MarineParameter.WAVE_HEIGHT,"Височина на вълните","Wave height",0.0,5.0,.1,"m",t.waveHeightMeters)
  add(MarineParameter.WAVE_PERIOD,"Интервал на вълните","Wave period",2.0,20.0,.5,"s",t.wavePeriodSeconds)
  add(MarineParameter.SWELL_HEIGHT,"Мъртво вълнение","Swell height",0.0,5.0,.1,"m",t.swellHeightMeters)
  add(MarineParameter.SWELL_PERIOD,"Интервал на мъртвото вълнение","Swell period",2.0,20.0,.5,"s",t.swellPeriodSeconds)
  add(MarineParameter.WIND_SPEED,"Среден вятър","Average wind",0.0,25.0,.5,"m/s",t.windSpeedMetersPerSecond)
  add(MarineParameter.WIND_GUST,"Пориви","Wind gusts",0.0,35.0,.5,"m/s",t.windGustMetersPerSecond)
  add(MarineParameter.PRECIPITATION,"Валеж","Precipitation",0.0,20.0,.1,"mm/h",t.precipitationMillimetersPerHour)
  add(MarineParameter.THUNDERSTORM_PROBABILITY,"Вероятност за гръмотевици","Thunderstorm probability",0.0,100.0,5.0,"%",t.thunderstormProbabilityPercent)
 }}
}

object UserActivityProfilesRuntime{var profiles:Map<MarineActivityType,UserActivityProfile> = emptyMap();fun effective(type:MarineActivityType)=ActivityProfileFactory.default(type)}

object UserActivityProfileCodec{
 fun encode(p:UserActivityProfile):String{val values=ActivityCriterionDefinitions.forActivity(p.activityType).joinToString(","){d->val r=p.thresholds.range(d.parameter);"${d.parameter.name}:${r?.minimum?:""}:${r?.maximum?:""}"};return listOf("2",p.activityType.name,p.experienceLevel.name,p.timeMode.name,p.customStartTime?:"",p.customEndTime?:"",p.includeNightHours,values).joinToString("|")}
 fun decode(s:String):UserActivityProfile?=runCatching{val x=s.split('|');val type=MarineActivityType.valueOf(x[1]);var t=ActivityProfileFactory.default(type).thresholds;x.getOrNull(7).orEmpty().split(',').filter{it.isNotBlank()}.forEach{v->val q=v.split(':');t=t.withRange(MarineParameter.valueOf(q[0]),NumericRange(q.getOrNull(1)?.toDoubleOrNull(),q.getOrNull(2)?.toDoubleOrNull()))};UserActivityProfile(type,ExperienceLevel.valueOf(x[2]),ActivityTimeMode.valueOf(x[3]),x[4].takeIf{it.isNotBlank()}?.let(LocalTime::parse),x[5].takeIf{it.isNotBlank()}?.let(LocalTime::parse),x[6].toBoolean(),t,false)}.getOrNull()
}

fun ActivityThresholds.range(p:MarineParameter)=when(p){MarineParameter.AIR_TEMPERATURE,MarineParameter.APPARENT_TEMPERATURE->airTemperatureCelsius;MarineParameter.SEA_TEMPERATURE->seaTemperatureCelsius;MarineParameter.WAVE_HEIGHT->waveHeightMeters;MarineParameter.WAVE_PERIOD->wavePeriodSeconds;MarineParameter.SWELL_HEIGHT->swellHeightMeters;MarineParameter.SWELL_PERIOD->swellPeriodSeconds;MarineParameter.WIND_SPEED->windSpeedMetersPerSecond;MarineParameter.WIND_GUST->windGustMetersPerSecond;MarineParameter.PRECIPITATION->precipitationMillimetersPerHour;MarineParameter.THUNDERSTORM_PROBABILITY->thunderstormProbabilityPercent;MarineParameter.VISIBILITY->visibilityKilometers;MarineParameter.UV_INDEX->uvIndex;MarineParameter.CURRENT_SPEED->currentSpeedMetersPerSecond;else->null}
fun ActivityThresholds.withRange(p:MarineParameter,r:NumericRange)=when(p){MarineParameter.AIR_TEMPERATURE,MarineParameter.APPARENT_TEMPERATURE->copy(airTemperatureCelsius=r);MarineParameter.SEA_TEMPERATURE->copy(seaTemperatureCelsius=r);MarineParameter.WAVE_HEIGHT->copy(waveHeightMeters=r);MarineParameter.WAVE_PERIOD->copy(wavePeriodSeconds=r);MarineParameter.SWELL_HEIGHT->copy(swellHeightMeters=r);MarineParameter.SWELL_PERIOD->copy(swellPeriodSeconds=r);MarineParameter.WIND_SPEED->copy(windSpeedMetersPerSecond=r);MarineParameter.WIND_GUST->copy(windGustMetersPerSecond=r);MarineParameter.PRECIPITATION->copy(precipitationMillimetersPerHour=r);MarineParameter.THUNDERSTORM_PROBABILITY->copy(thunderstormProbabilityPercent=r);MarineParameter.VISIBILITY->copy(visibilityKilometers=r);MarineParameter.UV_INDEX->copy(uvIndex=r);MarineParameter.CURRENT_SPEED->copy(currentSpeedMetersPerSecond=r);else->this}
