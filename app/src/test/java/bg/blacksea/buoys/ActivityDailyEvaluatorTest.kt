package bg.blacksea.buoys

import bg.blacksea.buoys.domain.activity.*
import bg.blacksea.buoys.forecast.MarineForecastPoint
import org.junit.Assert.*
import org.junit.Test
import java.time.*

class ActivityDailyEvaluatorTest{
 private val engine=DefaultActivityEvaluationEngine();private val base=LocalDate.now().plusDays(1).atTime(8,0).atZone(ZoneId.of("Europe/Sofia")).toInstant()
 @Test fun `seven days produce seven labeled daily ratings`(){val result=dailyActivityForecasts(MarineActivityType.BEACH,"varna",points(7,8),engine);assertEquals(7,result.size);assertTrue(result.all{it.title.isNotBlank()&&it.score in 0..100})}
 @Test fun `beach without air temperature is not rated green`(){val d=dailyActivityForecasts(MarineActivityType.BEACH,"varna",points(1,4),engine).single();assertTrue(d.bestTimeWindows.isEmpty());assertTrue(d.score<70)}
 @Test fun `single usable hour receives strong penalty`(){val d=dailyActivityForecasts(MarineActivityType.BEACH,"varna",points(1,1),engine).single();assertTrue(d.score<70)}
 @Test fun `daily result exposes hourly values and selected date`(){val d=dailyActivityForecasts(MarineActivityType.SURFING,"varna",points(1,5),engine).single();assertEquals(5,d.hourlyEvaluations.size);assertEquals(base.atZone(ZoneId.of("Europe/Sofia")).toLocalDate(),d.date);assertNotNull(d.hourlyEvaluations.first().waveHeightM)}
 private fun points(days:Int,hours:Int)=buildList{repeat(days){day->repeat(hours){hour->val at=base.plusSeconds(day*86400L+hour*3600L);add(MarineForecastPoint("varna",at,.4,70.0,4.0,.2,70.0,3.0,.2,80.0,5.0,24.0,fetchedAt=base))}}}
}
