package com.example.data.repository

import com.example.data.local.ResearchDao
import com.example.data.model.CorrelationResult
import com.example.data.model.DemographicDistributionItem
import com.example.data.model.IndicatorMeanItem
import com.example.data.model.ResearchAudioLogEntity
import com.example.data.model.ResearchMediaSessionEntity
import com.example.data.model.ResearchStatistics
import com.example.data.model.ResearchSurveyEntity
import com.example.data.model.StatisticsCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlin.random.Random

class ResearchRepository(private val dao: ResearchDao) {

    val allSurveys: Flow<List<ResearchSurveyEntity>> = dao.getAllSurveys()
    val allMediaSessions: Flow<List<ResearchMediaSessionEntity>> = dao.getAllMediaSessions()
    val allAudioLogs: Flow<List<ResearchAudioLogEntity>> = dao.getAllAudioLogs()

    suspend fun submitSurvey(survey: ResearchSurveyEntity): Long {
        val latestHash = dao.getLatestSurveyHash() ?: "GENESIS_BLOCK"
        // Compute cryptographic seal
        val preliminary = survey.copy(previousHash = latestHash)
        val seal = preliminary.computeHash()
        val sealedSurvey = preliminary.copy(
            isLocked = true,
            sha256Hash = seal
        )
        return dao.insertSurvey(sealedSurvey)
    }

    suspend fun logMediaSession(session: ResearchMediaSessionEntity): Long {
        val seal = session.computeHash()
        val sealed = session.copy(isLocked = true, sha256Hash = seal)
        return dao.insertMediaSession(sealed)
    }

    suspend fun saveAudioLog(audioLog: ResearchAudioLogEntity): Long {
        val seal = audioLog.computeHash()
        val sealed = audioLog.copy(isLocked = true, sha256Hash = seal)
        return dao.insertAudioLog(sealed)
    }

    suspend fun clearAllData() {
        dao.clearSurveys()
        dao.clearMediaSessions()
        dao.clearAudioLogs()
    }

    suspend fun seedBenchmark156Sample() {
        val sections = listOf("ABM-1", "ABM-2", "ABM-3", "ABM-4")
        val sexes = listOf("Female", "Male", "Female", "Female", "Male") // ABM often slightly higher female ratio
        val allowances = listOf(
            "₱200–₱500", "₱200–₱500", "₱501–₱1,000", "₱501–₱1,000",
            "Under ₱200", "₱1,001–₱1,500", "Above ₱1,500"
        )
        val platforms = listOf(
            "TikTok, Instagram Reels",
            "TikTok, YouTube Shorts",
            "TikTok, Instagram Reels, YouTube Shorts",
            "TikTok",
            "Instagram Reels, YouTube Shorts"
        )

        val seededList = mutableListOf<ResearchSurveyEntity>()
        var prevHash = dao.getLatestSurveyHash() ?: "GENESIS_BLOCK"

        for (i in 1..156) {
            val code = "RESP-%03d".format(i)
            val age = if (Random.nextFloat() < 0.75f) 16 else 17
            val sex = sexes[Random.nextInt(sexes.size)]
            val sec = sections[Random.nextInt(sections.size)]
            val allow = allowances[Random.nextInt(allowances.size)]
            val plat = platforms[Random.nextInt(platforms.size)]
            val dailyHours = 1.5f + Random.nextFloat() * 4.0f

            // Correlated Likert scores (higher functionality associates with higher awareness)
            val baseFunc = 3 + (if (Random.nextFloat() < 0.65f) 1 else 0)
            val baseAware = (baseFunc - (if (Random.nextFloat() < 0.3f) 1 else 0)).coerceIn(1, 4)

            val s = ResearchSurveyEntity(
                id = 0,
                participantCode = code,
                timestamp = System.currentTimeMillis() - Random.nextLong(1000L * 60 * 60 * 24 * 7),
                age = age,
                sex = sex,
                section = sec,
                weeklyAllowance = allow,
                primaryPlatforms = plat,
                dailyHoursEstimate = ((dailyHours * 10).toInt()) / 10f,
                platformAwareness1 = (baseAware + Random.nextInt(-1, 2)).coerceIn(2, 4),
                platformAwareness2 = (baseAware + Random.nextInt(0, 2)).coerceIn(2, 4),
                frequencyAwareness1 = (baseAware + Random.nextInt(-1, 2)).coerceIn(2, 4),
                frequencyAwareness2 = (baseAware + Random.nextInt(0, 2)).coerceIn(2, 4),
                timeSpentAwareness1 = (baseAware + Random.nextInt(-1, 2)).coerceIn(2, 4),
                timeSpentAwareness2 = (baseAware + Random.nextInt(0, 2)).coerceIn(2, 4),
                accurateRecording1 = (baseFunc + Random.nextInt(-1, 2)).coerceIn(2, 4),
                accurateRecording2 = (baseFunc + Random.nextInt(0, 2)).coerceIn(2, 4),
                easyDataEntry1 = (baseFunc + Random.nextInt(0, 2)).coerceIn(2, 4),
                easyDataEntry2 = 4,
                usefulFeatures1 = (baseFunc + Random.nextInt(-1, 2)).coerceIn(2, 4),
                usefulFeatures2 = (baseFunc + Random.nextInt(0, 2)).coerceIn(2, 4),
                purchaseIntention1 = Random.nextInt(2, 5),
                purchaseIntention2 = Random.nextInt(2, 5),
                productPreference1 = Random.nextInt(2, 5),
                impulseBuying1 = Random.nextInt(2, 5),
                impulseBuying2 = Random.nextInt(1, 4),
                isLocked = true,
                previousHash = prevHash
            )
            val hash = s.computeHash()
            val sealed = s.copy(sha256Hash = hash)
            seededList.add(sealed)
            prevHash = hash
        }

        dao.insertSurveys(seededList)

        // Seed some representative media sessions and audio logs
        val categories = listOf("E-Commerce / Budol", "Influencer GRWM / Unboxing", "Entertainment / Comedy", "Educational / ABM", "Viral Trends")
        val sampleSessions = mutableListOf<ResearchMediaSessionEntity>()
        for (i in 1..25) {
            val cat = categories[Random.nextInt(categories.size)]
            val isImpulse = cat.contains("Budol") || cat.contains("Influencer")
            val sess = ResearchMediaSessionEntity(
                participantCode = "RESP-%03d".format(Random.nextInt(1, 30)),
                timestamp = System.currentTimeMillis() - Random.nextLong(1000L * 60 * 60 * 48),
                platform = listOf("TikTok", "Instagram Reels", "YouTube Shorts").random(),
                contentCategory = cat,
                durationSeconds = Random.nextLong(180, 2400),
                impulseBuyUrgeTriggered = isImpulse,
                productNameViewed = if (isImpulse) listOf("Aesthetic Tumbler", "Affordable Lip Oil", "Study Planner", "Shopee Budol Bag").random() else "None",
                estimatedPricePhp = if (isImpulse) listOf(149.0, 299.0, 450.0, 750.0).random() else 0.0,
                reflectionNotes = if (isImpulse) "Algorithm pushed product via GRWM video during break" else "Watched educational accounting tutorial",
                isLocked = true
            )
            val h = sess.computeHash()
            sampleSessions.add(sess.copy(sha256Hash = h))
        }
        dao.insertMediaSessions(sampleSessions)
    }

    val liveStatistics: Flow<ResearchStatistics> = combine(
        allSurveys,
        allMediaSessions,
        allAudioLogs
    ) { surveys, sessions, audioLogs ->
        computeStatistics(surveys, sessions, audioLogs)
    }

    private fun computeStatistics(
        surveys: List<ResearchSurveyEntity>,
        sessions: List<ResearchMediaSessionEntity>,
        audioLogs: List<ResearchAudioLogEntity>
    ): ResearchStatistics {
        val n = surveys.size
        val targetN = 156
        val popN = 260
        val progress = if (targetN > 0) ((n.toFloat() / targetN) * 100f).coerceAtMost(100f) else 0f

        // Tamper verification
        var tampered = 0
        var totalAudited = surveys.size + sessions.size + audioLogs.size
        for (s in surveys) {
            if (!s.isUntampered()) tampered++
        }
        for (m in sessions) {
            if (!m.isUntampered()) tampered++
        }
        for (a in audioLogs) {
            if (!a.isUntampered()) tampered++
        }

        // Demographics distribution
        val sexDist = if (n > 0) {
            surveys.groupBy { it.sex }.map { (sex, list) ->
                DemographicDistributionItem(sex, list.size, (list.size.toFloat() / n) * 100f)
            }.sortedByDescending { it.count }
        } else emptyList()

        val ageDist = if (n > 0) {
            surveys.groupBy { "${it.age} years old" }.map { (age, list) ->
                DemographicDistributionItem(age, list.size, (list.size.toFloat() / n) * 100f)
            }.sortedBy { it.category }
        } else emptyList()

        val secDist = if (n > 0) {
            surveys.groupBy { it.section }.map { (sec, list) ->
                DemographicDistributionItem(sec, list.size, (list.size.toFloat() / n) * 100f)
            }.sortedBy { it.category }
        } else emptyList()

        val allowDist = if (n > 0) {
            surveys.groupBy { it.weeklyAllowance }.map { (allow, list) ->
                DemographicDistributionItem(allow, list.size, (list.size.toFloat() / n) * 100f)
            }.sortedByDescending { it.count }
        } else emptyList()

        // Weighted Means
        val platAwareMean = if (n > 0) surveys.map { it.platformAwarenessMean }.average().toFloat() else 0.0f
        val freqAwareMean = if (n > 0) surveys.map { it.frequencyAwarenessMean }.average().toFloat() else 0.0f
        val timeAwareMean = if (n > 0) surveys.map { it.timeSpentAwarenessMean }.average().toFloat() else 0.0f
        val overallAware = (platAwareMean + freqAwareMean + timeAwareMean) / 3.0f

        val accRecMean = if (n > 0) surveys.map { it.accurateRecordingMean }.average().toFloat() else 0.0f
        val easyDataMean = if (n > 0) surveys.map { it.easyDataEntryMean }.average().toFloat() else 0.0f
        val useFeatMean = if (n > 0) surveys.map { it.usefulFeaturesMean }.average().toFloat() else 0.0f
        val overallFunc = (accRecMean + easyDataMean + useFeatMean) / 3.0f

        val overallImpulse = if (n > 0) surveys.map { it.overallImpulseScore }.average().toFloat() else 0.0f

        val funcIndicators = listOf(
            IndicatorMeanItem("1.1. Accurate Recording", accRecMean, StatisticsCalculator.interpretLikertMean(accRecMean)),
            IndicatorMeanItem("1.2. Easy Data Entry", easyDataMean, StatisticsCalculator.interpretLikertMean(easyDataMean)),
            IndicatorMeanItem("1.3. Useful Features", useFeatMean, StatisticsCalculator.interpretLikertMean(useFeatMean))
        )

        val awareIndicators = listOf(
            IndicatorMeanItem("2.1. Platform Awareness", platAwareMean, StatisticsCalculator.interpretLikertMean(platAwareMean)),
            IndicatorMeanItem("2.2. Frequency Awareness", freqAwareMean, StatisticsCalculator.interpretLikertMean(freqAwareMean)),
            IndicatorMeanItem("2.3. Time Spent Awareness", timeAwareMean, StatisticsCalculator.interpretLikertMean(timeAwareMean))
        )

        // Correlation: Functionality (IV) vs Digital Media Awareness (DV)
        val xFunc = surveys.map { it.overallFunctionalityMean }
        val yAware = surveys.map { it.overallAwarenessMean }
        val corrFuncVsAware = StatisticsCalculator.computeCorrelation(xFunc, yAware)

        // Correlation: Functionality vs Impulse Buying
        val yImpulse = surveys.map { it.overallImpulseScore }
        val corrFuncVsImpulse = StatisticsCalculator.computeCorrelation(xFunc, yImpulse)

        return ResearchStatistics(
            sampleSize = n,
            targetSampleSize = targetN,
            totalPopulation = popN,
            progressPercentage = progress,
            sexDistribution = sexDist,
            ageDistribution = ageDist,
            sectionDistribution = secDist,
            allowanceDistribution = allowDist,
            platformAwarenessMean = platAwareMean,
            frequencyAwarenessMean = freqAwareMean,
            timeSpentAwarenessMean = timeAwareMean,
            overallAwarenessMean = overallAware,
            accurateRecordingMean = accRecMean,
            easyDataEntryMean = easyDataMean,
            usefulFeaturesMean = useFeatMean,
            overallFunctionalityMean = overallFunc,
            overallImpulseMean = overallImpulse,
            functionalityIndicators = funcIndicators,
            awarenessIndicators = awareIndicators,
            functionalityVsAwarenessCorrelation = corrFuncVsAware,
            functionalityVsImpulseCorrelation = corrFuncVsImpulse,
            totalRecordsAudited = totalAudited,
            tamperedCount = tampered,
            isSystemIntegrityValid = tampered == 0 && totalAudited > 0
        )
    }
}
