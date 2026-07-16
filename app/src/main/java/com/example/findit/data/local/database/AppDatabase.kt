package com.example.findit.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.findit.data.local.dao.ItemDao
import com.example.findit.data.local.entity.ItemEntity

@Database(
    entities = [ItemEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE items ADD COLUMN ownerUid TEXT NOT NULL DEFAULT ''"
                )
                db.execSQL("DELETE FROM items")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE items ADD COLUMN lastFoundAt INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE items ADD COLUMN remindEnabled INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    "ALTER TABLE items ADD COLUMN remindHour INTEGER NOT NULL DEFAULT 8"
                )
                db.execSQL(
                    "ALTER TABLE items ADD COLUMN remindMinute INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    "ALTER TABLE items ADD COLUMN remindNextAt INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    "ALTER TABLE items ADD COLUMN remindActive INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "findit_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
