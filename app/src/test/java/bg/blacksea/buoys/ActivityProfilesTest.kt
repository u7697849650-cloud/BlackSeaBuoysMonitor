package bg.blacksea.buoys

import bg.blacksea.buoys.domain.activity.*
import org.junit.Assert.*
import org.junit.Test
import java.time.*

class ActivityProfilesTest{
 @Test fun everySportHasDifferentFactoryProfile(){val profiles=ActivityProfileFactory.defaults();assertEquals(MarineActivityType.entries.size,profiles.size);assertTrue(profiles.values.all{it.requiredParameters.isNotEmpty()})}
 @Test fun weakWindIsNotGoodForKite(){val p=ActivityProfileFactory.default(MarineActivityType.KITESURFING);assertTrue(activityProfileScore(p,27.0,24.0,.5,8.0,.5,8.0,3.0,4.0,0.0,0.0)<70)}
 @Test fun tinyWavesAreNotGoodForSurf(){val p=ActivityProfileFactory.default(MarineActivityType.SURFING);assertTrue(activityProfileScore(p,27.0,24.0,.2,4.0,.2,4.0,3.0,4.0,0.0,0.0)<70)}
 @Test fun strongWindIsNotGoodForSup(){val p=ActivityProfileFactory.default(MarineActivityType.SUP);assertTrue(activityProfileScore(p,27.0,24.0,.2,5.0,.2,5.0,9.0,12.0,0.0,0.0)<70)}
 @Test fun highWavesAreNotGoodForSwimming(){val p=ActivityProfileFactory.default(MarineActivityType.SWIMMING);assertTrue(activityProfileScore(p,27.0,24.0,1.1,5.0,.5,6.0,4.0,6.0,0.0,0.0)<70)}
 @Test fun fishingAllowsNight(){assertEquals(ActivityTimeMode.DAY_AND_NIGHT,ActivityProfileFactory.default(MarineActivityType.FISHING).timePolicy.defaultMode)}
 @Test fun daytimeProfileExcludesNight(){val date=LocalDate.of(2026,7,18);val zone=ZoneId.of("Europe/Sofia");val p=ActivityProfileFactory.default(MarineActivityType.BEACH).timePolicy;assertFalse(p.includes(date.atTime(2,0).atZone(zone).toInstant(),date,null,zone));assertTrue(p.includes(date.atTime(12,0).atZone(zone).toInstant(),date,null,zone))}
 @Test fun overnightCustomRangeWorks(){val p=ActivityTimePolicy(ActivityTimeMode.CUSTOM_TIME_WINDOW,true,LocalTime.of(20,0),LocalTime.of(5,0));val date=LocalDate.of(2026,7,18);val zone=ZoneId.of("Europe/Sofia");assertTrue(p.includes(date.atTime(23,0).atZone(zone).toInstant(),date,null,zone));assertFalse(p.includes(date.atTime(12,0).atZone(zone).toInstant(),date,null,zone))}
 @Test(expected=IllegalArgumentException::class) fun invalidRangeRejected(){NumericRange(10.0,5.0)}
 @Test fun officialWarningAlwaysBlocks(){val p=ActivityProfileFactory.default(MarineActivityType.BEACH);assertEquals(0,activityProfileScore(p,28.0,25.0,.2,5.0,.2,6.0,2.0,3.0,0.0,0.0,true))}
}
