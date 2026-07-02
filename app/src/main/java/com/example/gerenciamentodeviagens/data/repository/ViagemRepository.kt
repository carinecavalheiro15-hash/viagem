package com.example.gerenciamentodeviagens.data.repository

import com.example.gerenciamentodeviagens.data.ViagemDao
import com.example.gerenciamentodeviagens.model.Viagem
import kotlinx.coroutines.flow.Flow

class ViagemRepository(private val dao: ViagemDao) {
    fun listarViagens(userId: Int): Flow<List<Viagem>> = dao.listarPorUsuario(userId)
    
    suspend fun inserir(viagem: Viagem) = dao.inserir(viagem)
    
    suspend fun atualizar(viagem: Viagem) = dao.atualizar(viagem)
    
    suspend fun excluir(viagem: Viagem) = dao.excluir(viagem)

    suspend fun buscarPorId(id: Int): Viagem? = dao.buscarPorId(id)

    suspend fun buscarViagensAtuais(userId: Int, cidade: String, dataAtual: Long): List<Viagem> {
        return dao.buscarViagensAtuais(userId, cidade, dataAtual)
    }
}
