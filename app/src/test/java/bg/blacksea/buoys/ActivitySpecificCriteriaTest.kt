package bg.blacksea.buoys

import bg.blacksea.buoys.domain.activity.*
import org.junit.Assert.*
import org.junit.Test

class ActivitySpecificCriteriaTest{
 @Test fun beachWithoutAirIsNotGreen(){assertTrue(activitySpecificScore(MarineActivityType.BEACH,82,null,24.0,.2,null)<70)}
 @Test fun coldBeachIsPoor(){assertTrue(activitySpecificScore(MarineActivityType.BEACH,82,15.0,17.0,.2,null)<50)}
 @Test fun comfortableBeachCanBeGood(){assertTrue(activitySpecificScore(MarineActivityType.BEACH,82,27.0,24.0,.2,null)>=70)}
 @Test fun warmerComfortableBeachScoresAboveCoolDay(){val cool=activitySpecificScore(MarineActivityType.BEACH,82,23.0,24.0,.2,null);val warm=activitySpecificScore(MarineActivityType.BEACH,82,27.0,24.0,.2,null);assertTrue(cool<50);assertTrue(warm>=70)}
 @Test fun swimmingPenalizesColdSea(){assertTrue(activitySpecificScore(MarineActivityType.SWIMMING,82,25.0,15.0,.2,null)<50)}
 @Test fun windSportsWithoutForecastWindAreCapped(){assertTrue(activitySpecificScore(MarineActivityType.KITESURFING,82,25.0,24.0,.3,null)<70)}
}
