package com.example.rutasapp_compose

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Response

interface APIService {
    @GET("/v2/directions/driving-car")
    suspend fun getRoute(
        @Query("api_key") apiKey: String,
        @Query("start", encoded = true) start: String,
        @Query("end", encoded = true) end: String
    ): Response<RouteResponse>
}