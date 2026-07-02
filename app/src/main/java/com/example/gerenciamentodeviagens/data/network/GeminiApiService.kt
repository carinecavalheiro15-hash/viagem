package com.example.gerenciamentodeviagens.data.network

import com.example.gerenciamentodeviagens.data.model.GeminiRequest
import com.example.gerenciamentodeviagens.data.model.GeminiResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiApiService {
    // Configurado para o modelo gemini-2.5-flash conforme requisito obrigatório do projeto.
    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}
