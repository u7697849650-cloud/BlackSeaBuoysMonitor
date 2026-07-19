package bg.blacksea.buoys.forecast

import android.content.Context
import bg.blacksea.buoys.data.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.temporal.ChronoUnit

data class ForecastState(val points:List<MarineForecastPoint> = emptyList(),val loading:Boolean=false,val error:String?=null,val cached:Boolean=false)
class DestinationCatalog(context:Context){
    val destinations:List<ForecastDestination> = Json.decodeFromString(context.assets.open("marine_destinations.json").bufferedReader().use{it.readText()})
    fun byId(id:String):ForecastDestination {
        val key=id.lowercase().replace("-","").replace(" ","")
        return destinations.firstOrNull{it.id==id} ?: when {
            "шабла" in key -> named("shabla"); "калиакра" in key -> named("kaliakra"); "балчик" in key -> named("balchik")
            "варна" in key -> named("varna"); "шкорпиловци" in key -> named("shkorpilovtsi"); "обзор" in key -> named("obzor")
            "несебър" in key -> named("nesebar"); "бургас" in key -> named("burgas"); "созопол" in key -> named("sozopol")
            "приморско" in key -> named("primorsko"); "ахтопол" in key -> named("ahtopol"); "синеморец" in key -> named("sinemorets")
            else -> named("varna")
        }
    }
    private fun named(id:String)=destinations.first{it.id==id}
}
class MarineForecastRepository(private val live:MarineForecastProvider,private val dao:BuoyDao){
    private val states=mutableMapOf<String,MutableStateFlow<ForecastState>>()
    fun observe(id:String):StateFlow<ForecastState> = states.getOrPut(id){MutableStateFlow(ForecastState())}.asStateFlow()
    suspend fun refresh(destination:ForecastDestination,days:Int=7){val state=states.getOrPut(destination.id){MutableStateFlow(ForecastState())};state.update{it.copy(loading=true,error=null)};live.getHourlyForecast(destination,days).fold({points->dao.insertForecast(points.map{it.entity()});dao.deleteOldForecast(Instant.now().minus(2,ChronoUnit.DAYS).toString());state.value=ForecastState(points=points,loading=false)},{e->val cached=dao.forecast(destination.id).first().map{it.domain(true)};state.value=ForecastState(cached,false,"Прогнозата не може да бъде обновена. Показват се последно запазените прогнозни данни.",true)})}
}
