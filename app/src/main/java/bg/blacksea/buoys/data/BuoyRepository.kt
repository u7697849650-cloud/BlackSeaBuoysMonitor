package bg.blacksea.buoys.data

import bg.blacksea.buoys.domain.*
import kotlinx.coroutines.flow.*

data class MarineSnapshot(val observations:List<BuoyObservation> = emptyList(), val error:String?=null, val refreshing:Boolean=false)
class BuoyRepository(private val provider:MarineDataProvider, private val dao:BuoyDao){
    private val state=MutableStateFlow(MarineSnapshot(refreshing=true)); val snapshot:StateFlow<MarineSnapshot> = state.asStateFlow()
    val favorites=dao.favorites()
    suspend fun refresh(){
        state.update{it.copy(refreshing=true,error=null)}
        provider.getLatestObservations().fold(onSuccess={items->
            dao.insert(items.mapNotNull{it.entity()})
            // The source contains history and sometimes repeats the same station/time.
            // Keep all unique observations in Room, but expose exactly one newest card per station.
            val latest = items
                .filter { it.measuredAtUtc != null }
                .distinctBy { it.stationId to it.measuredAtUtc }
                .groupBy { it.stationId }
                .mapNotNull { (_, observations) -> observations.maxByOrNull { it.measuredAtUtc!! } }
                .sortedBy { it.stationName }
            state.value=MarineSnapshot(latest)
        },onFailure={e->
            val cached=stateFromCache(); state.value=MarineSnapshot(cached,e.message?:"Неуспешно обновяване")
        })
    }
    private suspend fun stateFromCache():List<BuoyObservation> = dao.all().first().distinctBy{it.stationId}.map{it.domain()}.sortedBy { it.stationName }
    suspend fun toggleFavorite(id:String,current:Boolean){if(current)dao.unfavorite(id) else dao.favorite(FavoriteEntity(id))}
    fun history(id:String,since:String)=dao.history(id,since).map{list->list.map{it.domain(false)}}
}
