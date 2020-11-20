package fr.ffnet.downloader.repository

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CrawlService {
    @GET("s/{storyId}/{chapterId}")
    fun getFanfiction(
        @Path("storyId") storyId: String,
        @Path("chapterId") chapterId: String? = "1"
    ): Call<ResponseBody>

    @GET("u/{profileId}")
    fun getProfile(@Path("profileId") profileId: String): Call<ResponseBody>

    @GET("search/?ready=1&type=story")
    fun getSearch(@Query("keywords") keywords: String): Call<ResponseBody>
}
