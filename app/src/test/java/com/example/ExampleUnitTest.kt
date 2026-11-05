package com.example

import com.example.calculator.engine.CalculatorFormatter
import com.example.calculator.engine.ExpressionEvaluator
import com.example.calculator.model.FinanceTools
import com.example.calculator.model.UnitCategory
import com.example.calculator.model.UnitConverterData
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testBasicArithmetic() {
        val evaluator = ExpressionEvaluator(isRadian = false)

        val res1 = evaluator.evaluate("12 + 25")
        assertTrue(res1 is ExpressionEvaluator.EvalResult.Success)
        assertEquals("37", (res1 as ExpressionEvaluator.EvalResult.Success).formatted)

        val res2 = evaluator.evaluate("100 - 45.5")
        assertTrue(res2 is ExpressionEvaluator.EvalResult.Success)
        assertEquals("54.5", (res2 as ExpressionEvaluator.EvalResult.Success).formatted)

        val res3 = evaluator.evaluate("8 × 12.5")
        assertTrue(res3 is ExpressionEvaluator.EvalResult.Success)
        assertEquals("100", (res3 as ExpressionEvaluator.EvalResult.Success).formatted)

        val res4 = evaluator.evaluate("15 ÷ 4")
        assertTrue(res4 is ExpressionEvaluator.EvalResult.Success)
        assertEquals("3.75", (res4 as ExpressionEvaluator.EvalResult.Success).formatted)
    }

    @Test
    fun testUserScreenshotCase() {
        val evaluator = ExpressionEvaluator(isRadian = false)
        val res = evaluator.evaluate("1500 * 12")
        assertTrue(res is ExpressionEvaluator.EvalResult.Success)
        assertEquals("18000", (res as ExpressionEvaluator.EvalResult.Success).formatted)

        // Formatting tests matching screenshot
        assertEquals("18 000", CalculatorFormatter.formatNumber("18000"))
        assertEquals("1 500×12", CalculatorFormatter.formatExpression("1500*12"))
        assertEquals("1 500,5", CalculatorFormatter.formatNumber("1500.5"))
    }

    @Test
    fun testPrecedenceAndParentheses() {
        val evaluator = ExpressionEvaluator(isRadian = false)

        val res1 = evaluator.evaluate("2 + 3 × 4")
        assertTrue(res1 is ExpressionEvaluator.EvalResult.Success)
        assertEquals("14", (res1 as ExpressionEvaluator.EvalResult.Success).formatted)

        val res2 = evaluator.evaluate("(2 + 3) × 4")
        assertTrue(res2 is ExpressionEvaluator.EvalResult.Success)
        assertEquals("20", (res2 as ExpressionEvaluator.EvalResult.Success).formatted)
    }

    @Test
    fun testScientificFunctions() {
        val degEvaluator = ExpressionEvaluator(isRadian = false)

        val sin90 = degEvaluator.evaluate("sin(90)")
        assertTrue(sin90 is ExpressionEvaluator.EvalResult.Success)
        assertEquals("1", (sin90 as ExpressionEvaluator.EvalResult.Success).formatted)

        val cos0 = degEvaluator.evaluate("cos(0)")
        assertTrue(cos0 is ExpressionEvaluator.EvalResult.Success)
        assertEquals("1", (cos0 as ExpressionEvaluator.EvalResult.Success).formatted)

        val sqrt16 = degEvaluator.evaluate("sqrt(16)")
        assertTrue(sqrt16 is ExpressionEvaluator.EvalResult.Success)
        assertEquals("4", (sqrt16 as ExpressionEvaluator.EvalResult.Success).formatted)

        val log100 = degEvaluator.evaluate("log(100)")
        assertTrue(log100 is ExpressionEvaluator.EvalResult.Success)
        assertEquals("2", (log100 as ExpressionEvaluator.EvalResult.Success).formatted)

        val fact5 = degEvaluator.evaluate("5!")
        assertTrue(fact5 is ExpressionEvaluator.EvalResult.Success)
        assertEquals("120", (fact5 as ExpressionEvaluator.EvalResult.Success).formatted)
    }

    @Test
    fun testDivisionByZero() {
        val evaluator = ExpressionEvaluator(isRadian = false)
        val res = evaluator.evaluate("10 ÷ 0")
        assertTrue(res is ExpressionEvaluator.EvalResult.Error)
    }

    @Test
    fun testUnitConversion() {
        val lengthUnits = UnitConverterData.categories[UnitCategory.LENGTH]!!
        val m = lengthUnits.first { it.id == "m" }
        val km = lengthUnits.first { it.id == "km" }

        val converted = UnitConverterData.convert(UnitCategory.LENGTH, m, km, 1500.0)
        assertEquals(1.5, converted, 0.0001)

        val tempUnits = UnitConverterData.categories[UnitCategory.TEMPERATURE]!!
        val c = tempUnits.first { it.id == "c" }
        val f = tempUnits.first { it.id == "f" }

        val fVal = UnitConverterData.convert(UnitCategory.TEMPERATURE, c, f, 100.0)
        assertEquals(212.0, fVal, 0.0001)
    }

    @Test
    fun testFinanceTools() {
        val discount = FinanceTools.calculateDiscount(
            originalPrice = 200000.0,
            discountPercent = 25.0,
            extraDiscountPercent = 0.0,
            taxPercent = 0.0
        )
        assertEquals(150000.0, discount.finalPrice, 0.01)
        assertEquals(50000.0, discount.totalSavings, 0.01)

        val split = FinanceTools.calculateSplitBill(
            billAmount = 400000.0,
            tipPercent = 10.0,
            numPeople = 4
        )
        assertEquals(440000.0, split.grandTotal, 0.01)
        assertEquals(110000.0, split.perPersonTotal, 0.01)
    }
}
