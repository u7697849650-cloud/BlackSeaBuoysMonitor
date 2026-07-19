package bg.blacksea.buoys.data

import android.content.Context
import androidx.room.*
import bg.blacksea.buoys.domain.BuoyObservation
import bg.blacksea.buoys.forecast.MarineForecastPoint
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Entity(tableName="observations", indices=[Index(value=["stationId","measuredAtUtc"], unique=true)])
data class ObservationEntity(@PrimaryKey(autoGenerate=true) val rowId: Long=0, val stationId:String, val stationName:String, val measuredAtUtc:String,
    val waterTemperatureC:Double?, val significantWaveHeightM:Double?, val maximumWaveHeightM:Double?, val wavePeriodS:Double?, val waveDirectionDeg:Double?,
    val windSpeedMs:Double?, val windDirectionDeg:Double?, val latitude:Double?, val longitude:Double?, val sourceUrl:String, val receivedAtUtc:String)
@Entity(tableName="favorites") data class FavoriteEntity(@PrimaryKey val stationId:String)
@Entity(tableName="marine_forecast",indices=[Index(value=["destinationId","forecastAt"],unique=true)]) data class MarineForecastEntity(@PrimaryKey(autoGenerate=true)val id:Long=0,val destinationId:String,val forecastAt:String,val waveHeightM:Double?,val waveDirectionDeg:Double?,val wavePeriodS:Double?,val windWaveHeightM:Double?,val windWaveDirectionDeg:Double?,val windWavePeriodS:Double?,val swellWaveHeightM:Double?,val swellWaveDirectionDeg:Double?,val swellWavePeriodS:Double?,val seaSurfaceTemperatureC:Double?,val fetchedAt:String)

@Dao interface BuoyDao {
    @Insert(onConflict=OnConflictStrategy.IGNORE) suspend fun insert(items:List<ObservationEntity>)
    @Query("SELECT * FROM observations ORDER BY measuredAtUtc DESC") fun all():Flow<List<ObservationEntity>>
    @Query("SELECT * FROM observations WHERE stationId=:id ORDER BY measuredAtUtc DESC LIMIT 1") suspend fun latest(id:String):ObservationEntity?
    @Query("SELECT * FROM observations WHERE stationId=:id AND measuredAtUtc>=:since ORDER BY measuredAtUtc") fun history(id:String,since:String):Flow<List<ObservationEntity>>
    @Query("SELECT stationId FROM favorites") fun favorites():Flow<List<String>>
    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun favorite(item:FavoriteEntity)
    @Query("DELETE FROM favorites WHERE stationId=:id") suspend fun unfavorite(id:String)
    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun insertForecast(items:List<MarineForecastEntity>)
    @Query("SELECT * FROM marine_forecast WHERE destinationId=:id ORDER BY forecastAt") fun forecast(id:String):Flow<List<MarineForecastEntity>>
    @Query("DELETE FROM marine_forecast WHERE forecastAt<:before") suspend fun deleteOldForecast(before:String)
}
@Database(entities=[ObservationEntity::class,FavoriteEntity::class,MarineForecastEntity::class],version=2,exportSchema=false)
abstract class BuoyDatabase:RoomDatabase(){ abstract fun dao():BuoyDao
    companion object { fun create(c:Context)=Room.databaseBuilder(c,BuoyDatabase::class.java,"buoys.db").fallbackToDestructiveMigration().build() }
}
fun BuoyObservation.entity()= measuredAtUtc?.let { ObservationEntity(stationId=stationId,stationName=stationName,measuredAtUtc=it.toString(),waterTemperatureC=waterTemperatureC,
    significantWaveHeightM=significantWaveHeightM,maximumWaveHeightM=maximumWaveHeightM,wavePeriodS=wavePeriodS,waveDirectionDeg=waveDirectionDeg,
    windSpeedMs=windSpeedMs,windDirectionDeg=windDirectionDeg,latitude=latitude,longitude=longitude,sourceUrl=sourceUrl,receivedAtUtc=receivedAtUtc.toString()) }
fun ObservationEntity.domain(cached:Boolean=true)=BuoyObservation(stationId,stationName,Instant.parse(measuredAtUtc),waterTemperatureC,significantWaveHeightM,
    maximumWaveHeightM,wavePeriodS,waveDirectionDeg,windSpeedMs,windDirectionDeg,latitude,longitude,sourceUrl,Instant.parse(receivedAtUtc),cached)
fun MarineForecastPoint.entity()=MarineForecastEntity(destinationId=destinationId,forecastAt=forecastAt.toString(),waveHeightM=waveHeightM,waveDirectionDeg=waveDirectionDeg,wavePeriodS=wavePeriodS,windWaveHeightM=windWaveHeightM,windWaveDirectionDeg=windWaveDirectionDeg,windWavePeriodS=windWavePeriodS,swellWaveHeightM=swellWaveHeightM,swellWaveDirectionDeg=swellWaveDirectionDeg,swellWavePeriodS=swellWavePeriodS,seaSurfaceTemperatureC=seaSurfaceTemperatureC,fetchedAt=fetchedAt.toString())
fun MarineForecastEntity.domain(cached:Boolean)=MarineForecastPoint(destinationId,Instant.parse(forecastAt),waveHeightM,waveDirectionDeg,wavePeriodS,windWaveHeightM,windWaveDirectionDeg,windWavePeriodS,swellWaveHeightM,swellWaveDirectionDeg,swellWavePeriodS,seaSurfaceTemperatureC,fetchedAt=Instant.parse(fetchedAt),isCached=cached)
