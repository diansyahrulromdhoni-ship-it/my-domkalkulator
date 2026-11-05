package com.example.calculator.ui.components

import android.widget.Toast
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.viewmodel.CalculatorUiState
import com.example.ui.theme.DarkOledBackground
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun DisplayScreen(
    state: CalculatorUiState,
    onOpenHistory: () -> Unit,
    onToggleScientific: () -> Unit,
    onSwipeBackspace: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current

    // Drag detection for swipe-to-backspace
    var dragAccumulated by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 16.dp, bottom = 12.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { dragAccumulated = 0f },
                    onHorizontalDrag = { _, dragAmount ->
                        dragAccumulated += dragAmount
                        if (Math.abs(dragAccumulated) > 40f) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSwipeBackspace()
                            dragAccumulated = 0f
                        }
                    }
                )
            },
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // TOP HEADER BAR: History icon on the left, Rounded Scientific toggle on the right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Top Left: Menu / History bullet list icon in Glowing Neon Blue
            IconButton(
                onClick = onOpenHistory,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("top_history_button")
            ) {
                Icon(
                    imageVector = Icons.Default.FormatListBulleted,
                    contentDescription = "Riwayat Perhitungan",
                    tint = NeonBlue,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Top Right: Squircle container with Chevron Right (scientific mode toggle)
            Surface(
                onClick = onToggleScientific,
                shape = RoundedCornerShape(18.dp),
                color = if (state.isScientificExpanded) NeonBlue.copy(alpha = 0.25f) else Color(0xFF1E242E),
                border = if (state.isScientificExpanded) ButtonDefaults.outlinedButtonBorder else null,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("top_scientific_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Buka Mode Ilmiah",
                        tint = NeonBlue,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // DISPLAY READOUT AREA (Matches attached screenshot)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Line 1: Expression (e.g. "1 500×12")
            if (state.topExpressionDisplay.isNotEmpty()) {
                Text(
                    text = state.topExpressionDisplay,
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextMuted,
                    textAlign = TextAlign.End,
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expression_display")
                )
            } else {
                Spacer(modifier = Modifier.height(36.dp))
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Line 2: Large primary result / number (e.g. "18 000")
            // Automatically adjusts font size so large numbers fit comfortably
            val displayLength = state.displayValue.length
            val fontSize = when {
                displayLength > 12 -> 40.sp
                displayLength > 9 -> 50.sp
                displayLength > 6 -> 60.sp
                else -> 72.sp
            }

            Text(
                text = if (state.errorMessage != null) state.errorMessage else state.displayValue,
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = fontSize,
                fontWeight = FontWeight.Light,
                color = if (state.errorMessage != null) Color(0xFFFF5252) else TextWhite,
                textAlign = TextAlign.End,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("result_display")
            )
        }
    }
}
