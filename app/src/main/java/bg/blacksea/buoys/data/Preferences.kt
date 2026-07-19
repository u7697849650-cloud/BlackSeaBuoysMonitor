package bg.blacksea.buoys.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import bg.blacksea.buoys.domain.activity.*

private val Context.dataStore by preferencesDataStore("settings")
class Preferences(private val context:Context){
    private val selected=stringPreferencesKey("selected_station"); private val theme=stringPreferencesKey("theme"); private val refresh=intPreferencesKey("refresh_minutes");private val language=stringPreferencesKey("app_language");private val onboarding=booleanPreferencesKey("onboarding_complete");private val activityDaylight=booleanPreferencesKey("activity_daylight_only");private val activityExperience=stringPreferencesKey("activity_experience");private val activityProfiles=stringSetPreferencesKey("activity_user_profiles_v1");private val favoriteActivities=stringSetPreferencesKey("favorite_activities");private val allAds=booleanPreferencesKey("all_ads");private val bgAds=booleanPreferencesKey("background_ads");private val adOpacity=floatPreferencesKey("ad_opacity")
    val selectedStation=context.dataStore.data.map{it[selected]?:"varna"}; val themeMode=context.dataStore.data.map{it[theme]?:"system"}; val refreshMinutes=context.dataStore.data.map{it[refresh]?:60};val appLanguage=context.dataStore.data.map{it[language]?:"bg"};val onboardingComplete=context.dataStore.data.map{it[onboarding]?:false};val activitiesDaylightOnly=context.dataStore.data.map{it[activityDaylight]?:true};val defaultActivityExperience=context.dataStore.data.map{it[activityExperience]?:"INTERMEDIATE"};val userActivityProfiles=context.dataStore.data.map{p->p[activityProfiles].orEmpty().mapNotNull(UserActivityProfileCodec::decode).associateBy{it.activityType}};val activityFavorites=context.dataStore.data.map{it[favoriteActivities]?:emptySet()};val allAdsEnabled=context.dataStore.data.map{it[allAds]?:true};val backgroundAdsEnabled=context.dataStore.data.map{it[bgAds]?:false};val backgroundAdOpacity=context.dataStore.data.map{(it[adOpacity]?:.08f).coerceIn(.03f,.20f)}
    suspend fun select(id:String)=context.dataStore.edit{it[selected]=id}; suspend fun theme(value:String)=context.dataStore.edit{it[theme]=value}; suspend fun refresh(value:Int)=context.dataStore.edit{it[refresh]=value};suspend fun setLanguage(value:String)=context.dataStore.edit{it[language]=value}
    suspend fun toggleActivity(id:String)=context.dataStore.edit{p->val current=p[favoriteActivities]?.toMutableSet()?:mutableSetOf();if(!current.add(id))current.remove(id);p[favoriteActivities]=current}
    suspend fun setAllAds(value:Boolean)=context.dataStore.edit{it[allAds]=value};suspend fun setBackgroundAds(value:Boolean)=context.dataStore.edit{it[bgAds]=value};suspend fun setAdOpacity(value:Float)=context.dataStore.edit{it[adOpacity]=value.coerceIn(.03f,.20f)}
    suspend fun setActivitiesDaylightOnly(value:Boolean)=context.dataStore.edit{it[activityDaylight]=value};suspend fun setDefaultActivityExperience(value:String)=context.dataStore.edit{it[activityExperience]=value};suspend fun resetActivitySettings()=context.dataStore.edit{it.remove(activityDaylight);it.remove(activityExperience)}
    suspend fun completeOnboarding()=context.dataStore.edit{it[onboarding]=true}
    suspend fun saveActivityProfile(profile:UserActivityProfile)=context.dataStore.edit{p->val profiles=p[activityProfiles].orEmpty().mapNotNull(UserActivityProfileCodec::decode).associateBy{it.activityType}.toMutableMap();profiles[profile.activityType]=profile;p[activityProfiles]=profiles.values.map(UserActivityProfileCodec::encode).toSet()}
    suspend fun resetActivityProfile(type:MarineActivityType)=context.dataStore.edit{p->p[activityProfiles]=p[activityProfiles].orEmpty().filter{UserActivityProfileCodec.decode(it)?.activityType!=type}.toSet()}
}
