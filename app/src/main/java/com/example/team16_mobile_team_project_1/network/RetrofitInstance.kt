package com.example.team16_mobile_team_project_1.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * A singleton object that provides a configured Retrofit instance for network requests.
 */
object RetrofitInstance {
    /**
     * The base URL for the Firebase Realtime Database.
     */
    private const val BASE_URL = "https://mobileteamproject1-d8a95-default-rtdb.asia-southeast1.firebasedatabase.app/"

    /**
     * A lazily initialized Retrofit service instance for the [ApiService] interface.
     */
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}