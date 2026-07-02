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
     * Busca todas as viagens para a cidade atual dentro do período exato solicitado.
     * dataAtual >= dataInicio AND dataAtual <= dataFim
     */
    @Query("""
        SELECT * FROM viagens 
        WHERE userId = :userId 
        AND (LOWER(destino) LIKE '%' || LOWER(:cidade) || '%' OR LOWER(:cidade) LIKE '%' || LOWER(destino) || '%')
        AND (:dataAtual >= dataInicio AND :dataAtual <= dataFim)
    """)
    suspend fun buscarViagensAtuais(userId: Int, cidade: String, dataAtual: Long): List<Viagem>
}
