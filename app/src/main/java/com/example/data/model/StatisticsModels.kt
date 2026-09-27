package com.example.data.model

import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class DemographicDistributionItem(
    val category: String,
    val count: Int,
    val percentage: Float
)

data class IndicatorMeanItem(
    val indicatorName: String,
    val weightedMean: Float,
    val interpretation: String
)

data class CorrelationResult(
    val r: Float,
    val rSquared: Float,
    val tStatistic: Float,
    val degreesOfFreedom: Int,
    val sampleSize: Int,
    val interpretation: String,
    val hypothesisVerdict: String
)

data class ResearchStatistics(
    val sampleSize: Int,
    val targetSampleSize: Int = 156, // Cochran's formula target from N=260
    val totalPopulation: Int = 260,
    val progressPercentage: Float,

    // Demographics distributions
    val sexDistribution: List<DemographicDistributionItem>,
    val ageDistribution: List<DemographicDistributionItem>,
    val sectionDistribution: List<DemographicDistributionItem>,
    val allowanceDistribution: List<DemographicDistributionItem>,

    // Weighted Means for Part II (Awareness)
    val platformAwarenessMean: Float,
    val frequencyAwarenessMean: Float,
    val timeSpentAwarenessMean: Float,
    val overallAwarenessMean: Float,

    // Weighted Means for Part III (Functionality)
    val accurateRecordingMean: Float,
    val easyDataEntryMean: Float,
    val usefulFeaturesMean: Float,
    val overallFunctionalityMean: Float,

    // Consumer Buying
    val overallImpulseMean: Float,

    // Indicator lists
    val functionalityIndicators: List<IndicatorMeanItem>,
    val awarenessIndicators: List<IndicatorMeanItem>,

    // Correlation
    val functionalityVsAwarenessCorrelation: CorrelationResult,
    val functionalityVsImpulseCorrelation: CorrelationResult,

    // Tamper audit
    val totalRecordsAudited: Int,
    val tamperedCount: Int,
    val isSystemIntegrityValid: Boolean
)

object StatisticsCalculator {

    fun interpretLikertMean(mean: Float): String {
        return when {
            mean >= 3.26f -> "Strongly Agree / Very High"
            mean >= 2.51f -> "Agree / High"
            mean >= 1.76f -> "Disagree / Low"
            else -> "Strongly Disagree / Very Low"
        }
    }

    fun computeCorrelation(xValues: List<Float>, yValues: List<Float>): CorrelationResult {
        val n = xValues.size
        if (n < 3) {
            return CorrelationResult(
                r = 0.0f,
                rSquared = 0.0f,
                tStatistic = 0.0f,
                degreesOfFreedom = 0,
                sampleSize = n,
                interpretation = "Insufficient data points for Pearson r (minimum 3 required)",
                hypothesisVerdict = "Pending adequate sample data"
            )
        }

        var sumX = 0.0
        var sumY = 0.0
        var sumXY = 0.0
        var sumX2 = 0.0
        var sumY2 = 0.0

        for (i in 0 until n) {
            val x = xValues[i].toDouble()
            val y = yValues[i].toDouble()
            sumX += x
            sumY += y
            sumXY += x * y
            sumX2 += x * x
            sumY2 += y * y
        }

        val numerator = (n * sumXY) - (sumX * sumY)
        val denominator = sqrt(((n * sumX2) - sumX.pow(2.0)) * ((n * sumY2) - sumY.pow(2.0)))

        val r = if (denominator != 0.0) (numerator / denominator).toFloat() else 0.0f
        val rClamped = r.coerceIn(-1.0f, 1.0f)
        val rSq = (rClamped * rClamped)
        val df = n - 2
        val tStat = if (rSq < 0.999f) {
            (rClamped * sqrt(df.toDouble() / (1.0 - rSq))).toFloat()
        } else {
            99.9f
        }

        val interp = when {
            rClamped >= 0.80f -> "Very Strong Positive Correlation"
            rClamped >= 0.60f -> "Strong Positive Correlation"
            rClamped >= 0.40f -> "Moderate Positive Correlation"
            rClamped >= 0.20f -> "Weak Positive Correlation"
            rClamped > -0.20f -> "Negligible / No Linear Correlation"
            else -> "Negative Correlation"
        }

        val verdict = if (rClamped >= 0.25f && tStat > 1.96f) {
            "Statistically Significant (p < 0.05). Reject H₀ in favor of Hₐ: A significant relationship exists between tracker functionality and digital media awareness."
        } else {
            "Accept H₀: No statistically significant relationship established at current sample threshold."
        }

        return CorrelationResult(
            r = ((rClamped * 1000).roundToInt()) / 1000f,
            rSquared = ((rSq * 1000).roundToInt()) / 1000f,
            tStatistic = ((tStat * 100).roundToInt()) / 100f,
            degreesOfFreedom = df,
            sampleSize = n,
            interpretation = interp,
            hypothesisVerdict = verdict
        )
    }
}
