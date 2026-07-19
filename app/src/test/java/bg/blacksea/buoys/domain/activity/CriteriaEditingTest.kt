package bg.blacksea.buoys.domain.activity
import org.junit.Assert.*
import org.junit.Test
class CriteriaEditingTest{
 @Test fun localizedNumbers(){assertEquals(2.5,parseLocalizedDecimal("2,5")!!,.001);assertEquals(2.5,parseLocalizedDecimal("2.5")!!,.001);assertNull(parseLocalizedDecimal("x"))}
 @Test fun sliderMath(){assertEquals(49,calculateSliderSteps(0.0,5.0,.1));assertEquals(1.3,snapToStep(1.26,0.0,5.0,.1),.001)}
 @Test fun codecRoundTrip(){val p=UserActivityProfile(MarineActivityType.BEACH,experienceLevel=ExperienceLevel.CUSTOM,thresholds=ActivityProfileFactory.default(MarineActivityType.BEACH).thresholds.copy(airTemperatureCelsius=NumericRange(27.0,32.0)));val d=UserActivityProfileCodec.decode(UserActivityProfileCodec.encode(p))!!;assertEquals(NumericRange(27.0,32.0),d.thresholds.airTemperatureCelsius)}
 @Test fun everySportEditable(){MarineActivityType.entries.forEach{assertTrue(ActivityCriterionDefinitions.forActivity(it).isNotEmpty())}}
}
