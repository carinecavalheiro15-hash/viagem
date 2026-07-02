package com.example.gerenciamentodeviagens.data

import androidx.room.*
import com.example.gerenciamentodeviagens.model.Foto
import kotlinx.coroutines.flow.Flow

@Dao
interface FotoDao {
    @Insert
    suspend fun inserir(foto: Foto)

    @Query("SELECT * FROM fotos WHERE viagemId = :viagemId")
    fun listarPorViagem(viagemId: Int): Flow<List<Foto>>

    @Delete
    suspend fun excluir(foto: Foto)
}
