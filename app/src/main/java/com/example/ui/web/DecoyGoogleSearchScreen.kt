package com.example.ui.web

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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

@Composable
fun DecoyGoogleSearchScreen(
    onReturnToNotes: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
            .testTag("decoy_google_search_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Google Logo Letters
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("G", color = Color(0xFF4285F4), fontSize = 42.sp, fontWeight = FontWeight.Bold)
            Text("o", color = Color(0xFFEA4335), fontSize = 42.sp, fontWeight = FontWeight.Bold)
            Text("o", color = Color(0xFFFBBC05), fontSize = 42.sp, fontWeight = FontWeight.Bold)
            Text("g", color = Color(0xFF4285F4), fontSize = 42.sp, fontWeight = FontWeight.Bold)
            Text("l", color = Color(0xFF34A853), fontSize = 42.sp, fontWeight = FontWeight.Bold)
            Text("e", color = Color(0xFFEA4335), fontSize = 42.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Fake Google Search Bar
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, Color(0xFFDFE1E5), RoundedCornerShape(24.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color(0xFF9AA0A6),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "best productivity notes and study habits 2026",
                    color = Color(0xFF202124),
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(
                color = Color(0xFFF8F9FA),
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF8F9FA)),
                modifier = Modifier.clickable { onReturnToNotes() }
            ) {
                Text(
                    text = "Google Search",
                    color = Color(0xFF3C4043),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Surface(
                color = Color(0xFFF8F9FA),
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF8F9FA)),
                modifier = Modifier.clickable { onReturnToNotes() }
            ) {
                Text(
                    text = "Return to QuickNote Cloud",
                    color = Color(0xFF1A73E8),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}
