package com.hisabbook.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hisabbook.app.data.db.dao.EntryDao
import com.hisabbook.app.data.db.dao.PersonDao
import com.hisabbook.app.data.db.entity.EntryEntity
import com.hisabbook.app.data.db.entity.PersonEntity
import net.sqlcipher.database.SupportFactory
import java.security.SecureRandom

@Database(
    entities = [PersonEntity::class, EntryEntity::class],
    version = 1,
    exportSchema = true
)
abstract class HisabBookDb : RoomDatabase() {
    abstract fun personDao(): PersonDao
    abstract fun entryDao(): EntryDao

    companion object {
        private const val DB_NAME = "hisabbook.db"

        fun build(ctx: Context, passphrase: ByteArray): HisabBookDb {
            val factory = SupportFactory(passphrase)
            return Room.databaseBuilder(ctx, HisabBookDb::class.java, DB_NAME)
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration()
                .build()
        }

        /** Generate a random 32-byte key; caller is responsible for persisting to EncryptedSharedPreferences or Keystore. */
        fun generatePassphrase(): ByteArray {
            val b = ByteArray(32)
            SecureRandom().nextBytes(b)
            return b
        }
    }
}
