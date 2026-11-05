package com.example.calculator.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.viewmodel.CalculatorUiState
import com.example.ui.theme.*

@Composable
fun Keypad(
    state: CalculatorUiState,
    onKeyClick: (String) -> Unit,
    onOpenTools: () -> Unit,
    onToggleAngleMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Expandable Scientific Keypad Panel
        AnimatedVisibility(
            visible = state.isScientificExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Row S0: Angle mode (RAD/DEG) chip & memory
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = onToggleAngleMode,
                        shape = RoundedCornerShape(12.dp),
                        color = if (state.isRadian) NeonBlue.copy(alpha = 0.25f) else Color(0xFF1E242E),
                        modifier = Modifier.testTag("sci_angle_mode")
                    ) {
                        Text(
                            text = if (state.isRadian) "RAD" else "DEG",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (state.isRadian) NeonBlue else TextMuted
                            ),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }

                    Text(
                        text = "Mode Ilmiah",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonBlueGlow
                    )
                }

                // Row S1: sin, cos, tan, ln, log
                ScientificPillRow(
                    keys = listOf("sin", "cos", "tan", "ln", "log"),
                    onKeyClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onKeyClick(it)
                    }
                )

                // Row S2: x², xʸ, √, π, e
                ScientificPillRow(
                    keys = listOf("x²", "xʸ", "√", "π", "e"),
                    onKeyClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onKeyClick(it)
                    }
                )

                // Row S3: x!, 1/x, (, ), abs
                ScientificPillRow(
                    keys = listOf("x!", "1/x", "(", ")", "abs"),
                    onKeyClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onKeyClick(it)
                    }
                )
            }
        }

        // Standard 4x5 Keypad Grid (Matches attached user screenshot)
        val clearLabel = if (state.expression.isEmpty() && state.displayValue == "0") "AC" else "C"

        // ROW 1: AC, +/-, %, ÷
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CircleKeyButton(
                text = clearLabel,
                backgroundColor = KeyFunctionBg,
                textColor = TextWhite,
                onClick = { onKeyClick(clearLabel) }
            )
            CircleKeyButton(
                text = "+/-",
                backgroundColor = KeyFunctionBg,
                textColor = TextWhite,
                onClick = { onKeyClick("+/-") }
            )
            CircleKeyButton(
                text = "%",
                backgroundColor = KeyFunctionBg,
                textColor = TextWhite,
                onClick = { onKeyClick("%") }
            )
            CircleKeyButton(
                text = "÷",
                backgroundColor = if (state.activeOperator == "÷") KeyOperatorActiveBg else KeyOperatorBg,
                textColor = if (state.activeOperator == "÷") KeyOperatorActiveText else TextWhite,
                onClick = { onKeyClick("÷") }
            )
        }

        // ROW 2: 7, 8, 9, ×
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CircleKeyButton(
                text = "7",
                backgroundColor = KeyNumberBg,
                textColor = TextWhite,
                onClick = { onKeyClick("7") }
            )
            CircleKeyButton(
                text = "8",
                backgroundColor = KeyNumberBg,
                textColor = TextWhite,
                onClick = { onKeyClick("8") }
            )
            CircleKeyButton(
                text = "9",
                backgroundColor = KeyNumberBg,
                textColor = TextWhite,
                onClick = { onKeyClick("9") }
            )
            CircleKeyButton(
                text = "×",
                backgroundColor = if (state.activeOperator == "×") KeyOperatorActiveBg else KeyOperatorBg,
                textColor = if (state.activeOperator == "×") KeyOperatorActiveText else TextWhite,
                onClick = { onKeyClick("×") }
            )
        }

        // ROW 3: 4, 5, 6, −
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CircleKeyButton(
                text = "4",
                backgroundColor = KeyNumberBg,
                textColor = TextWhite,
                onClick = { onKeyClick("4") }
            )
            CircleKeyButton(
                text = "5",
                backgroundColor = KeyNumberBg,
                textColor = TextWhite,
                onClick = { onKeyClick("5") }
            )
            CircleKeyButton(
                text = "6",
                backgroundColor = KeyNumberBg,
                textColor = TextWhite,
                onClick = { onKeyClick("6") }
            )
            CircleKeyButton(
                text = "−",
                backgroundColor = if (state.activeOperator == "−") KeyOperatorActiveBg else KeyOperatorBg,
                textColor = if (state.activeOperator == "−") KeyOperatorActiveText else TextWhite,
                onClick = { onKeyClick("−") }
            )
        }

        // ROW 4: 1, 2, 3, +
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CircleKeyButton(
                text = "1",
                backgroundColor = KeyNumberBg,
                textColor = TextWhite,
                onClick = { onKeyClick("1") }
            )
            CircleKeyButton(
                text = "2",
                backgroundColor = KeyNumberBg,
                textColor = TextWhite,
                onClick = { onKeyClick("2") }
            )
            CircleKeyButton(
                text = "3",
                backgroundColor = KeyNumberBg,
                textColor = TextWhite,
                onClick = { onKeyClick("3") }
            )
            CircleKeyButton(
                text = "+",
                backgroundColor = if (state.activeOperator == "+") KeyOperatorActiveBg else KeyOperatorBg,
                textColor = if (state.activeOperator == "+") KeyOperatorActiveText else TextWhite,
                onClick = { onKeyClick("+") }
            )
        }

        // ROW 5: [Calculator/Tools Icon], 0, ,, =
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Bottom-Left Calculator Keypad Icon button (opens Converter & Finance Tools)
            CircleIconButton(
                icon = Icons.Default.Calculate,
                backgroundColor = KeyNumberBg,
                iconColor = TextWhite,
                onClick = onOpenTools,
                contentDescription = "Menu Alat & Konverter Satuan"
            )
            CircleKeyButton(
                text = "0",
                backgroundColor = KeyNumberBg,
                textColor = TextWhite,
                onClick = { onKeyClick("0") }
            )
            CircleKeyButton(
                text = ",",
                backgroundColor = KeyNumberBg,
                textColor = TextWhite,
                onClick = { onKeyClick(",") }
            )
            CircleKeyButton(
                text = "=",
                backgroundColor = KeyOperatorBg,
                textColor = TextWhite,
                onClick = { onKeyClick("=") }
            )
        }
    }
}

@Composable
fun CircleKeyButton(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .testTag("key_$text"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = PlusJakartaSansFontFamily,
            fontSize = when (text) {
                "AC", "+/-" -> 28.sp
                "÷", "×", "−", "+", "=" -> 36.sp
                "," -> 38.sp
                else -> 34.sp
            },
            fontWeight = when (text) {
                "÷", "×", "−", "+", "=" -> FontWeight.Medium
                else -> FontWeight.Normal
            },
            color = textColor
        )
    }
}

@Composable
fun CircleIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .testTag("key_tools_icon"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(34.dp)
        )
    }
}

@Composable
fun ScientificPillRow(
    keys: List<String>,
    onKeyClick: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        keys.forEach { key ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF161E2E))
                    .clickable { onKeyClick(key) }
                    .testTag("sci_key_$key"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = key,
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NeonBlue
                )
            }
        }
    }
}
