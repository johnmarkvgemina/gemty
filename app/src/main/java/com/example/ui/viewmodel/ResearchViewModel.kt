package com.example.ui.viewmodel

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ResearchAudioLogEntity
import com.example.data.model.ResearchMediaSessionEntity
import com.example.data.model.ResearchStatistics
import com.example.data.model.ResearchSurveyEntity
import com.example.data.repository.ResearchRepository
import com.example.util.AudioPlayerHelper
import com.example.util.AudioRecorderHelper
import com.example.util.ExportUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

enum class AppMode {
    PARTICIPANT_APP,
    RESEARCHER_VAULT
}

enum class ParticipantTab {
    DEMOGRAPHIC_QUESTIONNAIRE,
    MEDIA_SESSION_TRACKER,
    ORAL_INTERVIEW_RECORDER,
    MY_SEALED_LOGS
}

enum class ResearcherTab {
    LIVE_ANALYTICS,
    INCOMING_LOGS,
    ORAL_AUDIO_VAULT,
    TAMPER_PROOF_AUDIT,
    EXPORT_DATASET
}

class ResearchViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = ResearchRepository(db.researchDao())

    val audioRecorder = AudioRecorderHelper(application)
    val audioPlayer = AudioPlayerHelper(application)

    // Current App Mode Switcher
    private val _appMode = MutableStateFlow(AppMode.PARTICIPANT_APP)
    val appMode: StateFlow<AppMode> = _appMode.asStateFlow()

    private val _participantTab = MutableStateFlow(ParticipantTab.DEMOGRAPHIC_QUESTIONNAIRE)
    val participantTab: StateFlow<ParticipantTab> = _participantTab.asStateFlow()

    private val _researcherTab = MutableStateFlow(ResearcherTab.LIVE_ANALYTICS)
    val researcherTab: StateFlow<ResearcherTab> = _researcherTab.asStateFlow()

    // Database Flow streams
    val allSurveys: StateFlow<List<ResearchSurveyEntity>> = repository.allSurveys
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMediaSessions: StateFlow<List<ResearchMediaSessionEntity>> = repository.allMediaSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAudioLogs: StateFlow<List<ResearchAudioLogEntity>> = repository.allAudioLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val liveStatistics: StateFlow<ResearchStatistics?> = repository.liveStatistics
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- Participant Form State ---
    val participantCode = MutableStateFlow("RESP-${UUID.randomUUID().toString().take(4).uppercase()}")
    val age = MutableStateFlow(16)
    val sex = MutableStateFlow("Female")
    val section = MutableStateFlow("ABM-1")
    val weeklyAllowance = MutableStateFlow("₱200–₱500")
    val selectedPlatforms = MutableStateFlow(setOf("TikTok", "Instagram Reels", "YouTube Shorts"))
    val dailyHoursEstimate = MutableStateFlow(3.0f)

    // Likert responses (1 to 4)
    val platAwareness1 = MutableStateFlow(4)
    val platAwareness2 = MutableStateFlow(4)
    val freqAwareness1 = MutableStateFlow(4)
    val freqAwareness2 = MutableStateFlow(3)
    val timeAwareness1 = MutableStateFlow(3)
    val timeAwareness2 = MutableStateFlow(4)

    val accRec1 = MutableStateFlow(4)
    val accRec2 = MutableStateFlow(4)
    val easyData1 = MutableStateFlow(4)
    val easyData2 = MutableStateFlow(4)
    val usefulFeat1 = MutableStateFlow(4)
    val usefulFeat2 = MutableStateFlow(4)

    val purchaseIntent1 = MutableStateFlow(3)
    val purchaseIntent2 = MutableStateFlow(3)
    val prodPref1 = MutableStateFlow(3)
    val impulseBuy1 = MutableStateFlow(3)
    val impulseBuy2 = MutableStateFlow(2)

    val isSurveySubmitted = MutableStateFlow(false)
    val lastSubmittedSeal = MutableStateFlow<String?>(null)

    // --- Media Session Tracker State ---
    val sessionPlatform = MutableStateFlow("TikTok")
    val sessionCategory = MutableStateFlow("E-Commerce / Budol")
    val isTimerActive = MutableStateFlow(false)
    val elapsedSeconds = MutableStateFlow(0L)
    val impulseUrgeTriggered = MutableStateFlow(false)
    val sessionProductViewed = MutableStateFlow("")
    val sessionPriceEstimate = MutableStateFlow("")
    val sessionReflection = MutableStateFlow("")
    private var timerJob: Job? = null

    // --- Audio Voice Recording State ("records what is said in the research") ---
    val isRecordingAudio = MutableStateFlow(false)
    val recordingDurationSeconds = MutableStateFlow(0)
    val audioInterviewTopic = MutableStateFlow("Short-Form Video & Impulse Buying Debrief")
    val audioTranscriptNotes = MutableStateFlow("")
    val lastSavedAudioPath = MutableStateFlow<String?>(null)
    private var audioTimerJob: Job? = null

    // Audio Playback
    val currentlyPlayingPath = MutableStateFlow<String?>(null)
    val isAudioPlaying = MutableStateFlow(false)

    // SnackBar / Toast feedback
    val userNotification = MutableStateFlow<String?>(null)

    fun setAppMode(mode: AppMode) {
        _appMode.value = mode
    }

    fun setParticipantTab(tab: ParticipantTab) {
        _participantTab.value = tab
    }

    fun setResearcherTab(tab: ResearcherTab) {
        _researcherTab.value = tab
    }

    fun togglePlatform(platform: String) {
        val current = selectedPlatforms.value.toMutableSet()
        if (current.contains(platform)) {
            if (current.size > 1) current.remove(platform)
        } else {
            current.add(platform)
        }
        selectedPlatforms.value = current
    }

    // Submit & Lock Questionnaire
    fun submitAndSealSurvey() {
        viewModelScope.launch {
            val survey = ResearchSurveyEntity(
                participantCode = participantCode.value,
                age = age.value,
                sex = sex.value,
                section = section.value,
                weeklyAllowance = weeklyAllowance.value,
                primaryPlatforms = selectedPlatforms.value.joinToString(", "),
                dailyHoursEstimate = dailyHoursEstimate.value,
                platformAwareness1 = platAwareness1.value,
                platformAwareness2 = platAwareness2.value,
                frequencyAwareness1 = freqAwareness1.value,
                frequencyAwareness2 = freqAwareness2.value,
                timeSpentAwareness1 = timeAwareness1.value,
                timeSpentAwareness2 = timeAwareness2.value,
                accurateRecording1 = accRec1.value,
                accurateRecording2 = accRec2.value,
                easyDataEntry1 = easyData1.value,
                easyDataEntry2 = easyData2.value,
                usefulFeatures1 = usefulFeat1.value,
                usefulFeatures2 = usefulFeat2.value,
                purchaseIntention1 = purchaseIntent1.value,
                purchaseIntention2 = purchaseIntent2.value,
                productPreference1 = prodPref1.value,
                impulseBuying1 = impulseBuy1.value,
                impulseBuying2 = impulseBuy2.value,
                isLocked = true
            )
            repository.submitSurvey(survey)
            lastSubmittedSeal.value = survey.computeHash()
            isSurveySubmitted.value = true
            userNotification.value = "Entry cryptographically sealed with SHA-256 and locked permanently."
        }
    }

    fun resetParticipantFormForNewStudent() {
        participantCode.value = "RESP-${UUID.randomUUID().toString().take(4).uppercase()}"
        isSurveySubmitted.value = false
        lastSubmittedSeal.value = null
    }

    // --- Session Tracker controls ---
    fun startSessionTimer() {
        if (isTimerActive.value) return
        isTimerActive.value = true
        timerJob = viewModelScope.launch {
            while (isTimerActive.value) {
                delay(1000)
                elapsedSeconds.value += 1
            }
        }
    }

    fun pauseSessionTimer() {
        isTimerActive.value = false
        timerJob?.cancel()
    }

    fun submitAndSealSession() {
        viewModelScope.launch {
            pauseSessionTimer()
            val finalDuration = if (elapsedSeconds.value > 0) elapsedSeconds.value else 180L
            val price = sessionPriceEstimate.value.toDoubleOrNull() ?: 0.0
            val session = ResearchMediaSessionEntity(
                participantCode = participantCode.value,
                platform = sessionPlatform.value,
                contentCategory = sessionCategory.value,
                durationSeconds = finalDuration,
                impulseBuyUrgeTriggered = impulseUrgeTriggered.value,
                productNameViewed = if (sessionProductViewed.value.isBlank()) "General Feed Content" else sessionProductViewed.value,
                estimatedPricePhp = price,
                reflectionNotes = if (sessionReflection.value.isBlank()) "Logged via SCROLL live tracker prototype" else sessionReflection.value,
                isLocked = true
            )
            repository.logMediaSession(session)
            // Reset session
            elapsedSeconds.value = 0L
            sessionProductViewed.value = ""
            sessionPriceEstimate.value = ""
            sessionReflection.value = ""
            impulseUrgeTriggered.value = false
            userNotification.value = "Short-form session locked into immutable research ledger."
        }
    }

    // --- Voice Recording controls ("records what is said in the research") ---
    fun startAudioRecording() {
        val file = audioRecorder.startRecording()
        if (file != null) {
            isRecordingAudio.value = true
            recordingDurationSeconds.value = 0
            audioTimerJob = viewModelScope.launch {
                while (isRecordingAudio.value) {
                    delay(1000)
                    recordingDurationSeconds.value += 1
                }
            }
            userNotification.value = "Recording research oral reflection..."
        } else {
            userNotification.value = "Microphone access required or failed to initialize."
        }
    }

    fun stopAudioRecording() {
        val file = audioRecorder.stopRecording()
        isRecordingAudio.value = false
        audioTimerJob?.cancel()
        if (file != null && file.exists()) {
            lastSavedAudioPath.value = file.absolutePath
            userNotification.value = "Audio captured (${recordingDurationSeconds.value}s). Add transcript/notes and seal."
        }
    }

    fun saveAndSealAudioLog() {
        val path = lastSavedAudioPath.value
        viewModelScope.launch {
            val audioEntity = ResearchAudioLogEntity(
                participantCode = participantCode.value,
                title = audioInterviewTopic.value,
                topic = "Research Interview & Habit Debrief",
                audioFilePath = path ?: "",
                durationSeconds = recordingDurationSeconds.value.coerceAtLeast(3),
                transcriptNotes = if (audioTranscriptNotes.value.isBlank()) "Oral feedback on short-form scrolling and financial awareness" else audioTranscriptNotes.value,
                isLocked = true
            )
            repository.saveAudioLog(audioEntity)
            lastSavedAudioPath.value = null
            audioTranscriptNotes.value = ""
            recordingDurationSeconds.value = 0
            userNotification.value = "Oral research log cryptographically sealed with SHA-256."
        }
    }

    fun playAudioFile(path: String) {
        if (path.isBlank()) {
            userNotification.value = "Audio file path is empty."
            return
        }
        currentlyPlayingPath.value = path
        isAudioPlaying.value = true
        audioPlayer.playAudio(path) {
            currentlyPlayingPath.value = null
            isAudioPlaying.value = false
        }
    }

    fun stopAudioPlayback() {
        audioPlayer.stopAudio()
        currentlyPlayingPath.value = null
        isAudioPlaying.value = false
    }

    // --- Researcher Vault actions ---
    fun seedBenchmarkData() {
        viewModelScope.launch {
            repository.seedBenchmark156Sample()
            userNotification.value = "Loaded 156 Lawang Bato NHS Grade 11 ABM respondents (Cochran target sample)."
        }
    }

    fun exportSurveysCsv() {
        val surveys = allSurveys.value
        val csv = ExportUtils.generateSurveysCsv(surveys)
        ExportUtils.shareExportFile(getApplication(), "SCROLL_Survey_Dataset_${System.currentTimeMillis()}.csv", csv, "text/csv")
    }

    fun exportSessionsCsv() {
        val sessions = allMediaSessions.value
        val csv = ExportUtils.generateSessionsCsv(sessions)
        ExportUtils.shareExportFile(getApplication(), "SCROLL_Media_Sessions_${System.currentTimeMillis()}.csv", csv, "text/csv")
    }

    fun exportSummaryReport() {
        val stats = liveStatistics.value ?: return
        val report = ExportUtils.generateResearchSummaryReport(
            stats,
            allSurveys.value,
            allMediaSessions.value,
            allAudioLogs.value
        )
        ExportUtils.shareExportFile(getApplication(), "SCROLL_Research_Report_${System.currentTimeMillis()}.txt", report, "text/plain")
    }

    fun clearNotification() {
        userNotification.value = null
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        audioTimerJob?.cancel()
        audioRecorder.cancelRecording()
        audioPlayer.stopAudio()
    }
}
