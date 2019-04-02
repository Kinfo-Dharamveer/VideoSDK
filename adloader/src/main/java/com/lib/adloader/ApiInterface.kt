package com.lib.adloader

import com.lib.adloader.model.MediaModel
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiInterface {

    @FormUrlEncoded
    @POST("configuration_final")
    fun configuration(
            @Field("code") code: String
    ): Call<MediaModel>
}