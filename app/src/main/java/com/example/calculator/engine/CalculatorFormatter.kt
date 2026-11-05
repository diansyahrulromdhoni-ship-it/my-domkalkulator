package com.example.calculator.engine

object CalculatorFormatter {

    /**
     * Formats a raw number or result string with space as thousands separator
     * and comma as decimal separator, matching the attached screenshot:
     * e.g. "18000" -> "18 000", "1500.5" -> "1 500,5"
     */
    fun formatNumber(valueStr: String): String {
        if (valueStr.isEmpty()) return "0"
        if (valueStr == "Bukan angka" || valueStr == "Tak terhingga (÷ 0)" || valueStr == "Format salah" || valueStr == "∞" || valueStr == "-∞") {
            return valueStr
        }

        val isNegative = valueStr.startsWith("-") || valueStr.startsWith("−")
        val clean = valueStr.removePrefix("-").removePrefix("−")

        val parts = clean.split('.', ',')
        val intPart = parts[0]
        val decPart = if (parts.size > 1) parts[1] else null

        val formattedInt = formatIntegerWithSpaces(intPart)
        val prefix = if (isNegative) "−" else ""

        return if (decPart != null) {
            "$prefix$formattedInt,$decPart"
        } else {
            "$prefix$formattedInt"
        }
    }

    /**
     * Formats an ongoing expression with spaced thousands and clean operators
     * e.g. "1500*12" -> "1 500×12"
     */
    fun formatExpression(expr: String): String {
        if (expr.isEmpty()) return ""

        val sb = StringBuilder()
        var currentNum = StringBuilder()

        for (ch in expr) {
            if (ch.isDigit() || ch == '.' || ch == ',') {
                currentNum.append(ch)
            } else {
                if (currentNum.isNotEmpty()) {
                    sb.append(formatNumber(currentNum.toString()))
                    currentNum.clear()
                }
                when (ch) {
                    '*' -> sb.append("×")
                    '/' -> sb.append("÷")
                    '-' -> sb.append("−")
                    else -> sb.append(ch)
                }
            }
        }

        if (currentNum.isNotEmpty()) {
            sb.append(formatNumber(currentNum.toString()))
        }

        return sb.toString()
    }

    private fun formatIntegerWithSpaces(str: String): String {
        if (str.length <= 3) return str
        val reversed = str.reversed()
        val chunked = reversed.chunked(3).joinToString(" ")
        return chunked.reversed()
    }
}
