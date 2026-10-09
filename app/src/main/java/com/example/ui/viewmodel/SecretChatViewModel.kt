package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.ChatMessage
import com.example.data.repository.ChatRepository
import com.example.util.AudioUtils
import com.example.util.SoundEffects
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

data class CallParticipant(
    val handle: String,
    val colorHex: Long,
    val isSpeaking: Boolean = false,
    val isMuted: Boolean = false
)

class SecretChatViewModel(
    application: Application,
    private val repository: ChatRepository
) : AndroidViewModel(application) {

    private val audioUtils = AudioUtils(application)

    // Current anonymous user handle
    private val _myHandle = MutableStateFlow(generateRandomHandle())
    val myHandle: StateFlow<String> = _myHandle

    val allMessages: StateFlow<List<ChatMessage>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Ephemeral self-destruct setting (seconds: 0 = Off, 10 = 10s, 30 = 30s, 60 = 1m, 86400 = 24h)
    private val _selfDestructSeconds = MutableStateFlow(0)
    val selfDestructSeconds: StateFlow<Int> = _selfDestructSeconds

    // Privacy & Security toggles
    private val _isScreenshotBlocked = MutableStateFlow(false)
    val isScreenshotBlocked: StateFlow<Boolean> = _isScreenshotBlocked

    // Voice recording state
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording

    private val _recordingDurationSec = MutableStateFlow(0)
    val recordingDurationSec: StateFlow<Int> = _recordingDurationSec

    // Voice message playback state
    private val _playingAudioMessageId = MutableStateFlow<String?>(null)
    val playingAudioMessageId: StateFlow<String?> = _playingAudioMessageId

    // Group Voice Call state
    private val _isInVoiceCall = MutableStateFlow(false)
    val isInVoiceCall: StateFlow<Boolean> = _isInVoiceCall

    private val _isCallMuted = MutableStateFlow(false)
    val isCallMuted: StateFlow<Boolean> = _isCallMuted

    private val _isCallSpeaker = MutableStateFlow(true)
    val isCallSpeaker: StateFlow<Boolean> = _isCallSpeaker

    private val _callDurationSeconds = MutableStateFlow(0)
    val callDurationSeconds: StateFlow<Int> = _callDurationSeconds

    private val _callParticipants = MutableStateFlow<List<CallParticipant>>(emptyList())
    val callParticipants: StateFlow<List<CallParticipant>> = _callParticipants

    // Background jobs
    private var recordingTimerJob: Job? = null
    private var callTimerJob: Job? = null
    private var callSimulationJob: Job? = null
    private var peerSimulationJob: Job? = null
    private var expiredMessagesCleanerJob: Job? = null

    init {
        viewModelScope.launch {
            repository.checkAndSeedInitialPeerMessages()
        }
        startPeerChatterEngine()
        startExpiredMessagesCleaner()
    }

    private fun generateRandomHandle(): String {
        val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
        val suffix = (1..4).map { chars.random() }.joinToString("")
        return "User_$suffix"
    }

    fun reRollHandle() {
        _myHandle.value = generateRandomHandle()
    }

    fun setSelfDestructSeconds(seconds: Int) {
        _selfDestructSeconds.value = seconds
    }

    fun toggleScreenshotBlocked(blocked: Boolean) {
        _isScreenshotBlocked.value = blocked
    }

    // --- Message Sending ---

    fun sendTextMessage(text: String) {
        if (text.isBlank()) return
        val now = System.currentTimeMillis()
        val destructSec = _selfDestructSeconds.value
        val expiresAt = if (destructSec > 0) now + (destructSec * 1000L) else 0L

        val message = ChatMessage(
            id = UUID.randomUUID().toString(),
            senderHandle = _myHandle.value,
            senderColorHex = 0xFF00F0FF,
            isMe = true,
            content = text.trim(),
            messageType = "TEXT",
            selfDestructSeconds = destructSec,
            expiresAt = expiresAt,
            timestamp = now
        )
        viewModelScope.launch {
            repository.sendMessage(message)
            SoundEffects.playRadioChirp()
            triggerAutonomousPeerReply(text)
        }
    }

    fun sendImageMessage(uriString: String, caption: String = "") {
        val now = System.currentTimeMillis()
        val destructSec = _selfDestructSeconds.value
        val expiresAt = if (destructSec > 0) now + (destructSec * 1000L) else 0L

        val message = ChatMessage(
            id = UUID.randomUUID().toString(),
            senderHandle = _myHandle.value,
            senderColorHex = 0xFF00F0FF,
            isMe = true,
            content = caption,
            messageType = "IMAGE",
            mediaUri = uriString,
            selfDestructSeconds = destructSec,
            expiresAt = expiresAt,
            timestamp = now
        )
        viewModelScope.launch {
            repository.sendMessage(message)
            triggerAutonomousPeerReaction(message.id)
        }
    }

    fun sendVideoMessage(uriString: String, caption: String = "Intel footage packet") {
        val now = System.currentTimeMillis()
        val destructSec = _selfDestructSeconds.value
        val expiresAt = if (destructSec > 0) now + (destructSec * 1000L) else 0L

        val message = ChatMessage(
            id = UUID.randomUUID().toString(),
            senderHandle = _myHandle.value,
            senderColorHex = 0xFF00F0FF,
            isMe = true,
            content = caption,
            messageType = "VIDEO",
            mediaUri = uriString,
            selfDestructSeconds = destructSec,
            expiresAt = expiresAt,
            timestamp = now
        )
        viewModelScope.launch {
            repository.sendMessage(message)
        }
    }

    fun sendDoodleMessage(fileUri: String) {
        val now = System.currentTimeMillis()
        val destructSec = _selfDestructSeconds.value
        val expiresAt = if (destructSec > 0) now + (destructSec * 1000L) else 0L

        val message = ChatMessage(
            id = UUID.randomUUID().toString(),
            senderHandle = _myHandle.value,
            senderColorHex = 0xFF00F0FF,
            isMe = true,
            content = "Encrypted Sketch / Schema",
            messageType = "DOODLE",
            mediaUri = fileUri,
            selfDestructSeconds = destructSec,
            expiresAt = expiresAt,
            timestamp = now
        )
        viewModelScope.launch {
            repository.sendMessage(message)
            triggerAutonomousPeerReaction(message.id)
        }
    }

    fun sendSticker(stickerName: String, emoji: String) {
        val now = System.currentTimeMillis()
        val destructSec = _selfDestructSeconds.value
        val expiresAt = if (destructSec > 0) now + (destructSec * 1000L) else 0L

        val message = ChatMessage(
            id = UUID.randomUUID().toString(),
            senderHandle = _myHandle.value,
            senderColorHex = 0xFF00F0FF,
            isMe = true,
            content = "[$emoji $stickerName]",
            messageType = "STICKER",
            mediaUri = stickerName,
            selfDestructSeconds = destructSec,
            expiresAt = expiresAt,
            timestamp = now
        )
        viewModelScope.launch {
            repository.sendMessage(message)
        }
    }

    // --- Voice Recording & Playback ---

    fun startVoiceRecording(): Boolean {
        val started = audioUtils.startRecording()
        _isRecording.value = true
        _recordingDurationSec.value = 0
        recordingTimerJob?.cancel()
        recordingTimerJob = viewModelScope.launch {
            while (isActive && _isRecording.value) {
                delay(1000)
                _recordingDurationSec.value += 1
            }
        }
        return started
    }

    fun stopAndSendVoiceRecording(cancelled: Boolean = false) {
        if (!_isRecording.value) return
        _isRecording.value = false
        recordingTimerJob?.cancel()

        if (cancelled) {
            audioUtils.stopRecording()
            return
        }

        val result = audioUtils.stopRecording()
        if (result != null) {
            val now = System.currentTimeMillis()
            val destructSec = _selfDestructSeconds.value
            val expiresAt = if (destructSec > 0) now + (destructSec * 1000L) else 0L

            val message = ChatMessage(
                id = UUID.randomUUID().toString(),
                senderHandle = _myHandle.value,
                senderColorHex = 0xFF00F0FF,
                isMe = true,
                content = "Encrypted voice transmission [0:${result.durationSeconds.toString().padStart(2, '0')}]",
                messageType = "VOICE",
                mediaUri = result.fileUri,
                mediaDurationSec = result.durationSeconds,
                waveformSample = result.waveform,
                selfDestructSeconds = destructSec,
                expiresAt = expiresAt,
                timestamp = now
            )
            viewModelScope.launch {
                repository.sendMessage(message)
                SoundEffects.playRadioChirp()
            }
        }
    }

    fun toggleAudioPlayback(messageId: String, uri: String?) {
        if (_playingAudioMessageId.value == messageId) {
            audioUtils.stopPlayback()
            _playingAudioMessageId.value = null
        } else {
            _playingAudioMessageId.value = messageId
            if (uri != null) {
                audioUtils.playAudio(uri) {
                    _playingAudioMessageId.value = null
                }
            } else {
                // Simulated voice preview
                viewModelScope.launch {
                    delay(3000)
                    _playingAudioMessageId.value = null
                }
            }
        }
    }

    // --- Reactions & Actions ---

    fun toggleReaction(messageId: String, emoji: String) {
        viewModelScope.launch {
            repository.toggleReaction(messageId, emoji)
        }
    }

    fun deleteMessage(id: String) {
        viewModelScope.launch {
            repository.deleteMessage(id)
        }
    }

    fun purgeRoom() {
        viewModelScope.launch {
            repository.purgeAll()
        }
    }

    // --- Voice Call Simulation (WebRTC Group Comms) ---

    fun startVoiceCall() {
        _isInVoiceCall.value = true
        _callDurationSeconds.value = 0
        _callParticipants.value = listOf(
            CallParticipant(_myHandle.value, 0xFF00F0FF, isSpeaking = false),
            CallParticipant("Cipher_91f", 0xFF10B981, isSpeaking = true),
            CallParticipant("Phantom_8b", 0xFF8B5CF6, isSpeaking = false),
            CallParticipant("NeonDrifter", 0xFFFF0055, isSpeaking = false)
        )

        viewModelScope.launch {
            SoundEffects.playCallConnectedTone()
        }

        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            while (isActive && _isInVoiceCall.value) {
                delay(1000)
                _callDurationSeconds.value += 1
            }
        }

        callSimulationJob?.cancel()
        callSimulationJob = viewModelScope.launch {
            while (isActive && _isInVoiceCall.value) {
                delay(2400)
                // Alternate speaking participants
                val current = _callParticipants.value.toMutableList()
                val randomIndex = Random.nextInt(current.size)
                for (i in current.indices) {
                    current[i] = current[i].copy(isSpeaking = i == randomIndex)
                }
                _callParticipants.value = current
            }
        }
    }

    fun toggleCallMute() {
        _isCallMuted.value = !_isCallMuted.value
        val list = _callParticipants.value.map {
            if (it.handle == _myHandle.value) it.copy(isMuted = _isCallMuted.value) else it
        }
        _callParticipants.value = list
    }

    fun toggleCallSpeaker() {
        _isCallSpeaker.value = !_isCallSpeaker.value
    }

    fun endVoiceCall() {
        _isInVoiceCall.value = false
        callTimerJob?.cancel()
        callSimulationJob?.cancel()
        audioUtils.stopPlayback()
    }

    // --- Autonomous Secret Peer Chatter Engine ---

    private fun startPeerChatterEngine() {
        peerSimulationJob = viewModelScope.launch {
            while (isActive) {
                // Send an autonomous incoming message every 30 to 50 seconds
                delay(Random.nextLong(28000, 52000))
                val peerPool = listOf(
                    Triple("Cipher_91f", 0xFF10B981L, listOf(
                        "Node relay 0x4B verified. Ping 14ms.",
                        "Reminder: Keep notes updated so screen locks don't attract suspicion.",
                        "Anyone testing the new encrypted drawing tool?",
                        "Relay bandwidth expanded. High-fidelity voice notes operational."
                    )),
                    Triple("Ghost_x3p", 0xFF00F0FFL, listOf(
                        "Traffic spike detected on public gateway, zero leaks logged.",
                        "All packets scrambled. Zero footprint left.",
                        "Using the Panic button cleans UI cache immediately.",
                        "Encrypted channels 100% active."
                    )),
                    Triple("NeonDrifter", 0xFFFF0055L, listOf(
                        "Dropped a new secure drop in the archives.",
                        "Doodle sketches are end-to-end encrypted.",
                        "Secret trigger #APAP is solid.",
                        "Ready for voice comms if anyone needs to coordinate."
                    )),
                    Triple("Phantom_8b", 0xFF8B5CF6L, listOf(
                        "Self-destruct timer set to 30s is recommended for high-sec intel.",
                        "All good on this end. Signal strong.",
                        "Checking in from encrypted relay #7."
                    ))
                )
                val peer = peerPool.random()
                val text = peer.third.random()
                val now = System.currentTimeMillis()
                val msg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    senderHandle = peer.first,
                    senderColorHex = peer.second,
                    isMe = false,
                    content = text,
                    messageType = "TEXT",
                    timestamp = now
                )
                repository.sendMessage(msg)
            }
        }
    }

    private fun triggerAutonomousPeerReply(userMessage: String) {
        viewModelScope.launch {
            delay(Random.nextLong(2500, 4800))
            val replies = listOf(
                "Acknowledged.",
                "Copy that, packet verified.",
                "10-4. Transmission received loud and clear.",
                "Affirmative. Relaying to sub-nodes.",
                "Received on secure channel.",
                "Understood. Zero traces logged."
            )
            val peers = listOf(
                Pair("Cipher_91f", 0xFF10B981L),
                Pair("Ghost_x3p", 0xFF00F0FFL),
                Pair("NeonDrifter", 0xFFFF0055L),
                Pair("Phantom_8b", 0xFF8B5CF6L)
            )
            val peer = peers.random()
            val replyMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                senderHandle = peer.first,
                senderColorHex = peer.second,
                isMe = false,
                content = replies.random(),
                messageType = "TEXT",
                timestamp = System.currentTimeMillis()
            )
            repository.sendMessage(replyMsg)
            SoundEffects.playRadioChirp()
        }
    }

    private fun triggerAutonomousPeerReaction(messageId: String) {
        viewModelScope.launch {
            delay(Random.nextLong(1500, 3500))
            val emojis = listOf("🔥", "⚡", "🤫", "💀")
            repository.toggleReaction(messageId, emojis.random())
        }
    }

    private fun startExpiredMessagesCleaner() {
        expiredMessagesCleanerJob = viewModelScope.launch {
            while (isActive) {
                delay(3000)
                repository.deleteExpired(System.currentTimeMillis())
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioUtils.stopPlayback()
        peerSimulationJob?.cancel()
        expiredMessagesCleanerJob?.cancel()
        recordingTimerJob?.cancel()
        callTimerJob?.cancel()
        callSimulationJob?.cancel()
    }
}

class SecretChatViewModelFactory(
    private val application: Application,
    private val repository: ChatRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SecretChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SecretChatViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
