package com.example.calculator.model

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs

enum class UnitCategory(val displayName: String, val iconName: String) {
    LENGTH("Panjang", "Straighten"),
    WEIGHT("Massa / Berat", "Scale"),
    TEMPERATURE("Suhu", "Thermostat"),
    AREA("Luas", "SquareFoot"),
    VOLUME("Volume", "WaterDrop"),
    DATA("Data Digital", "Memory"),
    TIME("Waktu", "Schedule")
}

data class ConversionUnit(
    val id: String,
    val name: String,
    val symbol: String,
    val toBaseFactor: Double = 1.0, // Multiplier to convert to base unit
    val customToBase: ((Double) -> Double)? = null,
    val customFromBase: ((Double) -> Double)? = null
)

object UnitConverterData {
    val categories: Map<UnitCategory, List<ConversionUnit>> = mapOf(
        UnitCategory.LENGTH to listOf(
            ConversionUnit("km", "Kilometer", "km", 1000.0),
            ConversionUnit("m", "Meter", "m", 1.0), // Base
            ConversionUnit("cm", "Sentimeter", "cm", 0.01),
            ConversionUnit("mm", "Milimeter", "mm", 0.001),
            ConversionUnit("mi", "Mil (Mile)", "mi", 1609.344),
            ConversionUnit("yd", "Yard", "yd", 0.9144),
            ConversionUnit("ft", "Kaki (Feet)", "ft", 0.3048),
            ConversionUnit("in", "Inci (Inch)", "in", 0.0254)
        ),
        UnitCategory.WEIGHT to listOf(
            ConversionUnit("t", "Ton Metrik", "t", 1000.0),
            ConversionUnit("kg", "Kilogram", "kg", 1.0), // Base
            ConversionUnit("g", "Gram", "g", 0.001),
            ConversionUnit("mg", "Miligram", "mg", 0.000001),
            ConversionUnit("lb", "Pon (Pound)", "lb", 0.45359237),
            ConversionUnit("oz", "Ons (Ounce)", "oz", 0.028349523)
        ),
        UnitCategory.TEMPERATURE to listOf(
            ConversionUnit(
                id = "c",
                name = "Celsius",
                symbol = "°C",
                customToBase = { it },
                customFromBase = { it }
            ), // Base = Celsius
            ConversionUnit(
                id = "f",
                name = "Fahrenheit",
                symbol = "°F",
                customToBase = { (it - 32.0) * 5.0 / 9.0 },
                customFromBase = { it * 9.0 / 5.0 + 32.0 }
            ),
            ConversionUnit(
                id = "k",
                name = "Kelvin",
                symbol = "K",
                customToBase = { it - 273.15 },
                customFromBase = { it + 273.15 }
            ),
            ConversionUnit(
                id = "r",
                name = "Reamur",
                symbol = "°R",
                customToBase = { it * 5.0 / 4.0 },
                customFromBase = { it * 4.0 / 5.0 }
            )
        ),
        UnitCategory.AREA to listOf(
            ConversionUnit("km2", "Kilometer Persegi", "km²", 1_000_000.0),
            ConversionUnit("ha", "Hektar", "ha", 10_000.0),
            ConversionUnit("m2", "Meter Persegi", "m²", 1.0), // Base
            ConversionUnit("cm2", "Sentimeter Persegi", "cm²", 0.0001),
            ConversionUnit("ac", "Acre", "ac", 4046.856),
            ConversionUnit("ft2", "Kaki Persegi", "ft²", 0.092903)
        ),
        UnitCategory.VOLUME to listOf(
            ConversionUnit("m3", "Meter Kubik", "m³", 1000.0),
            ConversionUnit("l", "Liter", "L", 1.0), // Base
            ConversionUnit("ml", "Mililiter", "mL", 0.001),
            ConversionUnit("gal", "Galon (US)", "gal", 3.78541),
            ConversionUnit("cup", "Cangkir (Cup)", "cup", 0.236588)
        ),
        UnitCategory.DATA to listOf(
            ConversionUnit("tb", "Terabyte", "TB", 1024.0 * 1024.0 * 1024.0 * 1024.0),
            ConversionUnit("gb", "Gigabyte", "GB", 1024.0 * 1024.0 * 1024.0),
            ConversionUnit("mb", "Megabyte", "MB", 1024.0 * 1024.0),
            ConversionUnit("kb", "Kilobyte", "KB", 1024.0),
            ConversionUnit("b", "Byte", "B", 1.0), // Base
            ConversionUnit("bit", "Bit", "bit", 0.125)
        ),
        UnitCategory.TIME to listOf(
            ConversionUnit("yr", "Tahun (365h)", "thn", 31536000.0),
            ConversionUnit("wk", "Minggu", "mgg", 604800.0),
            ConversionUnit("d", "Hari", "hari", 86400.0),
            ConversionUnit("h", "Jam", "jam", 3600.0),
            ConversionUnit("min", "Menit", "menit", 60.0),
            ConversionUnit("s", "Detik", "s", 1.0) // Base
        )
    )

    fun convert(
        category: UnitCategory,
        fromUnit: ConversionUnit,
        toUnit: ConversionUnit,
        value: Double
    ): Double {
        if (fromUnit.id == toUnit.id) return value

        // Convert fromUnit -> Base
        val baseValue = if (fromUnit.customToBase != null) {
            fromUnit.customToBase.invoke(value)
        } else {
            value * fromUnit.toBaseFactor
        }

        // Convert Base -> toUnit
        return if (toUnit.customFromBase != null) {
            toUnit.customFromBase.invoke(baseValue)
        } else {
            baseValue / toUnit.toBaseFactor
        }
    }

    fun formatConvertedValue(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "-"
        if (abs(value) >= 1e12 || (abs(value) > 0 && abs(value) < 1e-6)) {
            return String.format(java.util.Locale.US, "%.5e", value).replace("e+", "e")
        }
        val bd = try {
            BigDecimal.valueOf(value).setScale(6, RoundingMode.HALF_UP).stripTrailingZeros()
        } catch (e: Exception) {
            BigDecimal.valueOf(value)
        }
        val plainStr = bd.toPlainString()
        return if (plainStr == "-0") "0" else plainStr
    }
}
