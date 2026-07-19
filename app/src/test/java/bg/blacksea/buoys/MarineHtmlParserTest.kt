package bg.blacksea.buoys

import bg.blacksea.buoys.data.MarineDataException
import bg.blacksea.buoys.data.MarineHtmlParser
import bg.blacksea.buoys.domain.Freshness
import bg.blacksea.buoys.domain.freshness
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class MarineHtmlParserTest {
    private val parser=MarineHtmlParser()
    @Test fun `valid Bulgarian table is parsed`() { val r=parser.parse(table("Станция","Дата UTC","Температура вода","Hm0","Варна","18.07.2026 09:00","23,4 °C","0.42 m"),"test").single();assertEquals(23.4,r.waterTemperatureC!!,.001);assertEquals(.42,r.significantWaveHeightM!!,.001) }
    @Test fun `changed column order is supported`() { val r=parser.parse(table("Hm0","Станция","Дата UTC","0,55","Ахтопол","18.07.2026 08:00"),"test").single();assertEquals("Ахтопол",r.stationName);assertEquals(.55,r.significantWaveHeightM!!,.001) }
    @Test(expected=MarineDataException.Structure::class) fun `missing key columns is explicit`() { parser.parse(table("Име","Стойност","x","1"),"test") }
    @Test fun `empty and invalid values become null`() { val r=parser.parse(table("Станция","Дата UTC","Температура","Hm0","Варна","18.07.2026 09:00","","abc"),"test").single();assertNull(r.waterTemperatureC);assertNull(r.significantWaveHeightM) }
    @Test fun `freshness boundaries`() { val now=Instant.parse("2026-07-18T12:00:00Z");assertEquals(Freshness.CURRENT,freshness(now.minusSeconds(3*3600),now));assertEquals(Freshness.STALE,freshness(now.minusSeconds(4*3600),now));assertEquals(Freshness.VERY_OLD,freshness(now.minusSeconds(25*3600),now));assertEquals(Freshness.CACHED,freshness(now,now,true)) }
    @Test fun `real source headers and malformed loose cell groups are parsed`() {
        val html="""<table><thead><tr><th>Буй</th><th>Дата/час (GMT)</th><th>Значима височина на вълната (Hm0) (м)</th><th>От посока (MeanDir) (°)</th><th>Макс. височина (Hmax) (м)</th><th>Период (t02) (сек.)</th><th>Пик период (tp)</th><th>От посока (DirTp)</th><th>Темп. морска вода (°C)</th><th>Скорост на вятъра (WSp) (м/с)</th><th>Вятър от посока</th></tr></thead><tbody><tr><td>Бургас-залив</td><td>18/07/2026 08:00:00</td><td>0.09 / 1</td><td>78°</td><td>0.20 / 2</td><td>2.2</td><td>2.8</td><td>78°</td><td>25.7°</td><td>2.0 / 2</td><td>85°</td></tr><td>Ахтопол</td><td>18/07/2026 06:32:01</td><td>0.18 / 2</td><td>29°</td><td></td><td>2.8</td><td>3.8</td><td>28°</td><td>25.6°</td><td>0.4 / 0</td><td>5°</td></tbody></table>"""
        val result=parser.parse(html,"live")
        assertTrue(result.isNotEmpty()); assertEquals("Бургас-залив",result.first().stationName);assertEquals(25.7,result.first().waterTemperatureC!!,.001);assertNotNull(result.first().measuredAtUtc)
    }
    private fun table(vararg cells:String):String { val half=cells.size/2;return "<table><tr>${cells.take(half).joinToString(""){"<th>$it</th>"}}</tr><tr>${cells.drop(half).joinToString(""){"<td>$it</td>"}}</tr></table>" }
}
