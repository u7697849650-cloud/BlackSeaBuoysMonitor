package bg.blacksea.buoys.domain.activity

import bg.blacksea.buoys.forecast.MarineForecastPoint
import java.time.Instant

fun suitableWindows(activity:MarineActivityType,forecast:List<MarineForecastPoint>,engine:ActivityEvaluationEngine,preferences:ActivityPreferences=ActivityPreferences()):List<ActivityTimeWindow>{
 val rated=forecast.sortedBy{it.forecastAt}.map{p->p to engine.evaluate(activity,null,listOf(p),preferences)}.filter{it.second.score>=50};if(rated.isEmpty())return emptyList();val groups=mutableListOf<MutableList<Pair<MarineForecastPoint,ActivitySuitability>>>()
 rated.forEach{item->val last=groups.lastOrNull();if(last!=null&&item.first.forecastAt.epochSecond-last.last().first.forecastAt.epochSecond<=3700)last+=item else groups+=mutableListOf(item)}
 return groups.map{g->val scores=g.map{it.second.score};ActivityTimeWindow(g.first().first.forecastAt,g.last().first.forecastAt.plusSeconds(3600),scores.min(),scores.average().toInt(),g.minBy{it.second.score}.second.level,g.flatMap{it.second.warnings}.distinct())}
}
