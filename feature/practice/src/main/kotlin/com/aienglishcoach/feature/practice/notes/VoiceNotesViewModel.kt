package com.aienglishcoach.feature.practice.notes

import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aienglishcoach.core.common.result.AppError
import com.aienglishcoach.core.domain.model.VoiceNote
import com.aienglishcoach.core.domain.service.AudioRecorderService
import com.aienglishcoach.core.domain.usecase.notes.DeleteVoiceNoteUseCase
import com.aienglishcoach.core.domain.usecase.notes.ObserveVoiceNotesUseCase
import com.aienglishcoach.core.domain.usecase.notes.SaveVoiceNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Voice memos: record with a live elapsed-time counter, name and save the
 * recording, play notes back with [MediaPlayer] and delete them after
 * confirmation.
 */
@HiltViewModel
class VoiceNotesViewModel @Inject constructor(
    observeVoiceNotes: ObserveVoiceNotesUseCase,
    private val saveVoiceNote: SaveVoiceNoteUseCase,
    private val deleteVoiceNote: DeleteVoiceNoteUseCase,
    private val audioRecorder: AudioRecorderService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoiceNotesUiState())
    val uiState: StateFlow<VoiceNotesUiState> = _uiState.asStateFlow()

    private var recordingPath: String? = null
    private var tickerJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null

    init {
        observeVoiceNotes()
            .onEach { notes -> _uiState.update { it.copy(isLoading = false, notes = notes) } }
            .launchIn(viewModelScope)
    }

    fun startRecording() {
        if (_uiState.value.isRecording) return
        stopPlayback()
        try {
            recordingPath = audioRecorder.start()
        } catch (throwable: Throwable) {
            Timber.w(throwable, "Failed to start audio recording")
            _uiState.update { it.copy(error = AppError.Storage(throwable.message)) }
            return
        }
        _uiState.update { it.copy(isRecording = true, recordingSeconds = 0, error = null) }
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (isActive) {
                delay(TICK_MILLIS)
                _uiState.update { it.copy(recordingSeconds = it.recordingSeconds + 1) }
            }
        }
    }

    /** Stops recording and opens the title dialog for the pending memo. */
    fun stopRecording() {
        if (!_uiState.value.isRecording) return
        tickerJob?.cancel()
        val durationSeconds = try {
            audioRecorder.stop()
        } catch (throwable: Throwable) {
            Timber.w(throwable, "Failed to stop audio recording")
            _uiState.update {
                it.copy(isRecording = false, error = AppError.Storage(throwable.message))
            }
            return
        }
        val path = recordingPath
        _uiState.update {
            it.copy(
                isRecording = false,
                pendingRecording = path?.let { audioPath ->
                    PendingRecording(audioPath = audioPath, durationSeconds = durationSeconds)
                },
            )
        }
    }

    /** Persists the pending recording under the given title. */
    fun savePendingRecording(title: String) {
        val pending = _uiState.value.pendingRecording ?: return
        _uiState.update { it.copy(pendingRecording = null) }
        viewModelScope.launch {
            try {
                saveVoiceNote(
                    title = title,
                    audioPath = pending.audioPath,
                    durationSeconds = pending.durationSeconds,
                )
            } catch (throwable: Throwable) {
                _uiState.update { it.copy(error = AppError.Storage(throwable.message)) }
            }
        }
    }

    fun discardPendingRecording() {
        _uiState.update { it.copy(pendingRecording = null) }
    }

    /** Starts playback of [note], or stops it when it is already playing. */
    fun togglePlayback(note: VoiceNote) {
        if (_uiState.value.playingNoteId == note.id) {
            stopPlayback()
            return
        }
        stopPlayback()
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(note.audioPath)
                setOnCompletionListener { stopPlayback() }
                prepare()
                start()
            }
            _uiState.update { it.copy(playingNoteId = note.id) }
        } catch (throwable: Throwable) {
            Timber.w(throwable, "Failed to play voice note %d", note.id)
            stopPlayback()
            _uiState.update { it.copy(error = AppError.Storage(throwable.message)) }
        }
    }

    private fun stopPlayback() {
        mediaPlayer?.let { player ->
            runCatching { player.stop() }
            player.release()
        }
        mediaPlayer = null
        _uiState.update { it.copy(playingNoteId = null) }
    }

    fun requestDelete(note: VoiceNote) {
        _uiState.update { it.copy(noteToDelete = note) }
    }

    fun dismissDelete() {
        _uiState.update { it.copy(noteToDelete = null) }
    }

    fun confirmDelete() {
        val note = _uiState.value.noteToDelete ?: return
        if (_uiState.value.playingNoteId == note.id) stopPlayback()
        _uiState.update { it.copy(noteToDelete = null) }
        viewModelScope.launch {
            try {
                deleteVoiceNote(note.id)
            } catch (throwable: Throwable) {
                _uiState.update { it.copy(error = AppError.Storage(throwable.message)) }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        tickerJob?.cancel()
        if (audioRecorder.isRecording()) {
            runCatching { audioRecorder.stop() }
        }
        stopPlayback()
        super.onCleared()
    }

    private companion object {
        const val TICK_MILLIS = 1_000L
    }
}

/** A finished recording waiting for a title before it is saved. */
data class PendingRecording(
    val audioPath: String,
    val durationSeconds: Int,
)

/** Immutable state of the voice notes screen. */
data class VoiceNotesUiState(
    val isLoading: Boolean = true,
    val notes: List<VoiceNote> = emptyList(),
    val isRecording: Boolean = false,
    /** Elapsed recording time in seconds while [isRecording]. */
    val recordingSeconds: Int = 0,
    /** Non-null while the save-title dialog should be shown. */
    val pendingRecording: PendingRecording? = null,
    /** Id of the note currently playing, or null. */
    val playingNoteId: Long? = null,
    /** Note awaiting delete confirmation, or null. */
    val noteToDelete: VoiceNote? = null,
    val error: AppError? = null,
)
