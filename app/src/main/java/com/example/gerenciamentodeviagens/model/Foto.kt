package com.example.gerenciamentodeviagens.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "fotos",
    foreignKeys = [
        ForeignKey(
            entity = Viagem::class,
            parentColumns = ["id"],
            childColumns = ["viagemId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Foto(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val viagemId: Int,
    val caminho: String // URI ou Path da foto
)
