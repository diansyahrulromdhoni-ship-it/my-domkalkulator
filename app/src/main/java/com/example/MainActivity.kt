package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.calculator.ui.components.DisplayScreen
import com.example.calculator.ui.components.HistoryModalSheet
import com.example.calculator.ui.components.Keypad
import com.example.calculator.ui.components.ToolsModalSheet
import com.example.calculator.viewmodel.CalculatorViewModel
import com.example.ui.theme.DarkOledBackground
import com.example.ui.theme.KalkulatorTheme

class MainActivity : ComponentActivity() {

    private val viewModel: CalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            KalkulatorTheme {
                CalculatorScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CalculatorScreen(viewModel: CalculatorViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle back button for modal sheets
    BackHandler(enabled = state.showToolsSheet || state.showHistorySheet || state.isScientificExpanded) {
        if (state.showToolsSheet) {
            viewModel.toggleToolsSheet(false)
        } else if (state.showHistorySheet) {
            viewModel.toggleHistorySheet(false)
        } else if (state.isScientificExpanded) {
            viewModel.toggleScientific()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkOledBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Header & Display Screen
            DisplayScreen(
                state = state,
                onOpenHistory = { viewModel.toggleHistorySheet(true) },
                onToggleScientific = { viewModel.toggleScientific() },
                onSwipeBackspace = { viewModel.backspace() },
                modifier = Modifier.weight(1f)
            )

            // Bottom Section: Exact Circular 4x5 Keypad
            Keypad(
                state = state,
                onKeyClick = { key -> viewModel.onKeyClick(key) },
                onOpenTools = { viewModel.toggleToolsSheet(true) },
                onToggleAngleMode = { viewModel.toggleAngleMode() },
                modifier = Modifier.wrapContentHeight()
            )
        }

        // Bottom Sheet for Tools (Unit Converter & Finance Calculator)
        if (state.showToolsSheet) {
            ToolsModalSheet(
                state = state,
                onDismiss = { viewModel.toggleToolsSheet(false) },
                onSelectSubTab = { viewModel.setToolsSubTab(it) },
                onCategorySelected = { viewModel.setUnitCategory(it) },
                onFromUnitSelected = { viewModel.setFromUnit(it) },
                onToUnitSelected = { viewModel.setToUnit(it) },
                onSwapUnits = { viewModel.swapUnits() },
                onUnitInputChange = { viewModel.onUnitInputChange(it) },
                onUpdateDiscount = { price, discount, extra, tax ->
                    viewModel.updateDiscount(price, discount, extra, tax)
                },
                onUpdateSplitBill = { bill, tip, people ->
                    viewModel.updateSplitBill(bill, tip, people)
                }
            )
        }

        // Bottom Sheet for History
        if (state.showHistorySheet) {
            HistoryModalSheet(
                historyList = state.historyList,
                onDismiss = { viewModel.toggleHistorySheet(false) },
                onSelectHistoryItem = { item, useResultOnly ->
                    viewModel.useHistoryItem(item, useResultOnly)
                },
                onDeleteItem = { item -> viewModel.deleteHistoryItem(item) },
                onClearAll = { viewModel.clearAllHistory() }
            )
        }
    }
}
