package bg.blacksea.buoys.advertising

import android.content.Context
import kotlinx.coroutines.flow.*
import okhttp3.*
import java.time.Instant

data class AdvertisingState(val configuration:AdvertisingConfiguration=AdvertisingConfiguration(),val campaigns:List<SponsoredCampaign> = emptyList(),val lastUpdated:Instant?=null,val source:String="defaults")
class AdvertisingManager(private val context:Context,private val configUrl:String){
 private val client=OkHttpClient();private val provider=JsonAdvertisingConfigProvider(context,configUrl.takeIf{it.isNotBlank()},client);private val state=MutableStateFlow(AdvertisingState());val advertisingState=state.asStateFlow()
 suspend fun refresh(){val config=provider.fetchConfiguration();val campaigns=fetchCampaigns(config.campaignsJsonUrl);state.value=AdvertisingState(config,campaigns,Instant.now(),if(configUrl.isBlank())"defaults" else "remote JSON")}
 private fun fetchCampaigns(url:String?):List<SponsoredCampaign>{val prefs=context.getSharedPreferences("advertising",0);if(!safeAdvertisingUrl(url))return cached(prefs);return runCatching{client.newCall(Request.Builder().url(url!!).build()).execute().use{r->if(!r.isSuccessful)error("HTTP ${r.code}");val raw=r.body?.string()?:error("empty");val parsed=parseCampaigns(raw);prefs.edit().putString("campaigns",raw).apply();parsed}}.getOrElse{cached(prefs)}}
 private fun cached(prefs:android.content.SharedPreferences)=runCatching{prefs.getString("campaigns",null)?.let(::parseCampaigns).orEmpty()}.getOrDefault(emptyList())
 fun dismiss(id:String,hours:Int){context.getSharedPreferences("advertising",0).edit().putLong("dismiss_$id",Instant.now().plusSeconds(hours*3600L).epochSecond).apply();state.update{it.copy(campaigns=it.campaigns.filterNot{c->c.id==id})}}
}
