package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ResearchSurveyEntity
import com.example.data.model.StatisticsCalculator
import com.example.util.CryptoUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SCROLL", appName)
    }

    @Test
    fun `verify sha256 tamper detection`() {
        val survey = ResearchSurveyEntity(
            id = 1,
            participantCode = "RESP-001",
            age = 16,
            sex = "Female",
            section = "ABM-1",
            weeklyAllowance = "₱200–₱500",
            primaryPlatforms = "TikTok",
            dailyHoursEstimate = 3.5f,
            isLocked = true
        )
        val validHash = survey.computeHash()
        val sealedSurvey = survey.copy(sha256Hash = validHash)

        assertTrue(sealedSurvey.isUntampered())

        // Simulating tampering attempt by modifying an answer
        val tamperedSurvey = sealedSurvey.copy(age = 17)
        assertFalse(tamperedSurvey.isUntampered())
    }

    @Test
    fun `verify pearson correlation calculation`() {
        val x = listOf(3.0f, 3.5f, 4.0f, 3.2f, 3.8f)
        val y = listOf(3.1f, 3.4f, 3.9f, 3.0f, 3.7f)
        val corr = StatisticsCalculator.computeCorrelation(x, y)

        assertTrue(corr.r > 0.8f)
        assertEquals(5, corr.sampleSize)
        assertTrue(corr.rSquared > 0.6f)
    }

    @Test
    fun `verify likert interpretation ranges`() {
        assertEquals("Strongly Agree / Very High", StatisticsCalculator.interpretLikertMean(3.5f))
        assertEquals("Agree / High", StatisticsCalculator.interpretLikertMean(3.0f))
        assertEquals("Disagree / Low", StatisticsCalculator.interpretLikertMean(2.2f))
        assertEquals("Strongly Disagree / Very Low", StatisticsCalculator.interpretLikertMean(1.5f))
    }
}
