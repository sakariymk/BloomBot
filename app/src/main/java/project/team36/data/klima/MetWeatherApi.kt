package project.team36.data.klima

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

/**
 * Formål og funksjon:
 * Jeg har laget denne filen for å fungere som appens direkte "telefonlinje" til Meteorologisk institutt (MET).
 * Den definerer Retrofit-kontrakten som forteller appen nøyaktig hvordan jeg skal bygge opp spørringen
 * for å hente værdata for en gitt lokasjon. Jeg har designet den til å kun hente dataene, ikke behandle dem.
 *
 * Relevante filer:
 * - MainActivity: Her setter jeg opp selve nettverksklienten som bruker dette interfacet.
 * - HybridAiRepository: Her gjør jeg selve anropet for å gi været videre til Claude.
 */

// Jeg bruker et interface slik at Retrofit automatisk kan skrive all den underliggende nettverkskoden for meg.
interface MetWeatherApi {

    // Her forteller jeg Retrofit at jeg skal gjøre et HTTP GET-kall mot METs 'compact' endepunkt.
    @GET("weatherapi/locationforecast/2.0/compact")

    // Jeg bruker 'suspend' fordi jeg vil at dette nettverkskallet skal kjøre asynkront i bakgrunnen.
    // På den måten unngår jeg at appen og UI-et fryser mens jeg venter på svar fra MET.
    suspend fun getWeather(

        // Jeg legger til breddegrad i URL-en slik: ?lat=verdi
        @Query("lat") lat: Double,

        // Jeg legger til lengdegrad i URL-en slik: &lon=verdi
        @Query("lon") lon: Double,

        // MET har et strengt krav om at alle anrop må identifisere seg med en User-Agent.
        // Jeg har hardkodet vår Team36-identifikator her for å sikre at vi ikke blir blokkert.
        @Header("User-Agent") userAgent: String = "Team36PlantApp/1.0 (test@example.com)"

        // Jeg velger å hente tilbake en rå ResponseBody fordi MET sender mye data,
        // og jeg vil bare trekke ut akkurat den teksten jeg trenger for AI-en min senere.
    ): Response<ResponseBody>
}