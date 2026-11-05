package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Pure OLED Black Background (matches attached screenshot)
val DarkOledBackground = Color(0xFF000000)
val DarkOledSurface = Color(0xFF0D121B)
val DarkOledSurfaceVariant = Color(0xFF161E2E)

// Radiant Neon Blue Theme
val NeonBlue = Color(0xFF00E5FF)             // Vibrant electric cyan/neon blue
val ElectricCyan = NeonBlue                  // Color alias for components
val NeonBlueGlow = Color(0xFF00B0FF)         // Rich neon cyan
val NeonBlueOperator = Color(0xFF0284C7)     // Deep neon blue for keypad operators
val NeonBlueOperatorActive = Color(0xFF38BDF8)
val NeonBlueContainer = Color(0xFF0B253A)
val NeonBlueBorder = Color(0xFF00E5FF).copy(alpha = 0.3f)

// Vibrant accents for tools & alerts
val NeonAmber = Color(0xFFF59E0B)
val NeonGreen = Color(0xFF10B981)
val NeonPink = Color(0xFFFF5252)

// Keypad Button Colors matching the screenshot with Neon Blue accents
val KeyNumberBg = Color(0xFF262A34)          // Dark circular key for digits 0-9 and comma
val KeyFunctionBg = Color(0xFF3D4758)        // Slate circular key for AC, +/-, %
val KeyOperatorBg = Color(0xFF0091EA)        // Radiant Neon Blue for operators ÷, ×, −, +, =
val KeyOperatorActiveBg = Color(0xFFE0F7FA)  // Inverted highlight when operator is selected
val KeyOperatorActiveText = Color(0xFF0077B6)

// Text Colors
val TextWhite = Color(0xFFFFFFFF)
val TextMuted = Color(0xFFA0ABBA)
val TextDark = Color(0xFF0B1320)
