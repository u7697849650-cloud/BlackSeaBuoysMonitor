package bg.blacksea.buoys

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import bg.blacksea.buoys.data.*
import bg.blacksea.buoys.ui.BuoysApp
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import bg.blacksea.buoys.forecast.*
import bg.blacksea.buoys.advertising.*

class MainViewModel(private val app:BuoysApplication, private val repo:BuoyRepository, val prefs:Preferences):ViewModel(){
    val snapshot=repo.snapshot; val selected=prefs.selectedStation.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),"varna")
    val favorites=repo.favorites.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
    val selectedHistory=selected.flatMapLatest{repo.history(it,java.time.Instant.now().minusSeconds(86400).toString())}.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
    val refreshMinutes=prefs.refreshMinutes.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),60);val appLanguage=prefs.appLanguage.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),"bg")
    val activityFavorites=prefs.activityFavorites.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptySet())
    val activitiesDaylightOnly=prefs.activitiesDaylightOnly.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),true);val defaultActivityExperience=prefs.defaultActivityExperience.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),"INTERMEDIATE")
    val onboardingComplete=prefs.onboardingComplete.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),false)
    val userActivityProfiles=prefs.userActivityProfiles.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyMap())
    val allAdsEnabled=prefs.allAdsEnabled.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),true);val backgroundAdsEnabled=prefs.backgroundAdsEnabled.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),false);val backgroundAdOpacity=prefs.backgroundAdOpacity.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),.08f)
    val advertising=app.advertising.advertisingState
    val destinations=app.destinations.destinations
    val forecast:StateFlow<ForecastState> = selected
        .map { app.destinations.byId(it).id }
        .distinctUntilChanged()
        .flatMapLatest { app.forecastRepository.observe(it) }
        .stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),ForecastState())
    val atmospheric:StateFlow<AtmosphericForecastState> = selected.map{app.destinations.byId(it).id}.distinctUntilChanged().flatMapLatest{app.atmosphericRepository.observe(it)}.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),AtmosphericForecastState())
    init{refresh();refreshForecast();viewModelScope.launch{selected.drop(1).distinctUntilChanged().collect{refreshForecast()}}}; fun refresh()=viewModelScope.launch{repo.refresh()}; fun refreshForecast()=viewModelScope.launch{val d=app.destinations.byId(selected.value);launch{app.forecastRepository.refresh(d,7)};launch{app.atmosphericRepository.refresh(d)}}; fun select(id:String)=viewModelScope.launch{prefs.select(id)}
    fun favorite(id:String)=viewModelScope.launch{repo.toggleFavorite(id,id in favorites.value)}
    fun setRefreshMinutes(minutes:Int)=viewModelScope.launch{prefs.refresh(minutes);RefreshWorker.schedule(app,minutes.toLong())}
    fun toggleActivityFavorite(id:String)=viewModelScope.launch{prefs.toggleActivity(id)};fun setLanguage(value:String)=viewModelScope.launch{prefs.setLanguage(value)}
    fun setActivitiesDaylightOnly(value:Boolean)=viewModelScope.launch{prefs.setActivitiesDaylightOnly(value)};fun setDefaultActivityExperience(value:String)=viewModelScope.launch{prefs.setDefaultActivityExperience(value)};fun resetActivitySettings()=viewModelScope.launch{prefs.resetActivitySettings()}
    fun completeOnboarding()=viewModelScope.launch{prefs.completeOnboarding()}
    fun saveActivityProfile(profile:bg.blacksea.buoys.domain.activity.UserActivityProfile)=viewModelScope.launch{prefs.saveActivityProfile(profile)};fun resetActivityProfile(type:bg.blacksea.buoys.domain.activity.MarineActivityType)=viewModelScope.launch{prefs.resetActivityProfile(type)}
    fun setAllAds(value:Boolean)=viewModelScope.launch{prefs.setAllAds(value)};fun setBackgroundAds(value:Boolean)=viewModelScope.launch{prefs.setBackgroundAds(value)};fun setAdOpacity(value:Float)=viewModelScope.launch{prefs.setAdOpacity(value)}
    fun dismissAd(id:String,hours:Int)=app.advertising.dismiss(id,hours)
    fun forecastDestinationId(id:String)=app.destinations.byId(id).id
    fun forecastDestinationName(id:String)=app.destinations.byId(id).name
}
class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);val app=application as BuoysApplication
    val vm=ViewModelProvider(this,object:ViewModelProvider.Factory{override fun<T:ViewModel>create(c:Class<T>):T=MainViewModel(app,app.repository,app.preferences) as T})[MainViewModel::class.java]
    setContent{BuoysApp(vm)}
}}
