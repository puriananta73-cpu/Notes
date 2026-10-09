package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.Note
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Note::class, ChatMessage::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "quicknote_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate initial innocent notes for authentic disguise
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = getDatabase(context).noteDao()
                                seedDefaultNotes(dao)
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedDefaultNotes(dao: NoteDao) {
            val now = System.currentTimeMillis()
            dao.insertNote(
                Note(
                    title = "Grocery List & Staples",
                    content = "- Almond milk (unsweetened)\n- Sourdough loaf\n- Greek yogurt & blueberries\n- Olive oil (cold-pressed)\n- Baby spinach\n- Coffee beans (Ethiopian roast)",
                    category = "Personal",
                    isPinned = true,
                    colorHex = 0xFFFFFFFF,
                    updatedAt = now - 1000 * 60 * 60 * 2,
                    createdAt = now - 1000 * 60 * 60 * 24 * 2
                )
            )
            dao.insertNote(
                Note(
                    title = "Q4 Product Strategy Sync",
                    content = "1. Streamline onboarding flow to sub-30 seconds\n2. Consolidate telemetry logging to minimize battery draw\n3. Push v2.4 hotfix by end of sprint\n4. Follow up with design team on dark mode tokens",
                    category = "Work",
                    isPinned = false,
                    colorHex = 0xFFFFFFFF,
                    updatedAt = now - 1000 * 60 * 60 * 5,
                    createdAt = now - 1000 * 60 * 60 * 24 * 3
                )
            )
            dao.insertNote(
                Note(
                    title = "Push/Pull/Legs Workout Routine",
                    content = "DAY 1 - PUSH:\n- Incline Dumbbell Press 4x8-10\n- Standing Overhead Press 3x10\n- Cable Lateral Raises 4x12-15\n- Overhead Tricep Extension 3x12\n\nDAY 2 - PULL:\n- Weighted Pull-ups 4x6\n- Barbell Chest-Supported Row 3x10",
                    category = "Personal",
                    isPinned = false,
                    colorHex = 0xFFFFFFFF,
                    updatedAt = now - 1000 * 60 * 60 * 24,
                    createdAt = now - 1000 * 60 * 60 * 24 * 5
                )
            )
            dao.insertNote(
                Note(
                    title = "Books to Read This Year",
                    content = "• Designing Data-Intensive Applications (Kleppmann)\n• Gödel, Escher, Bach (Hofstadter)\n• Chip War: The Fight for the World's Most Critical Tech\n• Snow Crash (Neal Stephenson)",
                    category = "Ideas",
                    isPinned = false,
                    colorHex = 0xFFFFFFFF,
                    updatedAt = now - 1000 * 60 * 60 * 48,
                    createdAt = now - 1000 * 60 * 60 * 24 * 7
                )
            )
            dao.insertNote(
                Note(
                    title = "Apartment WiFi & Smart Hub",
                    content = "Guest Network SSID: HomeGuest_5G\nSmart Plugs IP reservation: 192.168.1.120-135\nFirmware auto-update: Mondays 3 AM",
                    category = "Personal",
                    isPinned = false,
                    colorHex = 0xFFFFFFFF,
                    updatedAt = now - 1000 * 60 * 60 * 72,
                    createdAt = now - 1000 * 60 * 60 * 24 * 10
                )
            )
        }
    }
}
