package com.example.gerenciamentodeviagens.data.repository

import android.util.Log
import com.example.gerenciamentodeviagens.data.model.Content
import com.example.gerenciamentodeviagens.data.model.GeminiRequest
import com.example.gerenciamentodeviagens.data.model.Part
import com.example.gerenciamentodeviagens.data.network.GeminiApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class GeminiRepository {

    private val apiService: GeminiApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        // Aumentando o timeout para 60 segundos para evitar o erro de 'timeout'
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun gerarRoteiro(
        apiKey: String,
        destino: String,
        dias: Int,
        tipoViagem: String,
        epoca: String,
        orcamentoPretendido: String,
        interesses: String
    ): String? {
        val prompt = """
            Atue como um guia turístico especializado em $destino.
            Crie um roteiro de viagem detalhado para $destino por $dias dias.
            
            Informações do Viajante:
            - Objetivo: $tipoViagem
            - Época: $epoca
            - Orçamento: $orcamentoPretendido
            - Preferências: $interesses
            
            O roteiro DEVE ser dividido por dias e conter:
            1. CAFÉ DA MANHÃ, ALMOÇO e JANTAR: Sugira nomes de locais reais em $destino que caibam no orçamento de $orcamentoPretendido.
            2. ONDE DORMIR: Sugira 2 opções de hospedagem (nome de hotéis ou bairros) compatíveis com o orçamento.
            3. ATIVIDADES: O que fazer de manhã, tarde e noite.
            
            Formate o texto com títulos (Ex: DIA 1, DIA 2) e use emojis para ficar bonito no celular.
        """.trimIndent()
        
        val request = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt))))
        )

        return try {
            val response = apiService.generateContent(apiKey, request)
            val texto = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (texto == null) {
                Log.e("GeminiRepo", "Resposta da IA veio vazia. Verifique se a chave tem permissão.")
            }
            texto
        } catch (e: Exception) {
            Log.e("GeminiRepo", "Erro na chamada da API: ${e.message}")
            null
        }
    }
}
