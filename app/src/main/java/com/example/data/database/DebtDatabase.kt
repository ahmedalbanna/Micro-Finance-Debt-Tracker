package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.DebtDao
import com.example.data.entity.Client
import com.example.data.entity.Invoice
import com.example.data.entity.Payment
import com.example.data.entity.Reconciliation

@Database(
    entities = [Client::class, Invoice::class, Payment::class, Reconciliation::class],
    version = 1,
    exportSchema = false
)
abstract class DebtDatabase : RoomDatabase() {
    abstract fun debtDao(): DebtDao

    companion object {
        @Volatile
        private var INSTANCE: DebtDatabase? = null

        fun getDatabase(context: Context): DebtDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DebtDatabase::class.java,
                    "debt_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
