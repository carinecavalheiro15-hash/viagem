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

    @Query("SELECT * FROM viagens WHERE userId = :userId AND LOWER(destino) = LOWER(:cidade) AND :dataAtual BETWEEN dataInicio AND dataFim LIMIT 1")
    suspend fun buscarViagemAtual(userId: Int, cidade: String, dataAtual: Long): Viagem?
}
