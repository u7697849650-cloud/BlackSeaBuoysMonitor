package bg.blacksea.buoys.domain.activity

import org.junit.Assert.*
import org.junit.Test

class ActivitySafetyRegressionTest{
 private fun evaluate(type:MarineActivityType,wave:Double=.1,wind:Double=3.3,gust:Double=8.7,sea:Double=26.0,thunder:Double=0.0,warning:Boolean=false)=evaluateActivityConditions(ActivityProfileFactory.factoryDefault(type),mapOf(MarineParameter.WAVE_HEIGHT to wave,MarineParameter.WAVE_PERIOD to 3.3,MarineParameter.SWELL_HEIGHT to wave,MarineParameter.SWELL_PERIOD to 3.3,MarineParameter.WIND_SPEED to wind,MarineParameter.WIND_GUST to gust,MarineParameter.SEA_TEMPERATURE to sea,MarineParameter.AIR_TEMPERATURE to 26.0,MarineParameter.APPARENT_TEMPERATURE to 26.0,MarineParameter.THUNDERSTORM_PROBABILITY to thunder,MarineParameter.PRECIPITATION to 0.0),warning)
 @Test fun `SUP with calm sea is not dangerous`(){assertNotEquals(ActivitySafetyLevel.DANGEROUS,evaluate(MarineActivityType.SUP).safetyLevel)}
 @Test fun `dangerously strong SUP wind is dangerous`(){assertEquals(ActivitySafetyLevel.DANGEROUS,evaluate(MarineActivityType.SUP,wind=13.0,gust=16.0).safetyLevel)}
 @Test fun `weak sailing wind is poor but normal`(){val r=evaluate(MarineActivityType.SAILING);assertEquals(SportSuitability.POOR,r.suitability);assertEquals(ActivitySafetyLevel.NORMAL,r.safetyLevel);assertTrue(r.primaryMessage.contains("Слаб вятър"))}
 @Test fun `dangerously strong sailing wind is dangerous`(){assertEquals(ActivitySafetyLevel.DANGEROUS,evaluate(MarineActivityType.SAILING,wind=18.0,gust=23.0).safetyLevel)}
 @Test fun `weak kitesurf wind is not dangerous`(){val r=evaluate(MarineActivityType.KITESURFING);assertNotEquals(ActivitySafetyLevel.DANGEROUS,r.safetyLevel);assertTrue(r.primaryMessage.contains("Недостатъчен вятър"))}
 @Test fun `small surf waves are poor but normal`(){val r=evaluate(MarineActivityType.SURFING);assertEquals(ActivitySafetyLevel.NORMAL,r.safetyLevel);assertTrue(r.primaryMessage.contains("Малки вълни"))}
 @Test fun `dangerous surf waves are dangerous`(){assertEquals(ActivitySafetyLevel.DANGEROUS,evaluate(MarineActivityType.SURFING,wave=3.6).safetyLevel)}
 @Test fun `diving in calm warm sea is not dangerous`(){assertEquals(ActivitySafetyLevel.NORMAL,evaluate(MarineActivityType.DIVING).safetyLevel)}
 @Test fun `below preferred minimum is suitability only`(){val r=evaluate(MarineActivityType.KITESURFING);assertTrue(r.criteria.any{it.result==CriterionResult.BELOW_USEFUL&&!it.blocking&&!it.safetyReason})}
 @Test fun `above preferred is not automatically dangerous`(){assertNotEquals(ActivitySafetyLevel.DANGEROUS,evaluate(MarineActivityType.SUP,wave=.5).safetyLevel)}
 @Test fun `official warning is always dangerous`(){assertEquals(ActivitySafetyLevel.DANGEROUS,evaluate(MarineActivityType.SUP,warning=true).safetyLevel)}
}
