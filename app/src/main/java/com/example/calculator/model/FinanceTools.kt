package com.example.calculator.model

import java.text.NumberFormat
import java.util.Locale

object FinanceTools {
    fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        format.maximumFractionDigits = 0
        return format.format(amount)
    }

    data class DiscountResult(
        val finalPrice: Double,
        val totalSavings: Double,
        val taxAmount: Double,
        val formattedFinalPrice: String,
        val formattedSavings: String
    )

    fun calculateDiscount(
        originalPrice: Double,
        discountPercent: Double,
        extraDiscountPercent: Double = 0.0,
        taxPercent: Double = 0.0
    ): DiscountResult {
        if (originalPrice <= 0) {
            return DiscountResult(0.0, 0.0, 0.0, formatCurrency(0.0), formatCurrency(0.0))
        }

        // First discount
        val firstDiscountAmount = originalPrice * (discountPercent.coerceIn(0.0, 100.0) / 100.0)
        val afterFirstDiscount = originalPrice - firstDiscountAmount

        // Second discount on remaining
        val extraDiscountAmount = afterFirstDiscount * (extraDiscountPercent.coerceIn(0.0, 100.0) / 100.0)
        val afterDiscounts = afterFirstDiscount - extraDiscountAmount

        // Tax
        val taxAmount = afterDiscounts * (taxPercent.coerceIn(0.0, 100.0) / 100.0)
        val finalPrice = afterDiscounts + taxAmount
        val totalSavings = originalPrice - afterDiscounts

        return DiscountResult(
            finalPrice = finalPrice,
            totalSavings = totalSavings,
            taxAmount = taxAmount,
            formattedFinalPrice = formatCurrency(finalPrice),
            formattedSavings = formatCurrency(totalSavings)
        )
    }

    data class SplitBillResult(
        val grandTotal: Double,
        val totalTip: Double,
        val perPersonTotal: Double,
        val perPersonTip: Double,
        val formattedPerPersonTotal: String,
        val formattedGrandTotal: String
    )

    fun calculateSplitBill(
        billAmount: Double,
        tipPercent: Double,
        numPeople: Int
    ): SplitBillResult {
        val people = numPeople.coerceAtLeast(1)
        if (billAmount <= 0) {
            return SplitBillResult(0.0, 0.0, 0.0, 0.0, formatCurrency(0.0), formatCurrency(0.0))
        }

        val totalTip = billAmount * (tipPercent.coerceAtLeast(0.0) / 100.0)
        val grandTotal = billAmount + totalTip
        val perPersonTotal = grandTotal / people
        val perPersonTip = totalTip / people

        return SplitBillResult(
            grandTotal = grandTotal,
            totalTip = totalTip,
            perPersonTotal = perPersonTotal,
            perPersonTip = perPersonTip,
            formattedPerPersonTotal = formatCurrency(perPersonTotal),
            formattedGrandTotal = formatCurrency(grandTotal)
        )
    }
}
