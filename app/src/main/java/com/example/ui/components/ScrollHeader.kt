package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ScrollBorder
import com.example.ui.theme.ScrollDeepBlue
import com.example.ui.theme.ScrollElectricBlue
import com.example.ui.theme.ScrollLightBlueContainer
import com.example.ui.theme.ScrollOnBlueContainer
import com.example.ui.theme.ScrollRoyalBlue
import com.example.ui.theme.ScrollWhite
import com.example.ui.viewmodel.AppMode

@Composable
fun ScrollHeader(
    currentMode: AppMode,
    onModeChange: (AppMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("scroll_header"),
        color = ScrollRoyalBlue,
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)
        ) {
            // Top branding row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cat emblem badge inspired by chic (3).png & S.png
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(ScrollWhite),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Stylized cute eyes
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(ScrollRoyalBlue)
                            )
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(ScrollRoyalBlue)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "▲",
                            color = ScrollRoyalBlue,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SCROLL",
                        color = ScrollWhite,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "SMART CONTENT RECOGNITION FOR ONLINE LIFESTYLE LOGGING",
                        color = ScrollLightBlueContainer.copy(alpha = 0.9f),
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 11.sp
                    )
                }

                // Tamper-proof security tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(ScrollDeepBlue.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "SHA-256 Tamper-Proof",
                            tint = Color(0xFF6EE7B7),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "SHA-256",
                            color = ScrollWhite,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Lawang Bato National High School • Grade 11 ABM Practical Research 2",
                color = ScrollWhite.copy(alpha = 0.8f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(14.dp))

            // App Persona Switcher Pill (Participant Logger vs Researcher Central Receiver)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = ScrollDeepBlue
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    // Participant App Button
                    val isParticipant = currentMode == AppMode.PARTICIPANT_APP
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("switch_to_participant_app"),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isParticipant) ScrollWhite else Color.Transparent,
                        onClick = { onModeChange(AppMode.PARTICIPANT_APP) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assignment,
                                contentDescription = null,
                                tint = if (isParticipant) ScrollRoyalBlue else ScrollWhite.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Participant Logger",
                                color = if (isParticipant) ScrollRoyalBlue else ScrollWhite.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                fontWeight = if (isParticipant) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }

                    // Researcher Receiver Vault Button
                    val isResearcher = currentMode == AppMode.RESEARCHER_VAULT
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("switch_to_researcher_vault"),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isResearcher) ScrollWhite else Color.Transparent,
                        onClick = { onModeChange(AppMode.RESEARCHER_VAULT) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = null,
                                tint = if (isResearcher) ScrollRoyalBlue else ScrollWhite.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Researcher Vault",
                                color = if (isResearcher) ScrollRoyalBlue else ScrollWhite.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                fontWeight = if (isResearcher) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
