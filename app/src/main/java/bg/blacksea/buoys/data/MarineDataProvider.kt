package bg.blacksea.buoys.data

import bg.blacksea.buoys.domain.*

interface MarineDataProvider {
    suspend fun getStations(): Result<List<MarineStation>>
    suspend fun getLatestObservations(): Result<List<BuoyObservation>>
}

sealed class MarineDataException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class Network(cause: Throwable) : MarineDataException("Няма връзка с източника.", cause)
    class Http(val code: Int) : MarineDataException("Източникът върна HTTP грешка $code.")
    class Empty : MarineDataException("Източникът върна празен отговор.")
    class Structure(details: String) : MarineDataException("Структурата на източника е променена: $details")
}
