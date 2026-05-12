package com.example.gerenciamentodeviagens.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "viagens")
data class Viagem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val destino: String,
    val tipo: String, // "Lazer" ou "Negócios"
    val dataInicio: Long,
    val dataFim: Long,
    val orcamento: Double,
    val totalGastos: Double = 0.0, // Campo novo solicitado
    val userId: Int
)
