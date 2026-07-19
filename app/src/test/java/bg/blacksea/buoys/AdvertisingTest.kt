package bg.blacksea.buoys

import bg.blacksea.buoys.advertising.*
import bg.blacksea.buoys.domain.activity.MarineActivityType
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class AdvertisingTest{
 private val now=Instant.parse("2026-07-18T12:00:00Z")
 private fun campaign(format:AdvertisingFormat=AdvertisingFormat.COMPACT_BANNER,priority:Int=1,placement:AdPlacement=AdPlacement.HOME_BOTTOM)=SponsoredCampaign("c$priority",true,format,"Реклама",imageUrl="https://example.com/a.webp",actionUrl="https://example.com",placements=setOf(placement),priority=priority)
 private fun config()=AdvertisingConfiguration(advertisingEnabled=true,compactBannersEnabled=true,sponsoredCardsEnabled=true,backgroundAdsEnabled=true)
 @Test fun `advertising is disabled by default`(){assertNull(selectCampaign(listOf(campaign()),AdvertisingConfiguration(),UserAdvertisingPreferences(),AdPlacement.HOME_BOTTOM,now=now))}
 @Test fun `user can disable all advertising`(){assertNull(selectCampaign(listOf(campaign()),config(),UserAdvertisingPreferences(allAdsEnabled=false),AdPlacement.HOME_BOTTOM,now=now))}
 @Test fun `background requires explicit user opt in`(){assertNull(selectCampaign(listOf(campaign(AdvertisingFormat.BACKGROUND_AD,placement=AdPlacement.HOME_BACKGROUND)),config(),UserAdvertisingPreferences(),AdPlacement.HOME_BACKGROUND,now=now))}
 @Test fun `effective opacity always uses lower clamped value`(){val c=campaign(AdvertisingFormat.BACKGROUND_AD).copy(backgroundOpacity=.15f);assertEquals(.08f,effectiveOpacity(c,UserAdvertisingPreferences(backgroundOpacity=.08f),config()),.001f);assertEquals(.03f,effectiveOpacity(c,UserAdvertisingPreferences(backgroundOpacity=0f),config()),.001f)}
 @Test fun `production opacity never exceeds twenty percent`(){val c=campaign(AdvertisingFormat.BACKGROUND_AD).copy(backgroundOpacity=.8f);assertEquals(.2f,effectiveOpacity(c,UserAdvertisingPreferences(backgroundOpacity=.9f),config()),.001f)}
 @Test fun `only https links without credentials are accepted`(){assertTrue(safeAdvertisingUrl("https://example.com/ad"));assertFalse(safeAdvertisingUrl("http://example.com"));assertFalse(safeAdvertisingUrl("javascript:alert(1)"));assertFalse(safeAdvertisingUrl("https://user:pass@example.com"));assertFalse(safeAdvertisingUrl(null))}
 @Test fun `expired and future campaigns are rejected`(){assertNull(selectCampaign(listOf(campaign().copy(endsAt=now.minusSeconds(1))),config(),UserAdvertisingPreferences(),AdPlacement.HOME_BOTTOM,now=now));assertNull(selectCampaign(listOf(campaign().copy(startsAt=now.plusSeconds(1))),config(),UserAdvertisingPreferences(),AdPlacement.HOME_BOTTOM,now=now))}
 @Test fun `placement destination and sport filters apply`(){val c=campaign().copy(destinationIds=setOf("varna"),activityTypes=setOf(MarineActivityType.SURFING));assertNull(selectCampaign(listOf(c),config(),UserAdvertisingPreferences(),AdPlacement.FORECAST_BOTTOM,"varna",MarineActivityType.SURFING,now=now));assertNull(selectCampaign(listOf(c),config(),UserAdvertisingPreferences(),AdPlacement.HOME_BOTTOM,"burgas",MarineActivityType.SURFING,now=now));assertNotNull(selectCampaign(listOf(c),config(),UserAdvertisingPreferences(),AdPlacement.HOME_BOTTOM,"varna",MarineActivityType.SURFING,now=now))}
 @Test fun `highest priority wins deterministically`(){assertEquals("c9",selectCampaign(listOf(campaign(priority=2),campaign(priority=9)),config(),UserAdvertisingPreferences(),AdPlacement.HOME_BOTTOM,now=now)?.id)}
 @Test fun `dismissal suppresses campaign for configured duration`(){val p=UserAdvertisingPreferences(dismissedCampaigns=mapOf("c1" to now.minusSeconds(3600)));assertNull(selectCampaign(listOf(campaign()),config(),p,AdPlacement.HOME_BOTTOM,now=now))}
 @Test fun `session enforces impression cap and interval`(){var clock=now;val s=AdvertisingSession{clock};val c=campaign().copy(minimumIntervalMinutes=30,maxImpressionsPerSession=2);assertTrue(s.canShow(c));s.record(c);assertFalse(s.canShow(c));clock=clock.plusSeconds(31*60);assertTrue(s.canShow(c));s.record(c);clock=clock.plusSeconds(31*60);assertFalse(s.canShow(c))}
 @Test fun `invalid campaign entries do not break valid JSON document`(){val json="""{"campaigns":[{"id":"bad","format":"NOPE","title":"x"},{"id":"ok","enabled":true,"format":"COMPACT_BANNER","title":"ok","placements":["HOME_BOTTOM"]}]}""";val result=parseCampaigns(json);assertEquals(1,result.size);assertEquals("ok",result.single().id)}
}
