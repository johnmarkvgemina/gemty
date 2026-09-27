package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.util.CryptoUtils

@Entity(tableName = "research_surveys")
data class ResearchSurveyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val participantCode: String,
    val timestamp: Long = System.currentTimeMillis(),

    // --- Part I: Demographic Profile (Chapter III) ---
    val age: Int, // e.g. 16, 17
    val sex: String, // "Male", "Female", "Prefer not to say"
    val strandAndGrade: String = "Grade 11 ABM", // Lawang Bato National High School
    val section: String, // e.g. "ABM-1", "ABM-2", "ABM-3", "ABM-4"
    val weeklyAllowance: String, // e.g. "₱200–₱500", "₱501–₱1,000", etc.
    val primaryPlatforms: String, // "TikTok, Instagram Reels, YouTube Shorts"
    val dailyHoursEstimate: Float, // Estimated hours spent scrolling daily

    // --- Part II: Digital Media Use Awareness (4-Point Likert Scale) ---
    // 4 = Strongly Agree / Very High, 3 = Agree / High, 2 = Disagree / Low, 1 = Strongly Disagree / Very Low
    val platformAwareness1: Int = 4, // Aware of differences in TikTok vs Reels vs Shorts algorithm
    val platformAwareness2: Int = 4, // Notice how platforms push different creator styles
    val frequencyAwareness1: Int = 4, // Conscious of opening social apps multiple times during school breaks
    val frequencyAwareness2: Int = 3, // Aware of habitual reflex to open video apps without specific goal
    val timeSpentAwareness1: Int = 3, // Mindful of actual minutes spent watching short clips vs perceived time
    val timeSpentAwareness2: Int = 4, // Recognize when continuous autoplay causes extended viewing

    // --- Part III: SCROLL Tracker Functionality (4-Point Likert Scale) ---
    val accurateRecording1: Int = 4, // SCROLL accurately logs session duration
    val accurateRecording2: Int = 4, // Accurately categorizes commercial vs non-commercial clips
    val easyDataEntry1: Int = 4, // Inputting session and habit data is effortless and fast
    val easyDataEntry2: Int = 4, // Interface is clear and requires minimal cognitive load
    val usefulFeatures1: Int = 4, // Visual breakdowns and alerts raise consciousness of scrolling
    val usefulFeatures2: Int = 4, // Reflection prompts assist in identifying impulse buying triggers

    // --- Part IV: Consumer Buying Techniques & Impulse Buying (4-Point Likert Scale) ---
    val purchaseIntention1: Int = 3, // Encountering "Budol" / TikTok Shop links creates urge to check out
    val purchaseIntention2: Int = 3, // Micro-influencer GRWM / unboxings increase product curiosity
    val productPreference1: Int = 3, // Prefer purchasing items trending on short-form feeds
    val impulseBuying1: Int = 3, // Spend daily/weekly allowance spontaneously on viral items
    val impulseBuying2: Int = 2, // Compare prices and verify budget before completing checkout

    // --- Immutability & Tamper-Proof Safeguards ---
    val isLocked: Boolean = true,
    val sha256Hash: String = "",
    val previousHash: String = "GENESIS_BLOCK"
) {
    fun computeHash(): String {
        val payload = "$id|$participantCode|$timestamp|$age|$sex|$section|$weeklyAllowance|" +
                "$platformAwareness1,$platformAwareness2,$frequencyAwareness1,$frequencyAwareness2,$timeSpentAwareness1,$timeSpentAwareness2|" +
                "$accurateRecording1,$accurateRecording2,$easyDataEntry1,$easyDataEntry2,$usefulFeatures1,$usefulFeatures2|" +
                "$purchaseIntention1,$purchaseIntention2,$productPreference1,$impulseBuying1,$impulseBuying2"
        return CryptoUtils.sha256(payload)
    }

    fun isUntampered(): Boolean {
        return sha256Hash.isNotEmpty() && sha256Hash == computeHash()
    }

    // Computed sub-variable averages (1.00 to 4.00)
    val platformAwarenessMean: Float
        get() = (platformAwareness1 + platformAwareness2) / 2.0f

    val frequencyAwarenessMean: Float
        get() = (frequencyAwareness1 + frequencyAwareness2) / 2.0f

    val timeSpentAwarenessMean: Float
        get() = (timeSpentAwareness1 + timeSpentAwareness2) / 2.0f

    val overallAwarenessMean: Float
        get() = (platformAwarenessMean + frequencyAwarenessMean + timeSpentAwarenessMean) / 3.0f

    val accurateRecordingMean: Float
        get() = (accurateRecording1 + accurateRecording2) / 2.0f

    val easyDataEntryMean: Float
        get() = (easyDataEntry1 + easyDataEntry2) / 2.0f

    val usefulFeaturesMean: Float
        get() = (usefulFeatures1 + usefulFeatures2) / 2.0f

    val overallFunctionalityMean: Float
        get() = (accurateRecordingMean + easyDataEntryMean + usefulFeaturesMean) / 3.0f

    val overallImpulseScore: Float
        get() = (purchaseIntention1 + purchaseIntention2 + productPreference1 + impulseBuying1) / 4.0f
}

@Entity(tableName = "research_media_sessions")
data class ResearchMediaSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val participantCode: String,
    val timestamp: Long = System.currentTimeMillis(),
    val platform: String, // "TikTok", "Instagram Reels", "YouTube Shorts"
    val contentCategory: String, // "E-Commerce / Budol", "Influencer GRWM / Unboxing", "Entertainment / Comedy", "Educational / ABM", "Lifestyle / Viral"
    val durationSeconds: Long,
    val impulseBuyUrgeTriggered: Boolean,
    val productNameViewed: String,
    val estimatedPricePhp: Double,
    val reflectionNotes: String,
    val isLocked: Boolean = true,
    val sha256Hash: String = ""
) {
    fun computeHash(): String {
        val payload = "$id|$participantCode|$timestamp|$platform|$contentCategory|$durationSeconds|$impulseBuyUrgeTriggered|$productNameViewed|$estimatedPricePhp|$reflectionNotes"
        return CryptoUtils.sha256(payload)
    }

    fun isUntampered(): Boolean {
        return sha256Hash.isNotEmpty() && sha256Hash == computeHash()
    }
}

@Entity(tableName = "research_audio_logs")
data class ResearchAudioLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val participantCode: String,
    val timestamp: Long = System.currentTimeMillis(),
    val title: String,
    val topic: String, // "Oral Debriefing", "Short-Form Habit Reflection", "Budol Impulse Interview", "Usability Feedback"
    val audioFilePath: String,
    val durationSeconds: Int,
    val transcriptNotes: String, // What was said during the research interview / observation
    val isLocked: Boolean = true,
    val sha256Hash: String = ""
) {
    fun computeHash(): String {
        val payload = "$id|$participantCode|$timestamp|$title|$topic|$audioFilePath|$durationSeconds|$transcriptNotes"
        return CryptoUtils.sha256(payload)
    }

    fun isUntampered(): Boolean {
        return sha256Hash.isNotEmpty() && sha256Hash == computeHash()
    }
}
