package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DynamicForm
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ResearchAudioLogEntity
import com.example.data.model.ResearchStatistics
import com.example.data.model.ResearchSurveyEntity
import com.example.ui.components.TamperProofBadge
import com.example.ui.theme.ScrollAmber
import com.example.ui.theme.ScrollAmberContainer
import com.example.ui.theme.ScrollBackground
import com.example.ui.theme.ScrollBorder
import com.example.ui.theme.ScrollDeepBlue
import com.example.ui.theme.ScrollElectricBlue
import com.example.ui.theme.ScrollGreenContainer
import com.example.ui.theme.ScrollLightBlueContainer
import com.example.ui.theme.ScrollLockedRed
import com.example.ui.theme.ScrollOnBlueContainer
import com.example.ui.theme.ScrollRoyalBlue
import com.example.ui.theme.ScrollSurface
import com.example.ui.theme.ScrollSurfaceVariant
import com.example.ui.theme.ScrollTextMuted
import com.example.ui.theme.ScrollTextPrimary
import com.example.ui.theme.ScrollTextSecondary
import com.example.ui.theme.ScrollVerifiedGreen
import com.example.ui.theme.ScrollWhite
import com.example.ui.viewmodel.ResearchViewModel
import com.example.ui.viewmodel.ResearcherTab
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ResearcherVaultScreen(
    viewModel: ResearchViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.researcherTab.collectAsStateWithLifecycle()
    val statistics by viewModel.liveStatistics.collectAsStateWithLifecycle()
    val allSurveys by viewModel.allSurveys.collectAsStateWithLifecycle()
    val allSessions by viewModel.allMediaSessions.collectAsStateWithLifecycle()
    val allAudioLogs by viewModel.allAudioLogs.collectAsStateWithLifecycle()

    val tabs = listOf(
        ResearcherTab.LIVE_ANALYTICS to "Live Analytics & Stats",
        ResearcherTab.INCOMING_LOGS to "Incoming Logs (${allSurveys.size})",
        ResearcherTab.ORAL_AUDIO_VAULT to "Oral Audio Vault (${allAudioLogs.size})",
        ResearcherTab.TAMPER_PROOF_AUDIT to "Tamper Audit",
        ResearcherTab.EXPORT_DATASET to "Export & Download"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScrollBackground)
    ) {
        // Vault Tabs Bar
        ScrollableTabRow(
            selectedTabIndex = tabs.indexOfFirst { it.first == currentTab }.coerceAtLeast(0),
            containerColor = ScrollSurface,
            contentColor = ScrollRoyalBlue,
            edgePadding = 12.dp
        ) {
            tabs.forEach { (tab, title) ->
                val selected = currentTab == tab
                Tab(
                    selected = selected,
                    onClick = { viewModel.setResearcherTab(tab) },
                    text = {
                        Text(
                            text = title,
                            color = if (selected) ScrollRoyalBlue else ScrollTextSecondary,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier.testTag("researcher_tab_${tab.name.lowercase()}")
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (currentTab) {
                ResearcherTab.LIVE_ANALYTICS -> {
                    AnalyticsAndStatsView(
                        stats = statistics,
                        onSeedBenchmark = { viewModel.seedBenchmarkData() }
                    )
                }
                ResearcherTab.INCOMING_LOGS -> {
                    IncomingLogsView(surveys = allSurveys)
                }
                ResearcherTab.ORAL_AUDIO_VAULT -> {
                    OralAudioVaultView(
                        audioLogs = allAudioLogs,
                        viewModel = viewModel
                    )
                }
                ResearcherTab.TAMPER_PROOF_AUDIT -> {
                    TamperProofAuditView(
                        stats = statistics,
                        surveys = allSurveys
                    )
                }
                ResearcherTab.EXPORT_DATASET -> {
                    ExportDatasetView(
                        viewModel = viewModel,
                        stats = statistics,
                        surveyCount = allSurveys.size,
                        sessionCount = allSessions.size,
                        audioCount = allAudioLogs.size
                    )
                }
            }
        }
    }
}

@Composable
fun AnalyticsAndStatsView(
    stats: ResearchStatistics?,
    onSeedBenchmark: () -> Unit
) {
    if (stats == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Initializing statistical processing engine...", color = ScrollTextSecondary)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Cochran's Sample Size Progress Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = ScrollSurface),
                border = BorderStroke(1.dp, ScrollBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "COCHRAN'S SAMPLE TARGET (n = 156)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ScrollRoyalBlue,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Grade 11 ABM Population (N = 260) • 5% Margin of Error",
                                fontSize = 11.sp,
                                color = ScrollTextMuted
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (stats.sampleSize >= stats.targetSampleSize) ScrollGreenContainer else ScrollLightBlueContainer
                        ) {
                            Text(
                                text = "${stats.sampleSize} / ${stats.targetSampleSize}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (stats.sampleSize >= stats.targetSampleSize) ScrollVerifiedGreen else ScrollRoyalBlue,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { (stats.progressPercentage / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = ScrollRoyalBlue,
                        trackColor = ScrollLightBlueContainer
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (stats.sampleSize < stats.targetSampleSize) {
                        Button(
                            onClick = onSeedBenchmark,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("seed_benchmark_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ScrollDeepBlue),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Seed Benchmark Sample (N=156 Lawang Bato NHS)", fontSize = 12.sp)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = ScrollVerifiedGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cochran empirical sampling requirement satisfied.", fontSize = 12.sp, color = ScrollVerifiedGreen, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // ================= WEIGHTED MEAN TABLES (Chapter III) =================
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = ScrollSurface),
                border = BorderStroke(1.dp, ScrollBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Table 1: Weighted Means & Likert Interpretations",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ScrollTextPrimary
                    )
                    Text(
                        text = "Scale: 3.26-4.00 (Strongly Agree) • 2.51-3.25 (Agree) • 1.76-2.50 (Disagree) • 1.00-1.75 (Strongly Disagree)",
                        fontSize = 10.5.sp,
                        color = ScrollTextMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Functionality Sub-indicators (IV)
                    Text("INDEPENDENT VARIABLE: SCROLL FUNCTIONALITY", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = ScrollRoyalBlue)
                    Spacer(modifier = Modifier.height(6.dp))
                    stats.functionalityIndicators.forEach { item ->
                        MeanRow(title = item.indicatorName, mean = item.weightedMean, interp = item.interpretation)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = ScrollBorder)
                    MeanRow(
                        title = "Composite Functionality Mean",
                        mean = stats.overallFunctionalityMean,
                        interp = stats.functionalityIndicators.firstOrNull()?.interpretation ?: "",
                        isBold = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Awareness Sub-indicators (DV)
                    Text("DEPENDENT VARIABLE: DIGITAL MEDIA AWARENESS", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = ScrollRoyalBlue)
                    Spacer(modifier = Modifier.height(6.dp))
                    stats.awarenessIndicators.forEach { item ->
                        MeanRow(title = item.indicatorName, mean = item.weightedMean, interp = item.interpretation)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = ScrollBorder)
                    MeanRow(
                        title = "Composite Awareness Mean",
                        mean = stats.overallAwarenessMean,
                        interp = stats.awarenessIndicators.firstOrNull()?.interpretation ?: "",
                        isBold = true
                    )
                }
            }
        }

        // ================= PEARSON-R CORRELATION (Chapter III) =================
        item {
            val corr = stats.functionalityVsAwarenessCorrelation
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = ScrollDeepBlue),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PEARSON PRODUCT-MOMENT CORRELATION (r)",
                            color = ScrollLightBlueContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ScrollRoyalBlue
                        ) {
                            Text(
                                text = "N = ${corr.sampleSize}",
                                color = ScrollWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "r = ${String.format(Locale.US, "%.3f", corr.r)}",
                                color = ScrollWhite,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "r² (Coefficient of Determination) = ${String.format(Locale.US, "%.3f", corr.rSquared)}",
                                color = ScrollWhite.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "t = ${String.format(Locale.US, "%.2f", corr.tStatistic)}",
                                color = Color(0xFF6EE7B7),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "df = ${corr.degreesOfFreedom}",
                                color = ScrollWhite.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF161E48)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "VERBAL INTERPRETATION: ${corr.interpretation.uppercase()}",
                                color = Color(0xFF93C5FD),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = corr.hypothesisVerdict,
                                color = ScrollWhite,
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // ================= DEMOGRAPHIC FREQUENCY DISTRIBUTIONS =================
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = ScrollSurface),
                border = BorderStroke(1.dp, ScrollBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Demographic Frequency & Percentage Distribution",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ScrollTextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Allowance Distribution
                    Text("Weekly Allowance (PHP)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ScrollRoyalBlue)
                    Spacer(modifier = Modifier.height(6.dp))
                    stats.allowanceDistribution.forEach { item ->
                        FrequencyRow(label = item.category, count = item.count, pct = item.percentage)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Section Distribution
                    Text("Grade 11 ABM Section", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ScrollRoyalBlue)
                    Spacer(modifier = Modifier.height(6.dp))
                    stats.sectionDistribution.forEach { item ->
                        FrequencyRow(label = item.category, count = item.count, pct = item.percentage)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun MeanRow(
    title: String,
    mean: Float,
    interp: String,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isBold) ScrollRoyalBlue else ScrollTextPrimary,
            modifier = Modifier.weight(1f)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = String.format(Locale.US, "%.2f", mean),
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = ScrollTextPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = ScrollLightBlueContainer
            ) {
                Text(
                    text = interp,
                    fontSize = 9.5.sp,
                    color = ScrollRoyalBlue,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun FrequencyRow(label: String, count: Int, pct: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 11.5.sp, color = ScrollTextSecondary)
        Text(
            text = "$count (${String.format(Locale.US, "%.1f", pct)}%)",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = ScrollTextPrimary
        )
    }
}

@Composable
fun IncomingLogsView(surveys: List<ResearchSurveyEntity>) {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Central Receiving Log Ledger (${surveys.size} Entries Received)",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = ScrollTextPrimary
            )
        }

        if (surveys.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ScrollSurface),
                    border = BorderStroke(1.dp, ScrollBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.DynamicForm, contentDescription = null, tint = ScrollTextMuted, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No participant entries logged yet", color = ScrollTextPrimary, fontWeight = FontWeight.Bold)
                        Text("Participant submissions from the logger app will appear here in real time.", color = ScrollTextMuted, fontSize = 11.5.sp)
                    }
                }
            }
        }

        items(surveys) { s ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ScrollSurface),
                border = BorderStroke(1.dp, ScrollBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = s.participantCode,
                            fontWeight = FontWeight.Bold,
                            color = ScrollRoyalBlue,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(sdf.format(Date(s.timestamp)), fontSize = 10.sp, color = ScrollTextMuted)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "${s.strandAndGrade} • ${s.section} • Age ${s.age} (${s.sex})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ScrollTextPrimary
                    )

                    Text(
                        text = "Allowance: ${s.weeklyAllowance} • Daily: ${s.dailyHoursEstimate}h",
                        fontSize = 11.5.sp,
                        color = ScrollTextSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Awareness: ${String.format(Locale.US, "%.2f", s.overallAwarenessMean)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ScrollElectricBlue
                        )
                        Text(
                            text = "Functionality: ${String.format(Locale.US, "%.2f", s.overallFunctionalityMean)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ScrollRoyalBlue
                        )
                        Text(
                            text = "Impulse: ${String.format(Locale.US, "%.2f", s.overallImpulseScore)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ScrollLockedRed
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TamperProofBadge(isUntampered = s.isUntampered(), sha256Hash = s.sha256Hash)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun OralAudioVaultView(
    audioLogs: List<ResearchAudioLogEntity>,
    viewModel: ResearchViewModel
) {
    val isPlaying by viewModel.isAudioPlaying.collectAsStateWithLifecycle()
    val playingPath by viewModel.currentlyPlayingPath.collectAsStateWithLifecycle()
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Recorded Oral Research Debriefs & Voice Statements",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = ScrollTextPrimary
            )
            Text(
                text = "Verbal statements and audio interview records collected during the study.",
                fontSize = 11.sp,
                color = ScrollTextMuted
            )
        }

        if (audioLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ScrollSurface),
                    border = BorderStroke(1.dp, ScrollBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Audiotrack, contentDescription = null, tint = ScrollTextMuted, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No oral research recordings logged yet", color = ScrollTextPrimary, fontWeight = FontWeight.Bold)
                        Text("Participants or researchers can record verbal reflections in the logger app.", color = ScrollTextMuted, fontSize = 11.5.sp)
                    }
                }
            }
        }

        items(audioLogs) { a ->
            val isCurrentItemPlaying = isPlaying && playingPath == a.audioFilePath
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ScrollSurface),
                border = BorderStroke(1.dp, ScrollBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(a.title, fontWeight = FontWeight.Bold, color = ScrollRoyalBlue, fontSize = 13.5.sp)
                            Text("ID: ${a.participantCode} • ${sdf.format(Date(a.timestamp))}", fontSize = 10.5.sp, color = ScrollTextMuted)
                        }

                        if (a.audioFilePath.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    if (isCurrentItemPlaying) viewModel.stopAudioPlayback() else viewModel.playAudioFile(a.audioFilePath)
                                }
                            ) {
                                Icon(
                                    imageVector = if (isCurrentItemPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = if (isCurrentItemPlaying) "Stop" else "Play",
                                    tint = ScrollRoyalBlue
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "\"${a.transcriptNotes}\"",
                        fontSize = 12.sp,
                        color = ScrollTextPrimary,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TamperProofBadge(isUntampered = a.isUntampered(), sha256Hash = a.sha256Hash)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun TamperProofAuditView(
    stats: ResearchStatistics?,
    surveys: List<ResearchSurveyEntity>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = ScrollSurface),
                border = BorderStroke(1.dp, ScrollBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = if (stats?.isSystemIntegrityValid == true) ScrollVerifiedGreen else ScrollRoyalBlue,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Cryptographic Tamper-Proof Audit Vault",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = ScrollTextPrimary
                            )
                            Text(
                                text = "Irreversible SHA-256 Ledger Verification",
                                fontSize = 11.sp,
                                color = ScrollTextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (stats?.isSystemIntegrityValid == true) ScrollGreenContainer else ScrollLightBlueContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = if (stats?.isSystemIntegrityValid == true) ScrollVerifiedGreen else ScrollRoyalBlue
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (stats?.isSystemIntegrityValid == true) "100% UNTAMPERED & VERIFIED SECURE" else "AUDIT ACTIVE: Integrity Monitoring",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (stats?.isSystemIntegrityValid == true) Color(0xFF065F46) else ScrollRoyalBlue
                                )
                                Text(
                                    text = "${stats?.totalRecordsAudited ?: 0} records checked • ${stats?.tamperedCount ?: 0} tamper anomalies detected.",
                                    fontSize = 11.sp,
                                    color = ScrollTextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "How Immutability Works in SCROLL:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ScrollTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "1. When a respondent submits demographic or Likert data, a canonical string payload is constructed.\n" +
                                "2. An irreversible SHA-256 hash is computed and stored permanently.\n" +
                                "3. The audit engine dynamically recalculates the hash against stored attributes. If even a single byte or number is tampered with, the checksum mismatches and is flagged immediately.",
                        fontSize = 11.sp,
                        color = ScrollTextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun ExportDatasetView(
    viewModel: ResearchViewModel,
    stats: ResearchStatistics?,
    surveyCount: Int,
    sessionCount: Int,
    audioCount: Int
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Export & Download Research Data",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = ScrollTextPrimary
            )
            Text(
                text = "Download clean, SPSS and Excel compatible CSV datasets and complete Chapter III & IV research reports.",
                fontSize = 11.5.sp,
                color = ScrollTextMuted
            )
        }

        // Export Surveys CSV Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ScrollSurface),
                border = BorderStroke(1.dp, ScrollBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Survey & Demographic Dataset (.CSV)", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = ScrollRoyalBlue)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Includes respondent demographics, Likert scale responses for Parts II, III, IV, computed sub-variable means, and SHA-256 tamper seals ($surveyCount rows).",
                        fontSize = 11.5.sp,
                        color = ScrollTextSecondary,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.exportSurveysCsv() },
                        colors = ButtonDefaults.buttonColors(containerColor = ScrollRoyalBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("export_surveys_csv_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download Survey CSV", fontSize = 12.sp)
                    }
                }
            }
        }

        // Export Sessions CSV Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ScrollSurface),
                border = BorderStroke(1.dp, ScrollBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Media Consumption Tracker Sessions (.CSV)", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = ScrollRoyalBlue)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Includes timestamped scrolling durations, platform breakdown, content recognition categories, and impulse buying triggers ($sessionCount rows).",
                        fontSize = 11.5.sp,
                        color = ScrollTextSecondary,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.exportSessionsCsv() },
                        colors = ButtonDefaults.buttonColors(containerColor = ScrollRoyalBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("export_sessions_csv_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download Sessions CSV", fontSize = 12.sp)
                    }
                }
            }
        }

        // Full Executive Report Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ScrollSurface),
                border = BorderStroke(1.dp, ScrollBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Comprehensive Research Summary Report (.TXT)", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = ScrollRoyalBlue)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Generates complete statistical summary with Cochran formula analysis, Weighted Mean tables, and Pearson r hypothesis conclusions for Chapter IV of the research.",
                        fontSize = 11.5.sp,
                        color = ScrollTextSecondary,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.exportSummaryReport() },
                        colors = ButtonDefaults.buttonColors(containerColor = ScrollDeepBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("export_summary_report_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Generate & Share Research Report", fontSize = 12.sp)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}
