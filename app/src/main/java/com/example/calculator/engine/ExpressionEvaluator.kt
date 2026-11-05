package com.example.calculator.engine

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.*

class ExpressionEvaluator(private val isRadian: Boolean = false) {

    sealed class EvalResult {
        data class Success(val value: Double, val formatted: String) : EvalResult()
        data class Error(val message: String) : EvalResult()
    }

    /**
     * Evaluates a mathematical expression string.
     */
    fun evaluate(rawExpression: String): EvalResult {
        if (rawExpression.isBlank()) return EvalResult.Error("")

        val sanitized = sanitize(rawExpression)
        if (sanitized.isBlank()) return EvalResult.Error("")

        return try {
            val parser = Parser(sanitized, isRadian)
            val result = parser.parseExpression()

            if (result.isNaN()) {
                EvalResult.Error("Bukan angka")
            } else if (result.isInfinite()) {
                EvalResult.Error("Tak terhingga (÷ 0)")
            } else {
                EvalResult.Success(result, formatResult(result))
            }
        } catch (e: ArithmeticException) {
            EvalResult.Error(e.message ?: "Kesalahan matematika")
        } catch (e: Exception) {
            EvalResult.Error("Format salah")
        }
    }

    private fun sanitize(expr: String): String {
        var s = expr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace(" ", "")
            .replace(",", ".")

        // Auto-close open parentheses
        val openCount = s.count { it == '(' }
        val closeCount = s.count { it == ')' }
        if (openCount > closeCount) {
            s += ")".repeat(openCount - closeCount)
        }

        return s
    }

    private fun formatResult(value: Double): String {
        if (value.isNaN()) return "Bukan angka"
        if (value.isInfinite()) return if (value > 0) "∞" else "-∞"

        // Handle very large or very small numbers
        if (abs(value) >= 1e12 || (abs(value) > 0 && abs(value) < 1e-6)) {
            return String.format(java.util.Locale.US, "%.6e", value).replace("e+", "e")
        }

        // Clean floating point rounding issues
        val bd = try {
            BigDecimal.valueOf(value).setScale(10, RoundingMode.HALF_UP).stripTrailingZeros()
        } catch (e: Exception) {
            BigDecimal.valueOf(value)
        }

        val plainStr = bd.toPlainString()
        return if (plainStr == "-0") "0" else plainStr
    }

    private class Parser(private val src: String, private val isRadian: Boolean) {
        private var pos = 0
        private val len = src.length

        private fun peek(): Char = if (pos < len) src[pos] else '\u0000'
        private fun get(): Char = if (pos < len) src[pos++] else '\u0000'

        fun parseExpression(): Double {
            var value = parseTerm()
            while (pos < len) {
                when (peek()) {
                    '+' -> {
                        get()
                        val next = parseTerm()
                        value += next
                    }
                    '-' -> {
                        get()
                        val next = parseTerm()
                        value -= next
                    }
                    else -> break
                }
            }
            return value
        }

        private fun parseTerm(): Double {
            var value = parsePower()
            while (pos < len) {
                when (peek()) {
                    '*' -> {
                        get()
                        val next = parsePower()
                        value *= next
                    }
                    '/' -> {
                        get()
                        val next = parsePower()
                        if (abs(next) < 1e-15) {
                            throw ArithmeticException("Tak terhingga (÷ 0)")
                        }
                        value /= next
                    }
                    '%' -> {
                        get()
                        value /= 100.0
                    }
                    else -> break
                }
            }
            return value
        }

        private fun parsePower(): Double {
            var value = parseFactor()
            if (peek() == '^') {
                get()
                val exponent = parseFactor()
                value = value.pow(exponent)
            }
            // Check for factorial
            if (peek() == '!') {
                get()
                value = factorial(value)
            }
            return value
        }

        private fun parseFactor(): Double {
            // Handle leading unary signs
            if (peek() == '+') {
                get()
                return parseFactor()
            }
            if (peek() == '-') {
                get()
                return -parseFactor()
            }

            // Handle parentheses
            if (peek() == '(') {
                get()
                val value = parseExpression()
                if (peek() == ')') get()
                return value
            }

            // Handle named functions & constants
            if (peek().isLetter() || peek() == '√' || peek() == '∛' || peek() == 'π') {
                return parseFunctionOrConstant()
            }

            // Handle numbers
            if (peek().isDigit() || peek() == '.') {
                return parseNumber()
            }

            throw IllegalArgumentException("Karakter tidak diharapkan: ${peek()}")
        }

        private fun parseFunctionOrConstant(): Double {
            val start = pos
            if (peek() == '√') {
                get()
                val arg = parseFactor()
                if (arg < 0) throw ArithmeticException("Akar negatif")
                return sqrt(arg)
            }
            if (peek() == '∛') {
                get()
                val arg = parseFactor()
                return cbrt(arg)
            }
            if (peek() == 'π') {
                get()
                return Math.PI
            }

            val sb = StringBuilder()
            while (pos < len && (src[pos].isLetter() || src[pos] == '²' || src[pos] == '³')) {
                sb.append(src[pos++])
            }
            val name = sb.toString().lowercase()

            when (name) {
                "e" -> return Math.E
                "pi" -> return Math.PI
                "sin" -> {
                    val arg = parseFactor()
                    val rad = if (isRadian) arg else Math.toRadians(arg)
                    val res = sin(rad)
                    return if (abs(res) < 1e-15) 0.0 else res
                }
                "cos" -> {
                    val arg = parseFactor()
                    val rad = if (isRadian) arg else Math.toRadians(arg)
                    val res = cos(rad)
                    return if (abs(res) < 1e-15) 0.0 else res
                }
                "tan" -> {
                    val arg = parseFactor()
                    val rad = if (isRadian) arg else Math.toRadians(arg)
                    val cosVal = cos(rad)
                    if (abs(cosVal) < 1e-15) throw ArithmeticException("Tan tak hingga")
                    return tan(rad)
                }
                "asin" -> {
                    val arg = parseFactor()
                    if (arg < -1.0 || arg > 1.0) throw ArithmeticException("Domain asin [-1, 1]")
                    val rad = asin(arg)
                    return if (isRadian) rad else Math.toDegrees(rad)
                }
                "acos" -> {
                    val arg = parseFactor()
                    if (arg < -1.0 || arg > 1.0) throw ArithmeticException("Domain acos [-1, 1]")
                    val rad = acos(arg)
                    return if (isRadian) rad else Math.toDegrees(rad)
                }
                "atan" -> {
                    val arg = parseFactor()
                    val rad = atan(arg)
                    return if (isRadian) rad else Math.toDegrees(rad)
                }
                "ln" -> {
                    val arg = parseFactor()
                    if (arg <= 0) throw ArithmeticException("ln(x) memerlukan x > 0")
                    return ln(arg)
                }
                "log" -> {
                    val arg = parseFactor()
                    if (arg <= 0) throw ArithmeticException("log(x) memerlukan x > 0")
                    return log10(arg)
                }
                "sqrt" -> {
                    val arg = parseFactor()
                    if (arg < 0) throw ArithmeticException("Akar negatif")
                    return sqrt(arg)
                }
                "abs" -> {
                    val arg = parseFactor()
                    return abs(arg)
                }
                "exp" -> {
                    val arg = parseFactor()
                    return exp(arg)
                }
                else -> throw IllegalArgumentException("Fungsi tidak dikenal: $name")
            }
        }

        private fun parseNumber(): Double {
            val start = pos
            var hasDot = false
            while (pos < len) {
                val c = src[pos]
                if (c == '.') {
                    if (hasDot) break
                    hasDot = true
                    pos++
                } else if (c.isDigit()) {
                    pos++
                } else {
                    break
                }
            }
            val numStr = src.substring(start, pos)
            return numStr.toDoubleOrNull() ?: 0.0
        }

        private fun factorial(n: Double): Double {
            if (n < 0 || n != floor(n) || n > 170) {
                throw ArithmeticException("Faktorial hanya untuk bilangan bulat 0-170")
            }
            var result = 1.0
            for (i in 2..n.toInt()) {
                result *= i
            }
            return result
        }
    }
}
