package com.app.fitt_buddy

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface EdamamApiService {
    @GET("search")
    fun searchRecipes(
        @Query("q") query: String,
        @Query("app_id") appId: String,
        @Query("app_key") appKey: String,
        @Query("from") from: Int = 0,
        @Query("to") to: Int = 20
    ): Call<RecipeResponse>
}