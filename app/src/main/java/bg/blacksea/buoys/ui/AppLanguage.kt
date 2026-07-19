package bg.blacksea.buoys.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import bg.blacksea.buoys.domain.activity.ActivitySuitabilityLevel
import bg.blacksea.buoys.domain.activity.MarineActivityType
import bg.blacksea.buoys.domain.Freshness

object AppLanguage { var code by mutableStateOf("bg") }
fun tr(bg:String,en:String)=if(AppLanguage.code=="en")en else bg
fun activityTitle(a:MarineActivityType)=if(AppLanguage.code!="en")a.title else when(a){
    MarineActivityType.FISHING->"Fishing";MarineActivityType.KITESURFING->"Kitesurfing";MarineActivityType.WINDSURFING->"Windsurfing";MarineActivityType.SURFING->"Surfing";MarineActivityType.DIVING->"Diving";MarineActivityType.SPEARFISHING->"Spearfishing";MarineActivityType.SUP->"SUP";MarineActivityType.KAYAK->"Sea kayak";MarineActivityType.SMALL_BOAT->"Small boat";MarineActivityType.SAILING->"Sailing";MarineActivityType.SWIMMING->"Swimming";MarineActivityType.BEACH->"Beach"
}
fun levelTitle(l:ActivitySuitabilityLevel)=if(AppLanguage.code!="en")l.title else when(l){
    ActivitySuitabilityLevel.EXCELLENT->"Excellent conditions";ActivitySuitabilityLevel.GOOD->"Good conditions";ActivitySuitabilityLevel.ACCEPTABLE->"Acceptable conditions";ActivitySuitabilityLevel.POOR->"Poor conditions";ActivitySuitabilityLevel.DANGEROUS->"Dangerous conditions";ActivitySuitabilityLevel.NO_DATA->"Insufficient data"
}
fun freshnessTitle(value:Freshness)=if(AppLanguage.code!="en")value.label else when(value){Freshness.CURRENT->"Current";Freshness.STALE->"Stale";Freshness.VERY_OLD->"Very old";Freshness.NO_DATA->"No data";Freshness.CACHED->"Cached data"}
