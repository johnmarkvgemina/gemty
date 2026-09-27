package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.model.ResearchAudioLogEntity
import com.example.data.model.ResearchMediaSessionEntity
import com.example.data.model.ResearchStatistics
import com.example.data.model.ResearchSurveyEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportUtils {

    fun generateSurveysCsv(surveys: List<ResearchSurveyEntity>): String {
        val sb = StringBuilder()
        sb.append("Respondent_Code,Timestamp,Age,Sex,Strand_Grade,Section,Weekly_Allowance,Platforms,Daily_Hours_Est,")
        sb.append("Platform_Awareness_Mean,Frequency_Awareness_Mean,TimeSpent_Awareness_Mean,Overall_Awareness_Mean,")
        sb.append("Accurate_Recording_Mean,Easy_DataEntry_Mean,Useful_Features_Mean,Overall_Functionality_Mean,")
        sb.append("Impulse_Buying_Score,SHA256_Hash,Tamper_Status\n")

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        for (s in surveys) {
            val dateStr = sdf.format(Date(s.timestamp))
            val tamperStatus = if (s.isUntampered()) "VERIFIED_UNTAMPERED" else "TAMPER_DETECTED"
            sb.append("\"${s.participantCode}\",\"$dateStr\",${s.age},\"${s.sex}\",\"${s.strandAndGrade}\",\"${s.section}\",")
            sb.append("\"${s.weeklyAllowance}\",\"${s.primaryPlatforms}\",${s.dailyHoursEstimate},")
            sb.append("${s.platformAwarenessMean},${s.frequencyAwarenessMean},${s.timeSpentAwarenessMean},${s.overallAwarenessMean},")
            sb.append("${s.accurateRecordingMean},${s.easyDataEntryMean},${s.usefulFeaturesMean},${s.overallFunctionalityMean},")
            sb.append("${s.overallImpulseScore},\"${s.sha256Hash}\",\"$tamperStatus\"\n")
        }
        return sb.toString()
    }

    fun generateSessionsCsv(sessions: List<ResearchMediaSessionEntity>): String {
        val sb = StringBuilder()
        sb.append("Session_ID,Participant_Code,Timestamp,Platform,Category,Duration_Seconds,Impulse_Triggered,Product_Viewed,Price_PHP,Reflection_Notes,SHA256_Hash\n")

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        for (sess in sessions) {
            val dateStr = sdf.format(Date(sess.timestamp))
            sb.append("${sess.id},\"${sess.participantCode}\",\"$dateStr\",\"${sess.platform}\",\"${sess.contentCategory}\",")
            sb.append("${sess.durationSeconds},${sess.impulseBuyUrgeTriggered},\"${sess.productNameViewed}\",${sess.estimatedPricePhp},")
            sb.append("\"${sess.reflectionNotes.replace("\"", "\"\"")}\",\"${sess.sha256Hash}\"\n")
        }
        return sb.toString()
    }

    fun generateResearchSummaryReport(
        stats: ResearchStatistics,
        surveys: List<ResearchSurveyEntity>,
        sessions: List<ResearchMediaSessionEntity>,
        audioLogs: List<ResearchAudioLogEntity>
    ): String {
        val sdf = SimpleDateFormat("MMMM dd, yyyy HH:mm", Locale.US)
        val sb = StringBuilder()
        sb.append("================================================================================\n")
        sb.append("RESEARCH STUDY DATA VAULT REPORT\n")
        sb.append("Title: SCROLL: A Smart System for Monitoring Short-Form Media Consumption Habits\n")
        sb.append("Target Population: Grade 11 ABM Students, Lawang Bato National High School\n")
        sb.append("Generated On: ${sdf.format(Date())}\n")
        sb.append("================================================================================\n\n")

        sb.append("1. SAMPLING & COCHRAN'S FORMULA METRICS\n")
        sb.append("- Total Population (N): ${stats.totalPopulation} Grade 11 ABM learners\n")
        sb.append("- Cochran Sample Size Target (n): ${stats.targetSampleSize} respondents (at 5% margin of error, 95% confidence)\n")
        sb.append("- Current Sample Logged: ${stats.sampleSize} (${String.format(Locale.US, "%.1f", stats.progressPercentage)}% target reached)\n")
        sb.append("- Media Session Observations: ${sessions.size} logged sessions\n")
        sb.append("- Research Oral Interview Audio Logs: ${audioLogs.size} recorded entries\n\n")

        sb.append("2. CRYPTOGRAPHIC TAMPER-PROOF INTEGRITY AUDIT\n")
        sb.append("- Total Logged Records Audited: ${stats.totalRecordsAudited}\n")
        sb.append("- Tamper Verification Status: ${if (stats.isSystemIntegrityValid) "100% UNTAMPERED & VERIFIED SECURE" else "WARNING: TAMPERING DETECTED"}\n")
        sb.append("- Tampered Entries Detected: ${stats.tamperedCount}\n")
        sb.append("- Hashing Algorithm: SHA-256 with Cryptographic Seal on Submission\n\n")

        sb.append("3. WEIGHTED MEAN PERCEIVED FUNCTIONALITY OF SCROLL (IV)\n")
        for (item in stats.functionalityIndicators) {
            sb.append("- ${item.indicatorName}: ${String.format(Locale.US, "%.2f", item.weightedMean)} (${item.interpretation})\n")
        }
        sb.append("-> OVERALL TRACKER FUNCTIONALITY MEAN: ${String.format(Locale.US, "%.2f", stats.overallFunctionalityMean)}\n\n")

        sb.append("4. WEIGHTED MEAN DIGITAL MEDIA USE AWARENESS (DV)\n")
        for (item in stats.awarenessIndicators) {
            sb.append("- ${item.indicatorName}: ${String.format(Locale.US, "%.2f", item.weightedMean)} (${item.interpretation})\n")
        }
        sb.append("-> OVERALL DIGITAL MEDIA AWARENESS MEAN: ${String.format(Locale.US, "%.2f", stats.overallAwarenessMean)}\n\n")

        sb.append("5. PEARSON PRODUCT-MOMENT CORRELATION TEST (Chapter III Hypothesis)\n")
        val corr = stats.functionalityVsAwarenessCorrelation
        sb.append("- Computed Pearson r: ${corr.r}\n")
        sb.append("- Coefficient of Determination (r²): ${corr.rSquared}\n")
        sb.append("- Computed t-statistic: ${corr.tStatistic} (df = ${corr.degreesOfFreedom})\n")
        sb.append("- Correlation Strength: ${corr.interpretation}\n")
        sb.append("- Hypothesis Decision: ${corr.hypothesisVerdict}\n\n")

        sb.append("================================================================================\n")
        sb.append("END OF AUDITED RESEARCH REPORT - SCROLL SYSTEM 2026\n")
        return sb.toString()
    }

    fun shareExportFile(context: Context, filename: String, content: String, mimeType: String = "text/plain") {
        try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()
            val file = File(exportDir, filename)
            file.writeText(content)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "SCROLL Research Data: $filename")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share or Download $filename").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            // Fallback plain text share if file provider not yet declared
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, content)
                putExtra(Intent.EXTRA_SUBJECT, "SCROLL Research Export: $filename")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(sendIntent, "Export $filename").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }
}
