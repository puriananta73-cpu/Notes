package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Note
import com.example.data.repository.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotesViewModel(private val repository: NotesRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory

    private val _allNotes = repository.allNotes

    val filteredNotes: StateFlow<List<Note>> = combine(
        _allNotes,
        _searchQuery,
        _selectedCategory
    ) { notes, query, category ->
        notes.filter { note ->
            val matchesCategory = category == "All" || note.category.equals(category, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                    note.title.contains(query, ignoreCase = true) ||
                    note.content.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // App lock state
    private val _isPinProtected = MutableStateFlow(false)
    val isPinProtected: StateFlow<Boolean> = _isPinProtected

    private val _isAppLocked = MutableStateFlow(false)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked

    private val _savedPin = MutableStateFlow("1234")
    val savedPin: StateFlow<String> = _savedPin

    // Secret trigger counter for 3-tap logo gesture
    private var logoTapCount = 0
    private var lastLogoTapTime = 0L

    fun onLogoTapped(onSecretUnlock: () -> Unit) {
        val now = System.currentTimeMillis()
        if (now - lastLogoTapTime > 1500) {
            logoTapCount = 1
        } else {
            logoTapCount++
        }
        lastLogoTapTime = now

        if (logoTapCount >= 3) {
            logoTapCount = 0
            onSecretUnlock()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
    }

    fun saveNote(
        id: Long = 0,
        title: String,
        content: String,
        category: String = "Personal",
        isPinned: Boolean = false,
        onSecretUnlock: () -> Unit,
        onSaved: () -> Unit
    ) {
        val cleanTitle = title.trim()
        // SECRET TRIGGER: exact trigger #APAP
        if (cleanTitle == "#APAP") {
            onSecretUnlock()
            return
        }

        viewModelScope.launch {
            val note = Note(
                id = id,
                title = if (cleanTitle.isBlank()) "Untitled Note" else cleanTitle,
                content = content,
                category = category,
                isPinned = isPinned,
                updatedAt = System.currentTimeMillis(),
                createdAt = if (id == 0L) System.currentTimeMillis() else System.currentTimeMillis()
            )
            repository.insertOrUpdate(note)
            onSaved()
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            repository.insertOrUpdate(note.copy(isPinned = !note.isPinned, updatedAt = System.currentTimeMillis()))
        }
    }

    fun togglePinProtection(enabled: Boolean, pin: String = "1234") {
        _isPinProtected.value = enabled
        _savedPin.value = pin
    }

    fun unlockApp(pin: String): Boolean {
        if (pin == _savedPin.value) {
            _isAppLocked.value = false
            return true
        }
        return false
    }

    fun lockApp() {
        if (_isPinProtected.value) {
            _isAppLocked.value = true
        }
    }
}

class NotesViewModelFactory(private val repository: NotesRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
