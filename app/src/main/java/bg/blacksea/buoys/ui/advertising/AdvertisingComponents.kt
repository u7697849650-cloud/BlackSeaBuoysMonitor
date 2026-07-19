package bg.blacksea.buoys.ui.advertising

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import bg.blacksea.buoys.advertising.*
import coil.compose.AsyncImage

@Composable fun SponsoredCompactBanner(campaign:SponsoredCampaign,onDismiss:()->Unit,modifier:Modifier=Modifier){val context=LocalContext.current;Card(modifier.fillMaxWidth().heightIn(max=90.dp)){Row(Modifier.padding(10.dp),verticalAlignment=Alignment.CenterVertically){campaign.imageUrl?.let{AsyncImage(it,null,Modifier.size(52.dp),contentScale=ContentScale.Crop)};Spacer(Modifier.width(8.dp));Column(Modifier.weight(1f)){Text("Спонсорирано",style=MaterialTheme.typography.labelSmall);Text(campaign.title,style=MaterialTheme.typography.titleSmall);campaign.description?.let{Text(it,maxLines=1,style=MaterialTheme.typography.bodySmall)}};campaign.actionUrl?.takeIf(::safeAdvertisingUrl)?.let{url->TextButton(onClick={context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(url)))}){Text(campaign.actionLabel?:"Виж повече")}};IconButton(onClick=onDismiss){Icon(Icons.Default.Close,"Скрий рекламата")}}}}
@Composable fun SponsoredBackground(campaign:SponsoredCampaign?,preferences:UserAdvertisingPreferences,configuration:AdvertisingConfiguration,criticalWarning:Boolean=false,modifier:Modifier=Modifier,content: @Composable () -> Unit){val canShow=configuration.advertisingEnabled&&configuration.backgroundAdsEnabled&&preferences.allAdsEnabled&&preferences.backgroundAdsEnabled&&!criticalWarning&&campaign?.enabled==true&&campaign.format==AdvertisingFormat.BACKGROUND_AD&&safeAdvertisingUrl(campaign.imageUrl);if(!canShow){Box(modifier){content()};return};val opacity=effectiveOpacity(campaign!!,preferences,configuration);Box(modifier){AsyncImage(campaign.imageUrl,null,Modifier.matchParentSize(),contentScale=ContentScale.Crop,alpha=opacity);Box(Modifier.matchParentSize().background(MaterialTheme.colorScheme.background.copy(alpha=(campaign.scrimOpacity?:.80f).coerceIn(0f,.95f))));content();AssistChip(onClick={},label={Text("Спонсор")},modifier=Modifier.align(Alignment.TopEnd).padding(8.dp))}}
