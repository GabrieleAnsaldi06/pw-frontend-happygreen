package com.happygreen.data

import com.happygreen.models.*
import com.happygreen.viewmodels.*
import com.happygreen.viewmodels.QuizQuestion
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // Autenticazione
    @POST("api/login/")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/register/")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    // Gruppi
    @GET("api/groups/")
    suspend fun getGroups(): Response<PaginatedResponse<Group>>

    @POST("api/groups/")
    suspend fun createGroup(@Body group: Group): Response<Group>

    @GET("api/groups/{id}/")
    suspend fun getGroup(@Path("id") groupId: Int): Response<Group>

    @POST("api/groups/{id}/join/")
    suspend fun joinGroup(@Path("id") groupId: Int): Response<Map<String, String>>

    // Post
    @GET("api/posts/")
    suspend fun getPosts(@Query("group") groupId: Int? = null): Response<PaginatedResponse<Post>>

    @POST("api/posts/")
    suspend fun createPost(@Body post: CreatePostRequest): Response<Post>

    @GET("api/posts/{id}/")
    suspend fun getPost(@Path("id") postId: Int): Response<Post>

    @GET("api/posts/{id}/comments/")
    suspend fun getPostComments(@Path("id") postId: Int): Response<PaginatedResponse<Comment>>

    @POST("api/posts/{id}/comments/")
    suspend fun addComment(@Path("id") postId: Int, @Body comment: Comment): Response<Comment>

    // Oggetti riconosciuti
    @GET("api/objects/")
    suspend fun getObjects(): Response<PaginatedResponse<RecognizedObject>>

    @GET("api/objects/{id}/")
    suspend fun getObject(@Path("id") objectId: Int): Response<RecognizedObject>

    @POST("api/scans/")
    suspend fun recordScan(@Body scan: Map<String, Any>): Response<Map<String, Any>>

    // Quiz e sfide
    @GET("api/quizzes/")
    suspend fun getQuizzes(): Response<PaginatedResponse<QuizQuestion>>

    @GET("api/challenges/")
    suspend fun getChallenges(): Response<PaginatedResponse<Challenge>>

    @POST("api/challenges/{id}/join/")
    suspend fun joinChallenge(@Path("id") challengeId: Int): Response<Map<String, Any>>

    @POST("api/challenges/{id}/complete/")
    suspend fun completeChallenge(@Path("id") challengeId: Int): Response<Map<String, Any>>

    @GET("api/challenges/{id}/participants")
    suspend fun getChallengeParticipantsByID(@Path("id") userId: Int): Response<List<ChallengeParticipation>>

    // Profilo utente e badge
    @GET("api/profiles/{id}/")
    suspend fun getMyProfile(@Path("id") id: Int): Response<UserProfile>

    @GET("api/badges/")
    suspend fun getBadges(): Response<PaginatedResponse<Badge>>

    // Prodotti e barcode
    @GET("api/products/{barcode}/")
    suspend fun getProductByBarcode(@Path("barcode") barcode: String): Response<Product>

    @POST("api/products/lookup/")
    suspend fun lookupProduct(@Body request: Map<String, String>): Response<Product>

    @GET("api/profiles/")
    suspend fun getProfiles(): Response<PaginatedResponse<UserProfile>>
}