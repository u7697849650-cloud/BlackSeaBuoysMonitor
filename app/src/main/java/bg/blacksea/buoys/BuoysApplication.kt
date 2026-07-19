package bg.blacksea.buoys

import android.app.Application
import bg.blacksea.buoys.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import bg.blacksea.buoys.forecast.*
import bg.blacksea.buoys.advertising.*

class BuoysApplication:Application(){
    lateinit var repository:BuoyRepository; lateinit var forecastRepository:MarineForecastRepository;lateinit var atmosphericRepository:AtmosphericForecastRepository; lateinit var destinations:DestinationCatalog; lateinit var preferences:Preferences;lateinit var advertising:AdvertisingManager
    override fun onCreate(){super.onCreate(); val db=BuoyDatabase.create(this); preferences=Preferences(this)
        val provider:MarineDataProvider=if(BuildConfig.PROVIDER_MODE=="live") HtmlMarineDataProvider() else MockMarineDataProvider(this)
        repository=BuoyRepository(provider,db.dao());destinations=DestinationCatalog(this);forecastRepository=MarineForecastRepository(OpenMeteoMarineForecastProvider(),db.dao());atmosphericRepository=AtmosphericForecastRepository(OpenMeteoAtmosphericForecastProvider());advertising=AdvertisingManager(this,BuildConfig.AD_CONFIG_URL)
        CoroutineScope(SupervisorJob()+Dispatchers.Default).launch { RefreshWorker.schedule(this@BuoysApplication, preferences.refreshMinutes.first().toLong());advertising.refresh() }
    }
}
