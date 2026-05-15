package com.example.gerenciamentodeviagens.data

import androidx.room.*
import com.example.gerenciamentodeviagens.model.Viagem
import kotlinx.coroutines.flow.Flow

@Dao
interface ViagemDao {
    @Insert
    suspend fun inserir(viagem: Viagem)

    @Update
    suspend fun atualizar(viagem: Viagem)

    @Delete
    suspend fun excluir(viagem: Viagem)

    @Query("SELECT * FROM viagens WHERE userId = :userId")
    fun listarPorUsuario(userId: Int): Flow<List<Viagem>>

    @Query("SELECT * FROM viagens WHERE id = :id")
    suspend fun buscarPorId(id: Int): Viagem?

    /**
     * Busca todas as viagens para a cidade atual dentro do período.
     * 1. Usa LIKE para que "Bombinhas" encontre "Bombinhas SC" (ou vice-versa).
     * 2. Adiciona margem de 24h (86400000ms) para lidar com fuso horário e vésperas.
     */
    @Query("""
        SELECT * FROM viagens 
        WHERE userId = :userId 
        AND (LOWER(destino) LIKE '%' || LOWER(:cidade) || '%' OR LOWER(:cidade) LIKE '%' || LOWER(destino) || '%')
        AND (:dataAtual >= (dataInicio - 86400000) AND :dataAtual <= (dataFim + 86400000))
    """)
    suspend fun buscarViagensAtuais(userId: Int, cidade: String, dataAtual: Long): List<Viagem>
}
