package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ResearchAudioLogEntity
import com.example.data.model.ResearchMediaSessionEntity
import com.example.data.model.ResearchSurveyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ResearchDao {

    // --- Survey Responses & Demographics ---
    @Query("SELECT * FROM research_surveys ORDER BY timestamp DESC")
    fun getAllSurveys(): Flow<List<ResearchSurveyEntity>>

    @Query("SELECT COUNT(*) FROM research_surveys")
    fun getSurveysCount(): Flow<Int>

    @Query("SELECT sha256Hash FROM research_surveys ORDER BY id DESC LIMIT 1")
    suspend fun getLatestSurveyHash(): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurvey(survey: ResearchSurveyEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurveys(surveys: List<ResearchSurveyEntity>)

    @Update
    suspend fun updateSurvey(survey: ResearchSurveyEntity)

    // --- Short-Form Media Tracking Sessions ---
    @Query("SELECT * FROM research_media_sessions ORDER BY timestamp DESC")
    fun getAllMediaSessions(): Flow<List<ResearchMediaSessionEntity>>

    @Query("SELECT COUNT(*) FROM research_media_sessions")
    fun getMediaSessionsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaSession(session: ResearchMediaSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaSessions(sessions: List<ResearchMediaSessionEntity>)

    // --- Audio Interview Voice Logs ("records what is said in the research") ---
    @Query("SELECT * FROM research_audio_logs ORDER BY timestamp DESC")
    fun getAllAudioLogs(): Flow<List<ResearchAudioLogEntity>>

    @Query("SELECT COUNT(*) FROM research_audio_logs")
    fun getAudioLogsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudioLog(log: ResearchAudioLogEntity): Long

    // --- Utilities ---
    @Query("DELETE FROM research_surveys")
    suspend fun clearSurveys()

    @Query("DELETE FROM research_media_sessions")
    suspend fun clearMediaSessions()

    @Query("DELETE FROM research_audio_logs")
    suspend fun clearAudioLogs()
}
