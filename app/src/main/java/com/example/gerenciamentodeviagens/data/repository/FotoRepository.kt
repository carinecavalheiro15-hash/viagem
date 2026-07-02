package com.example.gerenciamentodeviagens.data.repository

import com.example.gerenciamentodeviagens.data.FotoDao
import com.example.gerenciamentodeviagens.model.Foto
import kotlinx.coroutines.flow.Flow

class FotoRepository(private val dao: FotoDao) {
    fun listarFotos(viagemId: Int): Flow<List<Foto>> = dao.listarPorViagem(viagemId)
    suspend fun inserir(foto: Foto) = dao.inserir(foto)
    suspend fun excluir(foto: Foto) = dao.excluir(foto)
}
