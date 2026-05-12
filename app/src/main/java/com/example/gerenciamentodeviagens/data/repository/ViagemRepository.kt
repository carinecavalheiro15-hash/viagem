package com.example.gerenciamentodeviagens.data.repository

import com.example.gerenciamentodeviagens.data.ViagemDao
import com.example.gerenciamentodeviagens.model.Viagem
import kotlinx.coroutines.flow.Flow

class ViagemRepository(private val dao: ViagemDao) {
    fun listarViagens(userId: Int): Flow<List<Viagem>> = dao.listarPorUsuario(userId)
    
    suspend fun inserir(viagem: Viagem) = dao.inserir(viagem)
    
    suspend fun atualizar(viagem: Viagem) = dao.atualizar(viagem)
    
    suspend fun excluir(viagem: Viagem) = dao.excluir(viagem)

    suspend fun buscarViagemAtual(userId: Int, cidade: String, dataAtual: Long): Viagem? {
        return dao.buscarViagemAtual(userId, cidade, dataAtual)
    }
}
