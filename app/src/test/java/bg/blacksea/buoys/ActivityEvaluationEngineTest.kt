package bg.blacksea.buoys

import bg.blacksea.buoys.domain.BuoyObservation
import bg.blacksea.buoys.domain.activity.*
import bg.blacksea.buoys.forecast.MarineForecastPoint
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class ActivityEvaluationEngineTest{
 private val engine=DefaultActivityEvaluationEngine();private val now=Instant.now()
 @Test fun `all activities return bounded score`(){MarineActivityType.entries.forEach{val r=engine.evaluate(it,observation(),forecast());assertTrue(r.score in 0..100);assertNotEquals(ActivitySuitabilityLevel.NO_DATA,r.level)}}
 @Test fun `missing critical data is never good`(){val r=engine.evaluate(MarineActivityType.SWIMMING,null,emptyList());assertEquals(ActivitySuitabilityLevel.NO_DATA,r.level)}
 @Test fun `dangerous swimming wave cannot be good`(){val r=engine.evaluate(MarineActivityType.SWIMMING,observation(wave=2.0),emptyList());assertTrue(r.level==ActivitySuitabilityLevel.DANGEROUS||r.level==ActivitySuitabilityLevel.POOR)}
 @Test fun `cached source receives warning and penalty`(){val clean=engine.evaluate(MarineActivityType.FISHING,observation(),forecast());val cached=engine.evaluate(MarineActivityType.FISHING,observation(cached=true),forecast());assertTrue(cached.score<clean.score);assertTrue(cached.warnings.any{it.contains("кеширани")})}
 @Test fun `old measurements do not produce positive rating`(){val r=engine.evaluate(MarineActivityType.SUP,observation(at=now.minusSeconds(26*3600)),forecast());assertEquals(ActivitySuitabilityLevel.NO_DATA,r.level)}
 @Test fun `personal boat threshold is enforced`(){val r=engine.evaluate(MarineActivityType.SMALL_BOAT,observation(wave=1.1),forecast(),ActivityPreferences(maximumWaveM=.5));assertTrue(r.score<25);assertTrue(r.warnings.any{it.contains("максимум")})}
 @Test fun `consecutive suitable hours merge into window`(){val points=(1..4).map{i->forecastPoint(now.plusSeconds(i*3600L),.3)};val windows=suitableWindows(MarineActivityType.BEACH,points,engine);assertEquals(1,windows.size);assertEquals(4*3600,windows.single().end.epochSecond-windows.single().start.epochSecond)}
 private fun observation(wave:Double=.3,cached:Boolean=false,at:Instant=now)=BuoyObservation("varna","Варна",at,24.0,wave,.5,4.0,70.0,4.0,80.0,43.2,27.9,"test",now,cached)
 private fun forecast()=listOf(forecastPoint(now.plusSeconds(3600),.3))
 private fun forecastPoint(at:Instant,wave:Double)=MarineForecastPoint("varna",at,wave,70.0,4.0,.2,70.0,3.0,.2,80.0,5.0,24.0,fetchedAt=now)
}
