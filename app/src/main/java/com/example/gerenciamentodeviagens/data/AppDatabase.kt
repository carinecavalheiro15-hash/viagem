package com.example.gerenciamentodeviagens.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.gerenciamentodeviagens.model.Usuario
import com.example.gerenciamentodeviagens.model.Viagem

@Database(entities = [Usuario::class, Viagem::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun viagemDao(): ViagemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "viagens_db"
                )
                .fallbackToDestructiveMigration() // Para simplificar o desenvolvimento inicial com novas tabelas
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
