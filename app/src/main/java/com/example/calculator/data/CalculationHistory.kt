package com.example.calculator.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CalculationHistoryItem(
    val id: Long = System.currentTimeMillis(),
    val expression: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun formattedTime(): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
        val sdfDate = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

        return when {
            diff < 60_000 -> "Baru saja"
            diff < 3_600_000 -> "${diff / 60_000} menit lalu"
            diff < 86_400_000 -> "Hari ini, ${sdfTime.format(Date(timestamp))}"
            else -> sdfDate.format(Date(timestamp))
        }
    }
}

class HistoryRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("kalkulator_history_prefs", Context.MODE_PRIVATE)

    fun loadHistory(): List<CalculationHistoryItem> {
        val rawJson = prefs.getString("history_items", null) ?: return emptyList()
        val list = mutableListOf<CalculationHistoryItem>()
        try {
            val array = JSONArray(rawJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    CalculationHistoryItem(
                        id = obj.optLong("id", System.currentTimeMillis()),
                        expression = obj.optString("expression", ""),
                        result = obj.optString("result", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun saveHistory(items: List<CalculationHistoryItem>) {
        val array = JSONArray()
        // Save max 50 recent items
        items.take(50).forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("expression", item.expression)
                put("result", item.result)
                put("timestamp", item.timestamp)
            }
            array.put(obj)
        }
        prefs.edit().putString("history_items", array.toString()).apply()
    }

    fun clearHistory() {
        prefs.edit().remove("history_items").apply()
    }
}
