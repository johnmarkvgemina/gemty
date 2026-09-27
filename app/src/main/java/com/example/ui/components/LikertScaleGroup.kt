package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ScrollBorder
import com.example.ui.theme.ScrollElectricBlue
import com.example.ui.theme.ScrollLightBlueContainer
import com.example.ui.theme.ScrollRoyalBlue
import com.example.ui.theme.ScrollSurface
import com.example.ui.theme.ScrollTextPrimary
import com.example.ui.theme.ScrollTextSecondary
import com.example.ui.theme.ScrollWhite

@Composable
fun LikertScaleGroup(
    itemNumber: String,
    questionText: String,
    subIndicatorLabel: String? = null,
    selectedScore: Int,
    isReadOnly: Boolean = false,
    onScoreSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("likert_item_$itemNumber"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ScrollSurface),
        border = BorderStroke(1.dp, ScrollBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = ScrollLightBlueContainer,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = itemNumber,
                        color = ScrollRoyalBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (subIndicatorLabel != null) {
                    Text(
                        text = subIndicatorLabel,
                        color = ScrollElectricBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 8.dp, top = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = questionText,
                color = ScrollTextPrimary,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 4-Point Likert options
            val options = listOf(
                1 to "1 - Strongly Disagree",
                2 to "2 - Disagree",
                3 to "3 - Agree",
                4 to "4 - Strongly Agree"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                options.forEach { (score, label) ->
                    val isSelected = selectedScore == score
                    val shortLabel = when (score) {
                        1 -> "1\nSD"
                        2 -> "2\nD"
                        3 -> "3\nA"
                        4 -> "4\nSA"
                        else -> "$score"
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("likert_${itemNumber}_option_$score"),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) ScrollRoyalBlue else ScrollLightBlueContainer.copy(alpha = 0.5f),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) ScrollRoyalBlue else ScrollBorder
                        ),
                        onClick = {
                            if (!isReadOnly) {
                                onScoreSelected(score)
                            }
                        }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = shortLabel,
                                color = if (isSelected) ScrollWhite else ScrollTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
