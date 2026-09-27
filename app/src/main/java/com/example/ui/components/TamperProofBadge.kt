package com.example.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ScrollDeepBlue
import com.example.ui.theme.ScrollGreenContainer
import com.example.ui.theme.ScrollLockedRed
import com.example.ui.theme.ScrollRoyalBlue
import com.example.ui.theme.ScrollVerifiedGreen
import com.example.ui.theme.ScrollWhite

@Composable
fun TamperProofBadge(
    isUntampered: Boolean,
    sha256Hash: String,
    participantCode: String? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tamper_proof_badge"),
        shape = RoundedCornerShape(10.dp),
        color = if (isUntampered) ScrollGreenContainer else Color(0xFFFFECEB),
        border = BorderStroke(
            1.dp,
            if (isUntampered) ScrollVerifiedGreen.copy(alpha = 0.4f) else ScrollLockedRed.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isUntampered) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = if (isUntampered) "Tamper Proof Record" else "Tamper Warning",
                tint = if (isUntampered) ScrollVerifiedGreen else ScrollLockedRed,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (isUntampered) ScrollVerifiedGreen else ScrollLockedRed,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = if (isUntampered) "LOCKED: Cryptographically Sealed & Untampered" else "SECURITY ALERT: Record Integrity Failed",
                        color = if (isUntampered) Color(0xFF065F46) else ScrollLockedRed,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "SHA-256: ${if (sha256Hash.length > 20) sha256Hash.take(16) + "..." + sha256Hash.takeLast(8) else sha256Hash}",
                    color = Color(0xFF374151),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
