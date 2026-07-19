package bg.blacksea.buoys

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

class RefreshWorker(context:Context,params:WorkerParameters):CoroutineWorker(context,params){
    override suspend fun doWork()=try{(applicationContext as BuoysApplication).repository.refresh();Result.success()}catch(_:Exception){Result.retry()}
    companion object{
      fun schedule(context:Context,minutes:Long=60){
        if (minutes <= 0) { cancel(context); return }
        val request=PeriodicWorkRequestBuilder<RefreshWorker>(minutes.coerceAtLeast(15),TimeUnit.MINUTES).setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork("marine-refresh",ExistingPeriodicWorkPolicy.UPDATE,request)
      }
      fun cancel(context:Context)=WorkManager.getInstance(context).cancelUniqueWork("marine-refresh")
    }
}
