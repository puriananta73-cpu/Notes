package com.example

import com.example.data.db.NoteDao
import com.example.data.model.Note
import com.example.data.repository.NotesRepository
import com.example.ui.viewmodel.NotesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExampleUnitTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    class FakeNoteDao : NoteDao {
        val notes = mutableListOf<Note>()
        override fun getAllNotes(): Flow<List<Note>> = flowOf(notes)
        override suspend fun getNoteById(id: Long): Note? = notes.find { it.id == id }
        override fun searchNotes(query: String): Flow<List<Note>> =
            flowOf(notes.filter { it.title.contains(query) || it.content.contains(query) })
        override suspend fun insertNote(note: Note): Long {
            notes.add(note)
            return note.id
        }
        override suspend fun updateNote(note: Note) {
            val index = notes.indexOfFirst { it.id == note.id }
            if (index != -1) notes[index] = note
        }
        override suspend fun deleteNote(note: Note) {
            notes.removeIf { it.id == note.id }
        }
        override suspend fun deleteNoteById(id: Long) {
            notes.removeIf { it.id == id }
        }
        override suspend fun getCount(): Int = notes.size
    }

    @Test
    fun testSecretTriggerUnlock() = runTest(testDispatcher) {
        val fakeDao = FakeNoteDao()
        val repo = NotesRepository(fakeDao)
        val vm = NotesViewModel(repo)

        var unlocked = false
        var saved = false

        // When title is exactly #APAP, trigger unlock and do NOT save note
        vm.saveNote(
            title = "#APAP",
            content = "This should not be saved",
            onSecretUnlock = { unlocked = true },
            onSaved = { saved = true }
        )

        advanceUntilIdle()

        assertTrue("Secret unlock must trigger for title #APAP", unlocked)
        assertFalse("Secret trigger note must not be saved as a regular note", saved)
        assertEquals(0, fakeDao.notes.size)
    }

    @Test
    fun testNormalNoteSaved() = runTest(testDispatcher) {
        val fakeDao = FakeNoteDao()
        val repo = NotesRepository(fakeDao)
        val vm = NotesViewModel(repo)

        var unlocked = false
        var saved = false

        vm.saveNote(
            title = "Innocent Note",
            content = "Regular grocery shopping",
            onSecretUnlock = { unlocked = true },
            onSaved = { saved = true }
        )

        advanceUntilIdle()

        assertFalse(unlocked)
        assertTrue(saved)
        assertEquals(1, fakeDao.notes.size)
        assertEquals("Innocent Note", fakeDao.notes[0].title)
    }

    @Test
    fun testTripleTapLogoUnlock() {
        val fakeDao = FakeNoteDao()
        val repo = NotesRepository(fakeDao)
        val vm = NotesViewModel(repo)

        var unlocked = false
        vm.onLogoTapped { unlocked = true }
        assertFalse(unlocked)
        vm.onLogoTapped { unlocked = true }
        assertFalse(unlocked)
        vm.onLogoTapped { unlocked = true }
        assertTrue("3 rapid logo taps should trigger secret unlock", unlocked)
    }
}
