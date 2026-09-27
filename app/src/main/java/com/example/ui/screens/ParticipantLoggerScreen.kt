package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.LikertScaleGroup
import com.example.ui.components.TamperProofBadge
import com.example.ui.theme.ScrollAmber
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
import com.example.ui.viewmodel.ParticipantTab
import com.example.ui.viewmodel.ResearchViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ParticipantLoggerScreen(
    viewModel: ResearchViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.participantTab.collectAsStateWithLifecycle()
    val isSurveySubmitted by viewModel.isSurveySubmitted.collectAsStateWithLifecycle()
    val lastSubmittedSeal by viewModel.lastSubmittedSeal.collectAsStateWithLifecycle()

    val tabs = listOf(
        ParticipantTab.DEMOGRAPHIC_QUESTIONNAIRE to "Questionnaire",
        ParticipantTab.MEDIA_SESSION_TRACKER to "Live Media Tracker",
        ParticipantTab.ORAL_INTERVIEW_RECORDER to "Audio Debrief Log",
        ParticipantTab.MY_SEALED_LOGS to "My Sealed Vault"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScrollBackground)
    ) {
        // Sub-tabs bar
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
                    onClick = { viewModel.setParticipantTab(tab) },
                    text = {
                        Text(
                            text = title,
                            color = if (selected) ScrollRoyalBlue else ScrollTextSecondary,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.5.sp
                        )
                    },
                    modifier = Modifier.testTag("participant_tab_${tab.name.lowercase()}")
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (currentTab) {
                ParticipantTab.DEMOGRAPHIC_QUESTIONNAIRE -> {
                    DemographicAndQuestionnaireView(
                        viewModel = viewModel,
                        isSubmitted = isSurveySubmitted,
                        seal = lastSubmittedSeal
                    )
                }
                ParticipantTab.MEDIA_SESSION_TRACKER -> {
                    MediaSessionTrackerView(viewModel = viewModel)
                }
                ParticipantTab.ORAL_INTERVIEW_RECORDER -> {
                    OralInterviewRecorderView(viewModel = viewModel)
                }
                ParticipantTab.MY_SEALED_LOGS -> {
                    MySealedLogsView(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DemographicAndQuestionnaireView(
    viewModel: ResearchViewModel,
    isSubmitted: Boolean,
    seal: String?
) {
    val participantCode by viewModel.participantCode.collectAsStateWithLifecycle()
    val age by viewModel.age.collectAsStateWithLifecycle()
    val sex by viewModel.sex.collectAsStateWithLifecycle()
    val section by viewModel.section.collectAsStateWithLifecycle()
    val weeklyAllowance by viewModel.weeklyAllowance.collectAsStateWithLifecycle()
    val selectedPlatforms by viewModel.selectedPlatforms.collectAsStateWithLifecycle()
    val dailyHoursEstimate by viewModel.dailyHoursEstimate.collectAsStateWithLifecycle()

    val plat1 by viewModel.platAwareness1.collectAsStateWithLifecycle()
    val plat2 by viewModel.platAwareness2.collectAsStateWithLifecycle()
    val freq1 by viewModel.freqAwareness1.collectAsStateWithLifecycle()
    val freq2 by viewModel.freqAwareness2.collectAsStateWithLifecycle()
    val time1 by viewModel.timeAwareness1.collectAsStateWithLifecycle()
    val time2 by viewModel.timeAwareness2.collectAsStateWithLifecycle()

    val acc1 by viewModel.accRec1.collectAsStateWithLifecycle()
    val acc2 by viewModel.accRec2.collectAsStateWithLifecycle()
    val easy1 by viewModel.easyData1.collectAsStateWithLifecycle()
    val easy2 by viewModel.easyData2.collectAsStateWithLifecycle()
    val useful1 by viewModel.usefulFeat1.collectAsStateWithLifecycle()
    val useful2 by viewModel.usefulFeat2.collectAsStateWithLifecycle()

    val purch1 by viewModel.purchaseIntent1.collectAsStateWithLifecycle()
    val purch2 by viewModel.purchaseIntent2.collectAsStateWithLifecycle()
    val prod1 by viewModel.prodPref1.collectAsStateWithLifecycle()
    val imp1 by viewModel.impulseBuy1.collectAsStateWithLifecycle()
    val imp2 by viewModel.impulseBuy2.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(6.dp)) }

        // Security Notice Banner
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ScrollLightBlueContainer),
                border = BorderStroke(1.dp, ScrollRoyalBlue.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = ScrollRoyalBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Research Instrument • Chapter III Methodology",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ScrollRoyalBlue
                        )
                        Text(
                            text = "Responses are sealed with an irreversible SHA-256 hash upon completion to guarantee strict data integrity.",
                            fontSize = 11.sp,
                            color = ScrollOnBlueContainer,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        if (isSubmitted && seal != null) {
            item {
                TamperProofBadge(
                    isUntampered = true,
                    sha256Hash = seal,
                    participantCode = participantCode
                )
            }
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ScrollWhite),
                    border = BorderStroke(1.dp, ScrollBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Entry Finalized & Sealed",
                            fontWeight = FontWeight.Bold,
                            color = ScrollRoyalBlue,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Your survey responses have been locked into the research database. To uphold empirical validity, locked entries cannot be edited further.",
                            fontSize = 12.sp,
                            color = ScrollTextSecondary,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { viewModel.resetParticipantFormForNewStudent() },
                            colors = ButtonDefaults.buttonColors(containerColor = ScrollRoyalBlue),
                            modifier = Modifier.testTag("register_another_student_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Register Next Student Respondent", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // ================= PART I: DEMOGRAPHIC PROFILE =================
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ScrollSurface),
                border = BorderStroke(1.dp, ScrollBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = ScrollRoyalBlue,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("I", color = ScrollWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Part I: Demographic Profile",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = ScrollTextPrimary
                            )
                            Text(
                                text = "Target: Grade 11 ABM Students, Lawang Bato National High School",
                                fontSize = 11.sp,
                                color = ScrollTextMuted
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = ScrollBorder)

                    // Participant Code
                    Text("Respondent Code", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ScrollTextSecondary)
                    Text(participantCode, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ScrollRoyalBlue, fontFamily = FontFamily.Monospace)

                    Spacer(modifier = Modifier.height(12.dp))

                    // Age
                    Text("Age", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ScrollTextSecondary)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        listOf(15, 16, 17, 18).forEach { a ->
                            FilterChip(
                                selected = age == a,
                                onClick = { if (!isSubmitted) viewModel.age.value = a },
                                label = { Text("$a yrs") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ScrollRoyalBlue,
                                    selectedLabelColor = ScrollWhite
                                ),
                                modifier = Modifier.testTag("demographic_age_$a")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sex
                    Text("Sex", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ScrollTextSecondary)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        listOf("Female", "Male", "Prefer not to say").forEach { s ->
                            FilterChip(
                                selected = sex == s,
                                onClick = { if (!isSubmitted) viewModel.sex.value = s },
                                label = { Text(s) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ScrollRoyalBlue,
                                    selectedLabelColor = ScrollWhite
                                ),
                                modifier = Modifier.testTag("demographic_sex_$s")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Grade 11 ABM Section
                    Text("Grade 11 ABM Section (Lawang Bato NHS)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ScrollTextSecondary)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        listOf("ABM-1", "ABM-2", "ABM-3", "ABM-4").forEach { sec ->
                            FilterChip(
                                selected = section == sec,
                                onClick = { if (!isSubmitted) viewModel.section.value = sec },
                                label = { Text(sec) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ScrollRoyalBlue,
                                    selectedLabelColor = ScrollWhite
                                ),
                                modifier = Modifier.testTag("demographic_sec_$sec")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Average Weekly Allowance
                    Text("Average Weekly Allowance (PHP)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ScrollTextSecondary)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        listOf("Under ₱200", "₱200–₱500", "₱501–₱1,000", "₱1,001–₱1,500", "Above ₱1,500").forEach { allow ->
                            FilterChip(
                                selected = weeklyAllowance == allow,
                                onClick = { if (!isSubmitted) viewModel.weeklyAllowance.value = allow },
                                label = { Text(allow) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ScrollRoyalBlue,
                                    selectedLabelColor = ScrollWhite
                                ),
                                modifier = Modifier.testTag("demographic_allowance_$allow")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Primary Short-Form Platforms
                    Text("Primary Short-Form Platforms Used", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ScrollTextSecondary)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        listOf("TikTok", "Instagram Reels", "YouTube Shorts").forEach { plat ->
                            val isSel = selectedPlatforms.contains(plat)
                            FilterChip(
                                selected = isSel,
                                onClick = { if (!isSubmitted) viewModel.togglePlatform(plat) },
                                label = { Text(plat) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ScrollRoyalBlue,
                                    selectedLabelColor = ScrollWhite
                                ),
                                modifier = Modifier.testTag("platform_chip_$plat")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Daily Estimated Hours
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Estimated Daily Scrolling Time", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ScrollTextSecondary)
                        Text("${String.format(Locale.US, "%.1f", dailyHoursEstimate)} hrs/day", fontWeight = FontWeight.Bold, color = ScrollRoyalBlue, fontSize = 12.sp)
                    }
                    Slider(
                        value = dailyHoursEstimate,
                        onValueChange = { if (!isSubmitted) viewModel.dailyHoursEstimate.value = it },
                        valueRange = 0.5f..8.0f,
                        steps = 15,
                        colors = SliderDefaults.colors(
                            thumbColor = ScrollRoyalBlue,
                            activeTrackColor = ScrollRoyalBlue
                        ),
                        modifier = Modifier.testTag("slider_daily_hours")
                    )
                }
            }
        }

        // ================= PART II: DIGITAL MEDIA USE AWARENESS =================
        item {
            Column {
                Text(
                    text = "Part II: Digital Media Use Awareness (DV)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = ScrollRoyalBlue
                )
                Text(
                    text = "4-Point Scale: 4 = Strongly Agree / Very High • 1 = Strongly Disagree / Very Low",
                    fontSize = 11.sp,
                    color = ScrollTextSecondary
                )
            }
        }

        item {
            LikertScaleGroup(
                itemNumber = "2.1A",
                subIndicatorLabel = "Platform Awareness",
                questionText = "I consciously recognize the algorithmic feed differences when switching between TikTok, Instagram Reels, and YouTube Shorts.",
                selectedScore = plat1,
                isReadOnly = isSubmitted,
                onScoreSelected = { viewModel.platAwareness1.value = it }
            )
        }

        item {
            LikertScaleGroup(
                itemNumber = "2.1B",
                subIndicatorLabel = "Platform Awareness",
                questionText = "I notice how short-form platforms adjust recommended videos based on my previous viewing speed and watch duration.",
                selectedScore = plat2,
                isReadOnly = isSubmitted,
                onScoreSelected = { viewModel.platAwareness2.value = it }
            )
        }

        item {
            LikertScaleGroup(
                itemNumber = "2.2A",
                subIndicatorLabel = "Frequency Awareness",
                questionText = "I am mindful of how frequently I open short-form video apps during school breaks, study hours, or right before sleeping.",
                selectedScore = freq1,
                isReadOnly = isSubmitted,
                onScoreSelected = { viewModel.freqAwareness1.value = it }
            )
        }

        item {
            LikertScaleGroup(
                itemNumber = "2.2B",
                subIndicatorLabel = "Frequency Awareness",
                questionText = "I catch myself habitually checking short-form feeds without having a specific video or topic in mind.",
                selectedScore = freq2,
                isReadOnly = isSubmitted,
                onScoreSelected = { viewModel.freqAwareness2.value = it }
            )
        }

        item {
            LikertScaleGroup(
                itemNumber = "2.3A",
                subIndicatorLabel = "Time Spent Awareness",
                questionText = "I accurately estimate the cumulative hours and minutes I spend actively scrolling through short videos each day.",
                selectedScore = time1,
                isReadOnly = isSubmitted,
                onScoreSelected = { viewModel.timeAwareness1.value = it }
            )
        }

        item {
            LikertScaleGroup(
                itemNumber = "2.3B",
                subIndicatorLabel = "Time Spent Awareness",
                questionText = "I recognize when autoplay and endless scroll mechanisms cause me to lose track of time while consuming micro-content.",
                selectedScore = time2,
                isReadOnly = isSubmitted,
                onScoreSelected = { viewModel.timeAwareness2.value = it }
            )
        }

        // ================= PART III: SCROLL TRACKER FUNCTIONALITY =================
        item {
            Column {
                Text(
                    text = "Part III: SCROLL Tracker Functionality (IV)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = ScrollRoyalBlue
                )
                Text(
                    text = "Evaluation of SCROLL's smart logging, recording accuracy, and usability features.",
                    fontSize = 11.sp,
                    color = ScrollTextSecondary
                )
            }
        }

        item {
            LikertScaleGroup(
                itemNumber = "1.1A",
                subIndicatorLabel = "Accurate Recording",
                questionText = "The SCROLL prototype precisely captures session durations and accurately categorizes my short-form media engagement.",
                selectedScore = acc1,
                isReadOnly = isSubmitted,
                onScoreSelected = { viewModel.accRec1.value = it }
            )
        }

        item {
            LikertScaleGroup(
                itemNumber = "1.1B",
                subIndicatorLabel = "Accurate Recording",
                questionText = "The tracker effectively differentiates between commercial/sponsored videos ('Budol' content) and educational or entertainment content.",
                selectedScore = acc2,
                isReadOnly = isSubmitted,
                onScoreSelected = { viewModel.accRec2.value = it }
            )
        }

        item {
            LikertScaleGroup(
                itemNumber = "1.2A",
                subIndicatorLabel = "Easy Data Entry",
                questionText = "Logging my short-form consumption sessions and reflections in SCROLL requires minimal effort and time.",
                selectedScore = easy1,
                isReadOnly = isSubmitted,
                onScoreSelected = { viewModel.easyData1.value = it }
            )
        }

        item {
            LikertScaleGroup(
                itemNumber = "1.2B",
                subIndicatorLabel = "Easy Data Entry",
                questionText = "The user interface is intuitive, easy to navigate, and eliminates cognitive friction during daily data entry.",
                selectedScore = easy2,
                isReadOnly = isSubmitted,
                onScoreSelected = { viewModel.easyData2.value = it }
            )
        }

        item {
            LikertScaleGroup(
                itemNumber = "1.3A",
                subIndicatorLabel = "Useful Features",
                questionText = "The visual statistics, duration alerts, and category breakdowns significantly heighten my personal consumption awareness.",
                selectedScore = useful1,
                isReadOnly = isSubmitted,
                onScoreSelected = { viewModel.usefulFeat1.value = it }
            )
        }

        item {
            LikertScaleGroup(
                itemNumber = "1.3B",
                subIndicatorLabel = "Useful Features",
                questionText = "The embedded reflection prompts help me analyze how algorithmic content affects my daily spending urges.",
                selectedScore = useful2,
                isReadOnly = isSubmitted,
                onScoreSelected = { viewModel.usefulFeat2.value = it }
            )
        }

        // ================= PART IV: CONSUMER BUYING & IMPULSE BEHAVIOR =================
        item {
            Column {
                Text(
                    text = "Part IV: Consumer Buying Techniques & Impulse Behavior",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = ScrollRoyalBlue
                )
                Text(
                    text = "Examining the relationship with student allowance spending and TikTok Shop / livestream exposure.",
                    fontSize = 11.sp,
                    color = ScrollTextSecondary
                )
            }
        }

        item {
            LikertScaleGroup(
                itemNumber = "3.1A",
                subIndicatorLabel = "Purchase Intention",
                questionText = "Exposure to viral products, in-video checkout buttons, and TikTok Shop livestreams creates an immediate intention to purchase.",
                selectedScore = purch1,
                isReadOnly = isSubmitted,
                onScoreSelected = { viewModel.purchaseIntent1.value = it }
            )
        }

        item {
            LikertScaleGroup(
                itemNumber = "3.1B",
                subIndicatorLabel = "Purchase Intention",
                questionText = "Micro-influencer 'GRWM' (Get Ready With Me) and unboxing videos increase my trust and desire to buy featured items.",
                selectedScore = purch2,
                isReadOnly = isSubmitted,
                onScoreSelected = { viewModel.purchaseIntent2.value = it }
            )
        }

        item {
            LikertScaleGroup(
                itemNumber = "3.2A",
                subIndicatorLabel = "Product Preference",
                questionText = "I prefer buying products that are trending on short-form platforms over items advertised through traditional television or billboards.",
                selectedScore = prod1,
                isReadOnly = isSubmitted,
                onScoreSelected = { viewModel.prodPref1.value = it }
            )
        }

        item {
            LikertScaleGroup(
                itemNumber = "3.3A",
                subIndicatorLabel = "Impulse Buying Behavior",
                questionText = "I have spent portions of my school allowance on unplanned purchases directly driven by persuasive short-form video algorithms.",
                selectedScore = imp1,
                isReadOnly = isSubmitted,
                onScoreSelected = { viewModel.impulseBuy1.value = it }
            )
        }

        item {
            LikertScaleGroup(
                itemNumber = "3.3B",
                subIndicatorLabel = "Budgeting & Verification",
                questionText = "Before checking out an item seen on a video, I systematically cross-check prices, reviews, and my weekly allowance constraints.",
                selectedScore = imp2,
                isReadOnly = isSubmitted,
                onScoreSelected = { viewModel.impulseBuy2.value = it }
            )
        }

        // Finalize & Cryptographically Seal Button
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.submitAndSealSurvey() },
                enabled = !isSubmitted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_and_seal_survey_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ScrollRoyalBlue,
                    disabledContainerColor = ScrollBorder
                )
            ) {
                Icon(
                    imageVector = if (isSubmitted) Icons.Default.Lock else Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSubmitted) "LOCKED & CRYPTOGRAPHICALLY SEALED" else "FINALIZE & LOCK RESEARCH ENTRY (SHA-256)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun MediaSessionTrackerView(viewModel: ResearchViewModel) {
    val participantCode by viewModel.participantCode.collectAsStateWithLifecycle()
    val sessionPlatform by viewModel.sessionPlatform.collectAsStateWithLifecycle()
    val sessionCategory by viewModel.sessionCategory.collectAsStateWithLifecycle()
    val isTimerActive by viewModel.isTimerActive.collectAsStateWithLifecycle()
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsStateWithLifecycle()
    val impulseUrgeTriggered by viewModel.impulseUrgeTriggered.collectAsStateWithLifecycle()
    val productViewed by viewModel.sessionProductViewed.collectAsStateWithLifecycle()
    val priceEstimate by viewModel.sessionPriceEstimate.collectAsStateWithLifecycle()
    val reflectionNotes by viewModel.sessionReflection.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Live Timer Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ScrollRoyalBlue),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ACTIVE SCROLLING SESSION TRACKER",
                        color = ScrollWhite.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val minutes = elapsedSeconds / 60
                    val seconds = elapsedSeconds % 60
                    val timeFormatted = String.format(Locale.US, "%02d:%02d", minutes, seconds)

                    Text(
                        text = timeFormatted,
                        color = ScrollWhite,
                        fontSize = 46.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (!isTimerActive) {
                            Button(
                                onClick = { viewModel.startSessionTimer() },
                                colors = ButtonDefaults.buttonColors(containerColor = ScrollWhite),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("start_session_timer_button")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = ScrollRoyalBlue)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Start Scrolling Tracker", color = ScrollRoyalBlue, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = { viewModel.pauseSessionTimer() },
                                colors = ButtonDefaults.buttonColors(containerColor = ScrollAmber),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("pause_session_timer_button")
                            ) {
                                Icon(Icons.Default.Pause, contentDescription = null, tint = ScrollWhite)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pause Timer", color = ScrollWhite, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Platform & Smart Content Recognition Classifier
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ScrollSurface),
                border = BorderStroke(1.dp, ScrollBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Select Platform",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ScrollTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("TikTok", "Instagram Reels", "YouTube Shorts").forEach { plat ->
                            FilterChip(
                                selected = sessionPlatform == plat,
                                onClick = { viewModel.sessionPlatform.value = plat },
                                label = { Text(plat) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ScrollRoyalBlue,
                                    selectedLabelColor = ScrollWhite
                                ),
                                modifier = Modifier.testTag("session_platform_$plat")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "2. Smart Content Recognition Category",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ScrollTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val categories = listOf(
                        "E-Commerce / Budol",
                        "Influencer GRWM / Unboxing",
                        "Entertainment / Comedy",
                        "Educational / ABM",
                        "Lifestyle / Viral"
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        categories.forEach { cat ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (sessionCategory == cat) ScrollLightBlueContainer else ScrollSurfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, if (sessionCategory == cat) ScrollRoyalBlue else Color.Transparent),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("content_cat_$cat"),
                                onClick = { viewModel.sessionCategory.value = cat }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (sessionCategory == cat) Icons.Default.CheckCircle else Icons.Default.Check,
                                        contentDescription = null,
                                        tint = if (sessionCategory == cat) ScrollRoyalBlue else ScrollTextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = cat,
                                        fontSize = 13.sp,
                                        fontWeight = if (sessionCategory == cat) FontWeight.Bold else FontWeight.Normal,
                                        color = if (sessionCategory == cat) ScrollRoyalBlue else ScrollTextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Impulse Buying Urge Trigger Details
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ScrollSurface),
                border = BorderStroke(1.dp, ScrollBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Did this session trigger an impulse buying urge?",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = ScrollTextPrimary
                            )
                            Text(
                                text = "e.g. Added item to cart, clicked yellow basket, or checked price",
                                fontSize = 11.sp,
                                color = ScrollTextMuted
                            )
                        }
                        Switch(
                            checked = impulseUrgeTriggered,
                            onCheckedChange = { viewModel.impulseUrgeTriggered.value = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ScrollWhite,
                                checkedTrackColor = ScrollRoyalBlue
                            ),
                            modifier = Modifier.testTag("switch_impulse_urge")
                        )
                    }

                    if (impulseUrgeTriggered) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = productViewed,
                            onValueChange = { viewModel.sessionProductViewed.value = it },
                            label = { Text("Product Featured / Viewed") },
                            placeholder = { Text("e.g. Trendy Oversized Hoodie, Lip Tint, Tumblr") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_product_viewed"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ScrollRoyalBlue,
                                focusedLabelColor = ScrollRoyalBlue
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = priceEstimate,
                            onValueChange = { viewModel.sessionPriceEstimate.value = it },
                            label = { Text("Estimated Price (₱ PHP)") },
                            placeholder = { Text("e.g. 299") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_price_estimate"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ScrollRoyalBlue,
                                focusedLabelColor = ScrollRoyalBlue
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = reflectionNotes,
                        onValueChange = { viewModel.sessionReflection.value = it },
                        label = { Text("Reflection: Did you search for this or did the algorithm push it?") },
                        placeholder = { Text("Describe how the video appeared in your feed...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_reflection_notes"),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ScrollRoyalBlue,
                            focusedLabelColor = ScrollRoyalBlue
                        )
                    )
                }
            }
        }

        // Lock & Seal Session Button
        item {
            Button(
                onClick = { viewModel.submitAndSealSession() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_session_log_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ScrollRoyalBlue)
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Lock & Cryptographically Seal Media Session", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun OralInterviewRecorderView(viewModel: ResearchViewModel) {
    val context = LocalContext.current
    val isRecording by viewModel.isRecordingAudio.collectAsStateWithLifecycle()
    val recordingSeconds by viewModel.recordingDurationSeconds.collectAsStateWithLifecycle()
    val topic by viewModel.audioInterviewTopic.collectAsStateWithLifecycle()
    val notes by viewModel.audioTranscriptNotes.collectAsStateWithLifecycle()
    val lastSavedAudioPath by viewModel.lastSavedAudioPath.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isAudioPlaying.collectAsStateWithLifecycle()

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) {
            viewModel.startAudioRecording()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = ScrollSurface),
                border = BorderStroke(1.dp, ScrollBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Research Oral Interview & Reflection Recorder",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = ScrollRoyalBlue
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Record verbal statements and oral feedback made during the study observation period and prototype tests.",
                        fontSize = 11.5.sp,
                        color = ScrollTextSecondary,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Pulse circle / Record Button
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(if (isRecording) ScrollLockedRed else ScrollLightBlueContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = {
                                if (isRecording) {
                                    viewModel.stopAudioRecording()
                                } else {
                                    if (hasPermission) {
                                        viewModel.startAudioRecording()
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(70.dp)
                                .testTag("record_audio_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                                tint = if (isRecording) ScrollWhite else ScrollRoyalBlue,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isRecording) "RECORDING: ${recordingSeconds}s" else if (lastSavedAudioPath != null) "Audio Captured (${recordingSeconds}s)" else "Tap to Record What is Said",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isRecording) ScrollLockedRed else ScrollRoyalBlue
                    )

                    if (lastSavedAudioPath != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    if (isPlaying) viewModel.stopAudioPlayback() else viewModel.playAudioFile(lastSavedAudioPath!!)
                                },
                                modifier = Modifier.testTag("preview_audio_button")
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = ScrollRoyalBlue
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isPlaying) "Stop" else "Listen to Audio", color = ScrollRoyalBlue)
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ScrollSurface),
                border = BorderStroke(1.dp, ScrollBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Oral Log Details & Transcripts",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = ScrollTextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = topic,
                        onValueChange = { viewModel.audioInterviewTopic.value = it },
                        label = { Text("Interview Topic / Research Question") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_audio_topic"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ScrollRoyalBlue,
                            focusedLabelColor = ScrollRoyalBlue
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { viewModel.audioTranscriptNotes.value = it },
                        label = { Text("What was said in the research? (Key Quotes / Notes)") },
                        placeholder = { Text("e.g. Respondent explained: 'I scrolled for 45 minutes without noticing until the tracker signaled me...'") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_audio_transcript"),
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ScrollRoyalBlue,
                            focusedLabelColor = ScrollRoyalBlue
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.saveAndSealAudioLog() },
                        enabled = lastSavedAudioPath != null || notes.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_and_seal_audio_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ScrollRoyalBlue)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cryptographically Seal Oral Log (SHA-256)", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun MySealedLogsView(viewModel: ResearchViewModel) {
    val participantCode by viewModel.participantCode.collectAsStateWithLifecycle()
    val allSurveys by viewModel.allSurveys.collectAsStateWithLifecycle()
    val allSessions by viewModel.allMediaSessions.collectAsStateWithLifecycle()
    val allAudioLogs by viewModel.allAudioLogs.collectAsStateWithLifecycle()

    val mySurveys = allSurveys.filter { it.participantCode == participantCode }
    val mySessions = allSessions.filter { it.participantCode == participantCode }
    val myAudio = allAudioLogs.filter { it.participantCode == participantCode }

    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "My Cryptographically Sealed Logs",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = ScrollTextPrimary
                    )
                    Text(
                        text = "ID: $participantCode",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = ScrollRoyalBlue
                    )
                }
            }
        }

        if (mySurveys.isEmpty() && mySessions.isEmpty() && myAudio.isEmpty()) {
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
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = ScrollTextMuted,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No Sealed Records Found Yet",
                            fontWeight = FontWeight.SemiBold,
                            color = ScrollTextPrimary,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Complete the Questionnaire, log a Media Session, or record an Oral Voice log to generate tamper-proof records.",
                            fontSize = 11.5.sp,
                            color = ScrollTextMuted,
                            modifier = Modifier.padding(top = 4.dp),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // Survey Entries
        items(mySurveys) { s ->
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
                        Text("Questionnaire Entry", fontWeight = FontWeight.Bold, color = ScrollRoyalBlue, fontSize = 13.sp)
                        Text(sdf.format(Date(s.timestamp)), fontSize = 10.sp, color = ScrollTextMuted)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Section: ${s.section} • Age: ${s.age} • Allowance: ${s.weeklyAllowance}", fontSize = 11.5.sp, color = ScrollTextPrimary)
                    Text("Awareness Score: ${String.format(Locale.US, "%.2f", s.overallAwarenessMean)} • Functionality: ${String.format(Locale.US, "%.2f", s.overallFunctionalityMean)}", fontSize = 11.5.sp, color = ScrollTextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    TamperProofBadge(isUntampered = s.isUntampered(), sha256Hash = s.sha256Hash)
                }
            }
        }

        // Media Sessions
        items(mySessions) { sess ->
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
                        Text("${sess.platform} Session", fontWeight = FontWeight.Bold, color = ScrollRoyalBlue, fontSize = 13.sp)
                        Text("${sess.durationSeconds / 60}m ${sess.durationSeconds % 60}s", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ScrollElectricBlue)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Category: ${sess.contentCategory}", fontSize = 11.5.sp, color = ScrollTextPrimary)
                    if (sess.impulseBuyUrgeTriggered) {
                        Text("Budol Trigger: ${sess.productNameViewed} (₱${sess.estimatedPricePhp})", fontSize = 11.sp, color = ScrollLockedRed)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TamperProofBadge(isUntampered = sess.isUntampered(), sha256Hash = sess.sha256Hash)
                }
            }
        }

        // Oral Audio Logs
        items(myAudio) { a ->
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
                        Text("Oral Audio Log: ${a.title}", fontWeight = FontWeight.Bold, color = ScrollRoyalBlue, fontSize = 13.sp)
                        Text("${a.durationSeconds}s", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ScrollTextMuted)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("\"${a.transcriptNotes}\"", fontSize = 11.5.sp, color = ScrollTextSecondary, lineHeight = 15.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    TamperProofBadge(isUntampered = a.isUntampered(), sha256Hash = a.sha256Hash)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}
