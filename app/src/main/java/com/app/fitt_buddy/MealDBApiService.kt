package com.app.fitt_buddy

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface MealDBApiService {
    @GET("api/json/v1/1/search.php")
    fun searchMeals(
        @Query("s") query: String
    ): Call<MealResponse>
}