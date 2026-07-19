package bg.blacksea.buoys

import bg.blacksea.buoys.forecast.*
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class MarineForecastTest{
 @Test fun `DTO accepts arrays with different lengths and missing values`(){val dto=Json.decodeFromString<OpenMeteoMarineResponse>("""{"timezone":"Europe/Sofia","hourly":{"time":["2026-07-18T12:00","2026-07-18T13:00"],"wave_height":[0.4],"wave_direction":[null,72],"sea_surface_temperature":[25.1,25.2]}}""");val p=OpenMeteoMarineForecastProvider().parse(dto,ForecastDestination("varna","Варна",43.2,27.9));assertEquals(2,p.size);assertNull(p[1].waveHeightM);assertEquals(72.0,p[1].waveDirectionDeg!!,.01)}
 @Test fun `invalid dates are skipped safely`(){val dto=Json.decodeFromString<OpenMeteoMarineResponse>("""{"hourly":{"time":["bad","2026-07-18T13:00"],"wave_height":[9,0.5]}}""");val p=OpenMeteoMarineForecastProvider().parse(dto,ForecastDestination("v","Варна",1.0,1.0));assertEquals(1,p.size);assertEquals(.5,p[0].waveHeightM!!,.01)}
 @Test fun `sixteen compass directions normalize values`(){assertEquals("С",degreesToCompassDirection(360.0));assertEquals("ИСИ",degreesToCompassDirection(72.0));assertEquals("СЗ",degreesToCompassDirection(-45.0));assertEquals("Няма данни",degreesToCompassDirection(null))}
 @Test fun `nearest future never returns past point`(){val now=Instant.parse("2026-07-18T10:00:00Z");val list=listOf(point(now.minusSeconds(1)),point(now.plusSeconds(7200)),point(now.plusSeconds(3600)));assertEquals(now.plusSeconds(3600),list.nearestFuture(now)?.forecastAt)}
 @Test fun `daily summary uses real points`(){val base=Instant.parse("2026-07-18T10:00:00Z");val result=listOf(point(base,.4,23.0),point(base.plusSeconds(3600),.8,25.0)).days();assertEquals(.8,result.single().maximumWaveHeightM!!,.01);assertEquals(23.0,result.single().minimumSeaTemperatureC!!,.01);assertEquals(25.0,result.single().maximumSeaTemperatureC!!,.01)}
 private fun point(at:Instant,wave:Double=.4,temp:Double=24.0)=MarineForecastPoint("v",at,wave,72.0,4.0,null,null,null,null,null,null,temp,fetchedAt=at)
}
