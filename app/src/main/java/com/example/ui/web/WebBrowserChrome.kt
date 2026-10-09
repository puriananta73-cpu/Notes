package com.example.ui.web

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberNeonGreen
import com.example.ui.theme.CyberPanicRed

@Composable
fun WebBrowserChrome(
    currentUrl: String,
    onUrlSubmit: (String) -> Unit,
    onReload: () -> Unit,
    onBack: () -> Unit,
    isSecretMode: Boolean,
    onPanicToGoogle: () -> Unit
) {
    var urlInput by remember(currentUrl) { mutableStateOf(currentUrl) }

    val browserBg = if (isSecretMode) Color(0xFF0B0F17) else Color(0xFFE2E8F0)
    val tabBg = if (isSecretMode) Color(0xFF131C2E) else Color(0xFFFFFFFF)
    val textPrimary = if (isSecretMode) Color.White else Color(0xFF1E293B)
    val textMuted = if (isSecretMode) Color(0xFF94A3B8) else Color(0xFF64748B)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(browserBg)
            .testTag("web_browser_chrome")
    ) {
        // Tab Strip & Window Dots
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Window control dots (Mac/Web browser style)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF5F56))
                        .clickable { onPanicToGoogle() }
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFBD2E))
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF27C93F))
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Active Browser Tab
            Surface(
                color = tabBg,
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .padding(top = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSecretMode) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(CyberNeonGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SHADOWCOM Web [0xAPAP]",
                            color = CyberCyan,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "QuickNote Web App",
                            color = textPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Tab",
                        tint = textMuted,
                        modifier = Modifier
                            .size(12.dp)
                            .clickable { onPanicToGoogle() }
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Fake Inactive Tab (+)
            Text(
                text = "+",
                color = textMuted,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp)
            )
        }

        // Navigation Bar & URL Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back & Forward
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = textMuted,
                    modifier = Modifier.size(16.dp)
                )
            }

            IconButton(
                onClick = { /* forward */ },
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Forward",
                    tint = textMuted.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }

            IconButton(
                onClick = onReload,
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reload page",
                    tint = textMuted,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // URL Address Input Box
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isSecretMode) Color(0xFF080C14) else Color.White)
                    .border(
                        1.dp,
                        if (isSecretMode) CyberCyan.copy(alpha = 0.4f) else Color(0xFFCBD5E1),
                        RoundedCornerShape(18.dp)
                    )
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (isSecretMode) Icons.Default.Security else Icons.Default.Lock,
                        contentDescription = "SSL Encrypted",
                        tint = if (isSecretMode) CyberNeonGreen else Color(0xFF10B981),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))

                    BasicTextField(
                        value = urlInput,
                        onValueChange = {
                            urlInput = it
                            if (it.trim().endsWith("#APAP") || it.trim().equals("#APAP", ignoreCase = true)) {
                                onUrlSubmit("#APAP")
                            }
                        },
                        textStyle = TextStyle(
                            fontSize = 12.sp,
                            color = if (isSecretMode) CyberCyan else textPrimary,
                            fontFamily = FontFamily.Monospace
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(if (isSecretMode) CyberCyan else textPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("browser_address_bar")
                    )

                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Bookmark",
                        tint = textMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Instant Panic button on the website
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSecretMode) CyberPanicRed else Color(0xFF0F172A),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onPanicToGoogle() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "PANIC",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Web Bookmarks Strip (Only shown in innocent notes mode)
        if (!isSecretMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("★ My Notes", color = textMuted, fontSize = 11.sp)
                Text("★ Cloud Drive", color = textMuted, fontSize = 11.sp)
                Text("★ Docs", color = textMuted, fontSize = 11.sp)
                Text("★ Calendar", color = textMuted, fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
