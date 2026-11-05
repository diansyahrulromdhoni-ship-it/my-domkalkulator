package com.example.calculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.calculator.model.ConversionUnit
import com.example.calculator.model.UnitCategory
import com.example.calculator.ui.screens.DiscountCalculatorView
import com.example.calculator.ui.screens.SplitBillCalculatorView
import com.example.calculator.ui.screens.UnitConverterScreen
import com.example.calculator.viewmodel.CalculatorUiState
import com.example.ui.theme.DarkOledSurface
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonBlueContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsModalSheet(
    state: CalculatorUiState,
    onDismiss: () -> Unit,
    onSelectSubTab: (Int) -> Unit,
    onCategorySelected: (UnitCategory) -> Unit,
    onFromUnitSelected: (ConversionUnit) -> Unit,
    onToUnitSelected: (ConversionUnit) -> Unit,
    onSwapUnits: () -> Unit,
    onUnitInputChange: (String) -> Unit,
    onUpdateDiscount: (String, Float, Float, Float) -> Unit,
    onUpdateSplitBill: (String, Float, Int) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkOledSurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = NeonBlue.copy(alpha = 0.5f)) },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 16.dp)
        ) {
            // Segmented Tab Row in Neon Blue
            TabRow(
                selectedTabIndex = state.toolsSubTab,
                containerColor = Color.Transparent,
                contentColor = NeonBlue,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[state.toolsSubTab]),
                        color = NeonBlue
                    )
                },
                divider = {}
            ) {
                Tab(
                    selected = state.toolsSubTab == 0,
                    onClick = { onSelectSubTab(0) },
                    text = {
                        Text(
                            text = "Konversi Satuan",
                            fontWeight = if (state.toolsSubTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (state.toolsSubTab == 0) NeonBlue else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                Tab(
                    selected = state.toolsSubTab == 1,
                    onClick = { onSelectSubTab(1) },
                    text = {
                        Text(
                            text = "Diskon",
                            fontWeight = if (state.toolsSubTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (state.toolsSubTab == 1) NeonBlue else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                Tab(
                    selected = state.toolsSubTab == 2,
                    onClick = { onSelectSubTab(2) },
                    text = {
                        Text(
                            text = "Bagi Tagihan",
                            fontWeight = if (state.toolsSubTab == 2) FontWeight.Bold else FontWeight.Normal,
                            color = if (state.toolsSubTab == 2) NeonBlue else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (state.toolsSubTab) {
                0 -> {
                    UnitConverterScreen(
                        state = state,
                        onCategorySelected = onCategorySelected,
                        onFromUnitSelected = onFromUnitSelected,
                        onToUnitSelected = onToUnitSelected,
                        onSwapUnits = onSwapUnits,
                        onInputValueChanged = onUnitInputChange
                    )
                }
                1 -> {
                    DiscountCalculatorView(
                        state = state,
                        onUpdate = onUpdateDiscount
                    )
                }
                2 -> {
                    SplitBillCalculatorView(
                        state = state,
                        onUpdate = onUpdateSplitBill
                    )
                }
            }
        }
    }
}
