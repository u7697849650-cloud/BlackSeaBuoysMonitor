package bg.blacksea.buoys

import bg.blacksea.buoys.domain.*
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class DataProvenanceTest{
 @Test fun forecastIsNotMeasurement(){val model=TemperatureUiModel("Температура на въздуха","26 °C",ValueOrigin.FORECAST,"Open-Meteo","14:00");assertNotEquals(ValueOrigin.MEASURED,model.origin)}
 @Test fun missingIsDashNotZero(){val model=TemperatureUiModel("Температура на въздуха","—",ValueOrigin.UNAVAILABLE);assertEquals("—",model.formattedValue);assertFalse(model.formattedValue.contains("0 °C"))}
 @Test fun sourcesRemainSeparate(){val now=Instant.parse("2026-07-18T12:00:00Z");val station=DataSourceInfo("station","Морска станция",DataSourceType.OBSERVATION_STATION,fetchedAt=now,isForecast=false);val weather=DataSourceInfo("weather","Open-Meteo Weather",DataSourceType.WEATHER_FORECAST_MODEL,fetchedAt=now,isForecast=true);assertFalse(station.isForecast);assertTrue(weather.isForecast);assertNotEquals(station.type,weather.type)}
 @Test fun officialAuthorityDiffersFromEstimate(){assertNotEquals(WarningAuthority.OFFICIAL_AUTHORITY,WarningAuthority.APPLICATION_ESTIMATE)}
}
