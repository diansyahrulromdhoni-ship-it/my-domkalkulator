package com.example.calculator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.model.ConversionUnit
import com.example.calculator.model.UnitCategory
import com.example.calculator.model.UnitConverterData
import com.example.calculator.viewmodel.CalculatorUiState
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.RobotoMonoFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterScreen(
    state: CalculatorUiState,
    onCategorySelected: (UnitCategory) -> Unit,
    onFromUnitSelected: (ConversionUnit) -> Unit,
    onToUnitSelected: (ConversionUnit) -> Unit,
    onSwapUnits: () -> Unit,
    onInputValueChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryScrollState = rememberScrollState()
    val availableUnits = remember(state.unitCategory) {
        UnitConverterData.categories[state.unitCategory] ?: emptyList()
    }

    var fromExpanded by remember { mutableStateOf(false) }
    var toExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        // Categories Horizontal Scrollable Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(categoryScrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UnitCategory.values().forEach { category ->
                    val isSelected = category == state.unitCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategorySelected(category) },
                        label = { Text(category.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricCyan.copy(alpha = 0.2f),
                            selectedLabelColor = ElectricCyan
                        ),
                        modifier = Modifier.testTag("unit_category_${category.name}")
                    )
                }
            }
        }

        // Conversion Card Container
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // FROM UNIT SECTION
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "DARI",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // From Unit Dropdown Selector
                            Box {
                                OutlinedButton(
                                    onClick = { fromExpanded = true },
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text(
                                        "${state.fromUnit.name} (${state.fromUnit.symbol})",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = fromExpanded,
                                    onDismissRequest = { fromExpanded = false }
                                ) {
                                    availableUnits.forEach { unit ->
                                        DropdownMenuItem(
                                            text = { Text("${unit.name} (${unit.symbol})") },
                                            onClick = {
                                                onFromUnitSelected(unit)
                                                fromExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // From Value Input TextField
                        OutlinedTextField(
                            value = state.unitInput,
                            onValueChange = onInputValueChanged,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = RobotoMonoFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            placeholder = { Text("0") },
                            trailingIcon = {
                                if (state.unitInput.isNotEmpty()) {
                                    IconButton(onClick = { onInputValueChanged("") }) {
                                        Icon(Icons.Default.Close, contentDescription = "Hapus")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("unit_input_field")
                        )
                    }

                    // SWAP BUTTON ROW
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        )
                        IconButton(
                            onClick = onSwapUnits,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .size(40.dp)
                                .testTag("swap_units_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = "Tukar Satuan",
                                tint = ElectricCyan
                            )
                        }
                    }

                    // TO UNIT SECTION
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "KE",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // To Unit Dropdown Selector
                            Box {
                                OutlinedButton(
                                    onClick = { toExpanded = true },
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text(
                                        "${state.toUnit.name} (${state.toUnit.symbol})",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = toExpanded,
                                    onDismissRequest = { toExpanded = false }
                                ) {
                                    availableUnits.forEach { unit ->
                                        DropdownMenuItem(
                                            text = { Text("${unit.name} (${unit.symbol})") },
                                            onClick = {
                                                onToUnitSelected(unit)
                                                toExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Converted Result Display Box
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = state.unitResult,
                                    fontFamily = RobotoMonoFontFamily,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricCyan,
                                    modifier = Modifier.testTag("unit_result_display")
                                )
                                Text(
                                    text = state.toUnit.symbol,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Conversions Table for other units in this category
        item {
            Text(
                text = "Tabel Konversi Semua Satuan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        val inputNum = state.unitInput.toDoubleOrNull() ?: 0.0
        val otherUnits = availableUnits.filter { it.id != state.fromUnit.id }

        items(otherUnits) { unit ->
            val converted = UnitConverterData.convert(
                state.unitCategory,
                state.fromUnit,
                unit,
                inputNum
            )
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = unit.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = unit.symbol,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "${UnitConverterData.formatConvertedValue(converted)} ${unit.symbol}",
                        fontFamily = RobotoMonoFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
