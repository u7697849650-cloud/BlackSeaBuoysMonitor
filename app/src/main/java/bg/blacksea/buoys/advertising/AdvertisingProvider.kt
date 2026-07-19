package bg.blacksea.buoys.advertising

import android.content.Context
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.*
import java.time.Instant

interface AdvertisingConfigProvider{suspend fun fetchConfiguration():AdvertisingConfiguration;fun observeConfiguration():Flow<AdvertisingConfiguration>}
@Serializable private data class RemoteConfigDto(val advertisingEnabled:Boolean=false,val compactBannersEnabled:Boolean=false,val sponsoredCardsEnabled:Boolean=false,val backgroundAdsEnabled:Boolean=false,val admobEnabled:Boolean=false,val campaignsJsonUrl:String?=null,val defaultMinimumIntervalMinutes:Int=30,val defaultMaxImpressionsPerSession:Int=2,val defaultDismissDurationHours:Int=24,val defaultBackgroundOpacity:Float=.08f,val minimumBackgroundOpacity:Float=.03f,val maximumBackgroundOpacity:Float=.20f,val userCanDisableBackgroundAds:Boolean=true,val userCanChangeBackgroundOpacity:Boolean=true)
class JsonAdvertisingConfigProvider(private val context:Context,private val configUrl:String?,private val client:OkHttpClient=OkHttpClient()):AdvertisingConfigProvider{
 private val json=Json{ignoreUnknownKeys=true};private val state=MutableStateFlow(loadCached())
 override fun observeConfiguration()=state.asStateFlow()
 override suspend fun fetchConfiguration():AdvertisingConfiguration{if(!safeAdvertisingUrl(configUrl))return state.value;return runCatching{client.newCall(Request.Builder().url(configUrl!!).build()).execute().use{r->if(!r.isSuccessful)error("HTTP ${r.code}");val text=r.body?.string()?:error("empty");val dto=json.decodeFromString<RemoteConfigDto>(text);context.getSharedPreferences("advertising",0).edit().putString("config",text).apply();dto.domain()}}.getOrElse{state.value}.also{state.value=it}}
 private fun loadCached()=runCatching{val raw=context.getSharedPreferences("advertising",0).getString("config",null)?:return@runCatching AdvertisingConfiguration();json.decodeFromString<RemoteConfigDto>(raw).domain()}.getOrDefault(AdvertisingConfiguration())
 private fun RemoteConfigDto.domain()=AdvertisingConfiguration(advertisingEnabled,compactBannersEnabled,sponsoredCardsEnabled,backgroundAdsEnabled,admobEnabled,campaignsJsonUrl,defaultMinimumIntervalMinutes,defaultMaxImpressionsPerSession,defaultDismissDurationHours,defaultBackgroundOpacity,minimumBackgroundOpacity.coerceIn(0f,.2f),maximumBackgroundOpacity.coerceIn(.03f,.2f),userCanDisableBackgroundAds,userCanChangeBackgroundOpacity)
}

@Serializable data class CampaignsDocumentDto(val version:Int=1,val updatedAt:String?=null,val campaigns:List<CampaignDto> = emptyList())
@Serializable data class CampaignDto(val id:String,val enabled:Boolean=false,val format:String,val title:String,val description:String?=null,val sponsorName:String?=null,val imageUrl:String?=null,val actionLabel:String?=null,val actionUrl:String?=null,val startsAt:String?=null,val endsAt:String?=null,val placements:Set<String> = emptySet(),val destinationIds:Set<String> = emptySet(),val activityTypes:Set<String> = emptySet(),val priority:Int=0,val minimumIntervalMinutes:Int=30,val maxImpressionsPerSession:Int=2,val dismissDurationHours:Int=24,val backgroundOpacity:Float?=null,val backgroundBlurDp:Float?=null,val backgroundPosition:String?=null,val backgroundFit:String?=null,val scrimOpacity:Float?=null,val allowUserOpacityControl:Boolean=true)
fun parseCampaigns(text:String):List<SponsoredCampaign>{val json=Json{ignoreUnknownKeys=true};return json.decodeFromString<CampaignsDocumentDto>(text).campaigns.mapNotNull{d->runCatching{SponsoredCampaign(d.id,d.enabled,AdvertisingFormat.valueOf(d.format),d.title,d.description,d.sponsorName,d.imageUrl?.takeIf(::safeAdvertisingUrl),d.actionLabel,d.actionUrl?.takeIf(::safeAdvertisingUrl),d.startsAt?.let(Instant::parse),d.endsAt?.let(Instant::parse),d.placements.map{AdPlacement.valueOf(it)}.toSet(),d.destinationIds,d.activityTypes.map{bg.blacksea.buoys.domain.activity.MarineActivityType.valueOf(it)}.toSet(),d.priority,d.minimumIntervalMinutes,d.maxImpressionsPerSession,d.dismissDurationHours,d.backgroundOpacity,d.backgroundBlurDp,d.backgroundPosition?.let(BackgroundImagePosition::valueOf),d.backgroundFit?.let(BackgroundImageFit::valueOf),d.scrimOpacity,d.allowUserOpacityControl)}.getOrNull()}}
