package com.example.calculator.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calculator.data.CalculationHistoryItem
import com.example.calculator.data.HistoryRepository
import com.example.calculator.engine.CalculatorFormatter
import com.example.calculator.engine.ExpressionEvaluator
import com.example.calculator.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CalculatorUiState(
    // Expression & Display
    val expression: String = "",                  // e.g. "1500*12"
    val displayValue: String = "0",              // The big bold number on screen, e.g. "18 000"
    val topExpressionDisplay: String = "",       // The smaller text above big number, e.g. "1 500×12"
    val activeOperator: String? = null,          // Highlighted operator "+", "−", "×", "÷"
    val isEvaluated: Boolean = false,
    val isRadian: Boolean = false,
    val isScientificExpanded: Boolean = false,
    val showToolsSheet: Boolean = false,         // Bottom sheet for Converter & Finance tools
    val showHistorySheet: Boolean = false,       // Bottom sheet/drawer for History
    val historyList: List<CalculationHistoryItem> = emptyList(),
    val errorMessage: String? = null,

    // Tools Sheet active sub-tab (0: Unit Converter, 1: Diskon, 2: Bagi Tagihan)
    val toolsSubTab: Int = 0,

    // Unit Converter State
    val unitCategory: UnitCategory = UnitCategory.LENGTH,
    val fromUnit: ConversionUnit = UnitConverterData.categories[UnitCategory.LENGTH]!![1], // Meter
    val toUnit: ConversionUnit = UnitConverterData.categories[UnitCategory.LENGTH]!![0],   // Kilometer
    val unitInput: String = "1",
    val unitResult: String = "0.001",

    // Finance Tools - Diskon
    val discountOriginalPrice: String = "150000",
    val discountPercent: Float = 20f,
    val extraDiscountPercent: Float = 5f,
    val taxPercent: Float = 11f,
    val discountResult: FinanceTools.DiscountResult = FinanceTools.calculateDiscount(150000.0, 20.0, 5.0, 11.0),

    // Finance Tools - Split Bill
    val splitBillAmount: String = "250000",
    val splitTipPercent: Float = 10f,
    val splitNumPeople: Int = 4,
    val splitResult: FinanceTools.SplitBillResult = FinanceTools.calculateSplitBill(250000.0, 10.0, 4)
)

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val historyRepo = HistoryRepository(application)
    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            val list = historyRepo.loadHistory()
            _uiState.update { it.copy(historyList = list) }
        }
    }

    fun toggleScientific() {
        _uiState.update { it.copy(isScientificExpanded = !it.isScientificExpanded) }
    }

    fun toggleToolsSheet(show: Boolean? = null) {
        _uiState.update { it.copy(showToolsSheet = show ?: !it.showToolsSheet) }
    }

    fun toggleHistorySheet(show: Boolean? = null) {
        _uiState.update { it.copy(showHistorySheet = show ?: !it.showHistorySheet) }
    }

    fun setToolsSubTab(index: Int) {
        _uiState.update { it.copy(toolsSubTab = index) }
    }

    fun toggleAngleMode() {
        _uiState.update {
            val newRad = !it.isRadian
            val updated = it.copy(isRadian = newRad)
            recalculateLive(updated)
        }
    }

    fun onKeyClick(key: String) {
        when (key) {
            "AC" -> clearAll()
            "C" -> clearAll()
            "⌫" -> backspace()
            "=" -> evaluateFinal()
            "+/-" -> toggleSign()
            "%" -> appendPercentage()
            "+", "−", "×", "÷" -> appendOperator(key)
            ",", "." -> appendDecimalComma()
            "sin", "cos", "tan", "asin", "acos", "atan", "ln", "log", "sqrt", "abs" -> appendFunction(key)
            "x²" -> appendExponent("^2")
            "x³" -> appendExponent("^3")
            "xʸ" -> appendOperator("^")
            "√" -> appendFunction("sqrt")
            "∛" -> appendFunction("∛")
            "1/x" -> appendReciprocal()
            "x!" -> appendFactorial()
            "π" -> appendConstant("π")
            "e" -> appendConstant("e")
            "(", ")" -> appendParenthesis(key)
            else -> appendDigit(key)
        }
    }

    private fun clearAll() {
        _uiState.update {
            it.copy(
                expression = "",
                displayValue = "0",
                topExpressionDisplay = "",
                activeOperator = null,
                isEvaluated = false,
                errorMessage = null
            )
        }
    }

    fun backspace() {
        _uiState.update { state ->
            if (state.isEvaluated) {
                state.copy(isEvaluated = false, topExpressionDisplay = "")
            } else if (state.expression.isNotEmpty()) {
                val newExpr = state.expression.dropLast(1)
                val updated = state.copy(expression = newExpr, activeOperator = null)
                recalculateLive(updated)
            } else {
                state
            }
        }
    }

    private fun appendDigit(digit: String) {
        _uiState.update { state ->
            val expr = if (state.isEvaluated) {
                digit
            } else {
                state.expression + digit
            }
            val updated = state.copy(
                expression = expr,
                isEvaluated = false,
                activeOperator = null,
                errorMessage = null
            )
            recalculateLive(updated)
        }
    }

    private fun appendDecimalComma() {
        _uiState.update { state ->
            val expr = if (state.isEvaluated) {
                "0."
            } else if (state.expression.isEmpty()) {
                "0."
            } else {
                val lastNumber = state.expression.takeLastWhile { it.isDigit() || it == '.' || it == ',' }
                if (!lastNumber.contains('.') && !lastNumber.contains(',')) {
                    val prefix = if (!state.expression.last().isDigit()) "0" else ""
                    state.expression + prefix + "."
                } else {
                    state.expression
                }
            }
            val updated = state.copy(
                expression = expr,
                isEvaluated = false,
                activeOperator = null,
                errorMessage = null
            )
            recalculateLive(updated)
        }
    }

    private fun appendOperator(op: String) {
        val mathOp = when (op) {
            "×" -> "*"
            "÷" -> "/"
            "−" -> "-"
            else -> op
        }

        _uiState.update { state ->
            val baseExpr = if (state.isEvaluated) {
                state.displayValue.replace(" ", "").replace(",", ".")
            } else {
                state.expression
            }

            if (baseExpr.isEmpty()) {
                if (op == "−" || op == "-") {
                    state.copy(expression = "-", activeOperator = op, isEvaluated = false)
                } else {
                    state
                }
            } else {
                val lastChar = baseExpr.last().toString()
                val operators = listOf("+", "-", "*", "/", "^")
                val newExpr = if (operators.contains(lastChar)) {
                    baseExpr.dropLast(1) + mathOp
                } else {
                    baseExpr + mathOp
                }
                state.copy(
                    expression = newExpr,
                    activeOperator = op,
                    isEvaluated = false,
                    topExpressionDisplay = CalculatorFormatter.formatExpression(newExpr)
                )
            }
        }
    }

    private fun appendPercentage() {
        _uiState.update { state ->
            val expr = if (state.isEvaluated) {
                state.displayValue.replace(" ", "").replace(",", ".") + "%"
            } else if (state.expression.isNotEmpty()) {
                state.expression + "%"
            } else {
                state.expression
            }
            val updated = state.copy(expression = expr, isEvaluated = false)
            recalculateLive(updated)
        }
    }

    private fun toggleSign() {
        _uiState.update { state ->
            val expr = if (state.isEvaluated) {
                state.displayValue.replace(" ", "").replace(",", ".")
            } else {
                state.expression
            }

            if (expr.isEmpty() || expr == "0") {
                state.copy(expression = "-", displayValue = "-0")
            } else if (expr.startsWith("-")) {
                val updated = state.copy(expression = expr.removePrefix("-"), isEvaluated = false)
                recalculateLive(updated)
            } else {
                val updated = state.copy(expression = "-$expr", isEvaluated = false)
                recalculateLive(updated)
            }
        }
    }

    private fun appendFunction(fn: String) {
        _uiState.update { state ->
            val expr = if (state.isEvaluated) "" else state.expression
            val separator = if (expr.isNotEmpty() && (expr.last().isDigit() || expr.last() == ')')) "*" else ""
            val newExpr = "$expr$separator$fn("
            val updated = state.copy(expression = newExpr, isEvaluated = false, activeOperator = null)
            recalculateLive(updated)
        }
    }

    private fun appendConstant(symbol: String) {
        _uiState.update { state ->
            val expr = if (state.isEvaluated) "" else state.expression
            val separator = if (expr.isNotEmpty() && (expr.last().isDigit() || expr.last() == ')')) "*" else ""
            val newExpr = "$expr$separator$symbol"
            val updated = state.copy(expression = newExpr, isEvaluated = false, activeOperator = null)
            recalculateLive(updated)
        }
    }

    private fun appendExponent(exp: String) {
        _uiState.update { state ->
            val expr = if (state.isEvaluated) {
                state.displayValue.replace(" ", "").replace(",", ".")
            } else {
                state.expression
            }
            if (expr.isNotEmpty()) {
                val updated = state.copy(expression = expr + exp, isEvaluated = false)
                recalculateLive(updated)
            } else {
                state
            }
        }
    }

    private fun appendReciprocal() {
        _uiState.update { state ->
            val expr = if (state.isEvaluated) {
                state.displayValue.replace(" ", "").replace(",", ".")
            } else {
                state.expression
            }
            val newExpr = if (expr.isEmpty()) "1/(" else "1/($expr)"
            val updated = state.copy(expression = newExpr, isEvaluated = false)
            recalculateLive(updated)
        }
    }

    private fun appendFactorial() {
        _uiState.update { state ->
            val expr = if (state.isEvaluated) {
                state.displayValue.replace(" ", "").replace(",", ".")
            } else {
                state.expression
            }
            if (expr.isNotEmpty()) {
                val updated = state.copy(expression = "$expr!", isEvaluated = false)
                recalculateLive(updated)
            } else {
                state
            }
        }
    }

    private fun appendParenthesis(paren: String) {
        _uiState.update { state ->
            val expr = if (state.isEvaluated) "" else state.expression
            val newExpr = if (paren == "(" && expr.isNotEmpty() && (expr.last().isDigit() || expr.last() == ')')) {
                "$expr*("
            } else {
                "$expr$paren"
            }
            val updated = state.copy(expression = newExpr, isEvaluated = false)
            recalculateLive(updated)
        }
    }

    private fun evaluateFinal() {
        val state = _uiState.value
        if (state.expression.isBlank()) return

        val evaluator = ExpressionEvaluator(isRadian = state.isRadian)
        when (val result = evaluator.evaluate(state.expression)) {
            is ExpressionEvaluator.EvalResult.Success -> {
                val formattedResult = CalculatorFormatter.formatNumber(result.formatted)
                val formattedExpr = CalculatorFormatter.formatExpression(state.expression)

                val historyItem = CalculationHistoryItem(
                    expression = formattedExpr,
                    result = formattedResult
                )
                val newHistory = listOf(historyItem) + state.historyList
                historyRepo.saveHistory(newHistory)

                _uiState.update {
                    it.copy(
                        displayValue = formattedResult,
                        topExpressionDisplay = formattedExpr,
                        activeOperator = null,
                        isEvaluated = true,
                        errorMessage = null,
                        historyList = newHistory
                    )
                }
            }
            is ExpressionEvaluator.EvalResult.Error -> {
                _uiState.update {
                    it.copy(
                        errorMessage = if (result.message.isNotEmpty()) result.message else "Format salah",
                        displayValue = "Error",
                        isEvaluated = true
                    )
                }
            }
        }
    }

    private fun recalculateLive(state: CalculatorUiState): CalculatorUiState {
        if (state.expression.isBlank()) {
            return state.copy(
                displayValue = "0",
                topExpressionDisplay = "",
                errorMessage = null
            )
        }

        val evaluator = ExpressionEvaluator(isRadian = state.isRadian)
        val formattedExpr = CalculatorFormatter.formatExpression(state.expression)

        // If expression has operators like 1500*12, evaluate live preview
        val hasOp = state.expression.any { it == '+' || it == '-' || it == '*' || it == '/' || it == '^' || it == '%' }

        return when (val res = evaluator.evaluate(state.expression)) {
            is ExpressionEvaluator.EvalResult.Success -> {
                val formattedRes = CalculatorFormatter.formatNumber(res.formatted)
                if (hasOp) {
                    state.copy(
                        displayValue = formattedRes,
                        topExpressionDisplay = formattedExpr,
                        errorMessage = null
                    )
                } else {
                    state.copy(
                        displayValue = formattedRes,
                        topExpressionDisplay = "",
                        errorMessage = null
                    )
                }
            }
            is ExpressionEvaluator.EvalResult.Error -> {
                // If incomplete e.g. "1500*", display the number so far or expression
                val lastNum = state.expression.takeLastWhile { it.isDigit() || it == '.' || it == ',' }
                val display = if (lastNum.isNotEmpty()) CalculatorFormatter.formatNumber(lastNum) else state.displayValue
                state.copy(
                    displayValue = display,
                    topExpressionDisplay = formattedExpr
                )
            }
        }
    }

    // History interaction
    fun useHistoryItem(item: CalculationHistoryItem, useResultOnly: Boolean = false) {
        val target = if (useResultOnly) {
            item.result.replace(" ", "").replace(",", ".")
        } else {
            item.expression.replace(" ", "").replace("×", "*").replace("÷", "/").replace("−", "-").replace(",", ".")
        }

        _uiState.update { state ->
            val updated = state.copy(
                expression = target,
                isEvaluated = useResultOnly,
                showHistorySheet = false
            )
            recalculateLive(updated)
        }
    }

    fun clearAllHistory() {
        historyRepo.clearHistory()
        _uiState.update { it.copy(historyList = emptyList()) }
    }

    fun deleteHistoryItem(item: CalculationHistoryItem) {
        val current = _uiState.value.historyList.filter { it.id != item.id }
        historyRepo.saveHistory(current)
        _uiState.update { it.copy(historyList = current) }
    }

    // Unit Converter methods
    fun setUnitCategory(category: UnitCategory) {
        val units = UnitConverterData.categories[category] ?: return
        val from = units.getOrNull(1) ?: units.first()
        val to = units.first()
        _uiState.update { state ->
            val newState = state.copy(
                unitCategory = category,
                fromUnit = from,
                toUnit = to
            )
            recalculateUnit(newState)
        }
    }

    fun setFromUnit(unit: ConversionUnit) {
        _uiState.update { state ->
            recalculateUnit(state.copy(fromUnit = unit))
        }
    }

    fun setToUnit(unit: ConversionUnit) {
        _uiState.update { state ->
            recalculateUnit(state.copy(toUnit = unit))
        }
    }

    fun swapUnits() {
        _uiState.update { state ->
            recalculateUnit(state.copy(fromUnit = state.toUnit, toUnit = state.fromUnit))
        }
    }

    fun onUnitInputChange(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' || it == '-' || it == ',' }.replace(",", ".")
        _uiState.update { state ->
            recalculateUnit(state.copy(unitInput = filtered))
        }
    }

    private fun recalculateUnit(state: CalculatorUiState): CalculatorUiState {
        val num = state.unitInput.toDoubleOrNull() ?: 0.0
        val converted = UnitConverterData.convert(
            state.unitCategory,
            state.fromUnit,
            state.toUnit,
            num
        )
        return state.copy(unitResult = UnitConverterData.formatConvertedValue(converted))
    }

    // Finance Tools methods
    fun updateDiscount(
        price: String,
        discount: Float,
        extraDiscount: Float = _uiState.value.extraDiscountPercent,
        tax: Float = _uiState.value.taxPercent
    ) {
        val numPrice = price.filter { it.isDigit() }.toDoubleOrNull() ?: 0.0
        val result = FinanceTools.calculateDiscount(
            originalPrice = numPrice,
            discountPercent = discount.toDouble(),
            extraDiscountPercent = extraDiscount.toDouble(),
            taxPercent = tax.toDouble()
        )
        _uiState.update {
            it.copy(
                discountOriginalPrice = price,
                discountPercent = discount,
                extraDiscountPercent = extraDiscount,
                taxPercent = tax,
                discountResult = result
            )
        }
    }

    fun updateSplitBill(
        bill: String,
        tip: Float,
        people: Int
    ) {
        val numBill = bill.filter { it.isDigit() }.toDoubleOrNull() ?: 0.0
        val result = FinanceTools.calculateSplitBill(
            billAmount = numBill,
            tipPercent = tip.toDouble(),
            numPeople = people
        )
        _uiState.update {
            it.copy(
                splitBillAmount = bill,
                splitTipPercent = tip,
                splitNumPeople = people,
                splitResult = result
            )
        }
    }
}
