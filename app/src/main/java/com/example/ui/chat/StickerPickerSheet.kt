package com.example.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBg
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberCyan

data class CyberSticker(
    val name: String,
    val emoji: String,
    val tag: String,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StickerPickerSheet(
    onDismiss: () -> Unit,
    onSelectSticker: (String, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    val stickers = listOf(
        CyberSticker("CLASSIFIED", "🛑", "TOP SECRET", Color(0xFFEF4444)),
        CyberSticker("GHOST PROTOCOL", "👻", "ZERO TRACE", Color(0xFF00F0FF)),
        CyberSticker("ENCRYPTED", "🔐", "AES-256", Color(0xFF10B981)),
        CyberSticker("BURNING INTEL", "🔥", "SELF-DESTRUCT", Color(0xFFF59E0B)),
        CyberSticker("MATRIX GLITCH", "👾", "NULL POINTER", Color(0xFF8B5CF6)),
        CyberSticker("SKULL BREACH", "💀", "OVERRIDE", Color(0xFFFF0055)),
        CyberSticker("RADIO SILENCE", "🤫", "STAY DARK", Color(0xFF38BDF8)),
        CyberSticker("SIGNAL BOOST", "⚡", "HIGH FREQ", Color(0xFFFBBF24)),
        CyberSticker("HACKER CAT", "🐱‍💻", "ROOT ACCESS", Color(0xFF34D399)),
        CyberSticker("CYBER NET", "🌐", "GLOBAL RELAY", Color(0xFF60A5FA))
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CyberBg,
        modifier = Modifier.testTag("sticker_picker_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "INTEL STICKERS & EMBLEMS",
                    color = CyberCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(stickers) { sticker ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyberCard)
                            .border(1.dp, sticker.color.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clickable {
                                onSelectSticker(sticker.name, sticker.emoji)
                                onDismiss()
                            }
                            .padding(14.dp)
                            .testTag("sticker_item_${sticker.name}")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = sticker.emoji,
                                fontSize = 36.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = sticker.name,
                                color = sticker.color,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Text(
                                text = sticker.tag,
                                color = Color(0xFF94A3B8),
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
