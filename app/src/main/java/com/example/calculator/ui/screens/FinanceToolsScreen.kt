package com.example.calculator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Savings
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
import com.example.calculator.model.FinanceTools
import com.example.calculator.viewmodel.CalculatorUiState
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.RobotoMonoFontFamily

@Composable
fun FinanceToolsScreen(
    state: CalculatorUiState,
    onUpdateDiscount: (price: String, discount: Float, extraDiscount: Float, tax: Float) -> Unit,
    onUpdateSplitBill: (bill: String, tip: Float, people: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFinanceTab by remember { mutableStateOf(0) } // 0: Diskon, 1: Split Bill

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Segmented Tab Switcher
        TabRow(
            selectedTabIndex = selectedFinanceTab,
            containerColor = Color.Transparent,
            contentColor = ElectricCyan,
            divider = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Tab(
                selected = selectedFinanceTab == 0,
                onClick = { selectedFinanceTab = 0 },
                text = { Text("Kalkulator Diskon", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedFinanceTab == 1,
                onClick = { selectedFinanceTab = 1 },
                text = { Text("Bagi Tagihan & Tip", fontWeight = FontWeight.Bold) }
            )
        }

        if (selectedFinanceTab == 0) {
            DiscountCalculatorView(
                state = state,
                onUpdate = onUpdateDiscount
            )
        } else {
            SplitBillCalculatorView(
                state = state,
                onUpdate = onUpdateSplitBill
            )
        }
    }
}

@Composable
fun DiscountCalculatorView(
    state: CalculatorUiState,
    onUpdate: (price: String, discount: Float, extraDiscount: Float, tax: Float) -> Unit
) {
    var priceInput by remember { mutableStateOf(state.discountOriginalPrice) }
    var discountPercent by remember { mutableFloatStateOf(state.discountPercent) }
    var extraDiscountPercent by remember { mutableFloatStateOf(state.extraDiscountPercent) }
    var taxPercent by remember { mutableFloatStateOf(state.taxPercent) }

    fun notifyChange() {
        onUpdate(priceInput, discountPercent, extraDiscountPercent, taxPercent)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        // Result Card (Hero)
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
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Harga Akhir yang Dibayar",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = state.discountResult.formattedFinalPrice,
                        fontFamily = RobotoMonoFontFamily,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan,
                        modifier = Modifier.testTag("discount_final_price")
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Total Hemat:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = state.discountResult.formattedSavings,
                            fontFamily = RobotoMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = NeonGreen
                        )
                    }

                    if (taxPercent > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Pajak (${taxPercent.toInt()}%):",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = FinanceTools.formatCurrency(state.discountResult.taxAmount),
                                fontFamily = RobotoMonoFontFamily,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Input Harga Asli
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Harga Asli (Rp)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = {
                            priceInput = it.filter { c -> c.isDigit() }
                            notifyChange()
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        prefix = { Text("Rp ") },
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = RobotoMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("discount_price_input")
                    )

                    // Quick percentage chips for discount
                    Text(
                        text = "Persentase Diskon: ${discountPercent.toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(10f, 20f, 30f, 50f, 70f).forEach { pct ->
                            FilterChip(
                                selected = discountPercent == pct,
                                onClick = {
                                    discountPercent = pct
                                    notifyChange()
                                },
                                label = { Text("${pct.toInt()}%") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElectricCyan.copy(alpha = 0.2f),
                                    selectedLabelColor = ElectricCyan
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Slider(
                        value = discountPercent,
                        onValueChange = {
                            discountPercent = it
                            notifyChange()
                        },
                        valueRange = 0f..100f,
                        steps = 99
                    )

                    // Diskon Tambahan
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Diskon Tambahan (Promo): ${extraDiscountPercent.toInt()}%",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Slider(
                        value = extraDiscountPercent,
                        onValueChange = {
                            extraDiscountPercent = it
                            notifyChange()
                        },
                        valueRange = 0f..50f,
                        steps = 49
                    )

                    // Pajak / PPN
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pajak / PPN: ${taxPercent.toInt()}%",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(0f, 11f).forEach { tax ->
                                SuggestionChip(
                                    onClick = {
                                        taxPercent = tax
                                        notifyChange()
                                    },
                                    label = { Text(if (tax == 0f) "Tanpa Pajak" else "PPN 11%") }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SplitBillCalculatorView(
    state: CalculatorUiState,
    onUpdate: (bill: String, tip: Float, people: Int) -> Unit
) {
    var billInput by remember { mutableStateOf(state.splitBillAmount) }
    var tipPercent by remember { mutableFloatStateOf(state.splitTipPercent) }
    var peopleCount by remember { mutableIntStateOf(state.splitNumPeople) }

    fun notifyChange() {
        onUpdate(billInput, tipPercent, peopleCount)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        // Result Card (Hero)
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
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Bayar Per Orang ($peopleCount orang)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = state.splitResult.formattedPerPersonTotal,
                        fontFamily = RobotoMonoFontFamily,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan,
                        modifier = Modifier.testTag("split_per_person_total")
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total Tagihan Keseluruhan:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = state.splitResult.formattedGrandTotal,
                            fontFamily = RobotoMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (tipPercent > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Tip per Orang (${tipPercent.toInt()}%):",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = FinanceTools.formatCurrency(state.splitResult.perPersonTip),
                                fontFamily = RobotoMonoFontFamily,
                                style = MaterialTheme.typography.bodySmall,
                                color = NeonAmber
                            )
                        }
                    }
                }
            }
        }

        // Tagihan & Orang Inputs
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Total Tagihan (Rp)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = billInput,
                        onValueChange = {
                            billInput = it.filter { c -> c.isDigit() }
                            notifyChange()
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        prefix = { Text("Rp ") },
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = RobotoMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("split_bill_input")
                    )

                    // Jumlah Orang Stepper
                    Text(
                        text = "Jumlah Orang",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalIconButton(
                            onClick = {
                                if (peopleCount > 1) {
                                    peopleCount--
                                    notifyChange()
                                }
                            },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Kurang orang")
                        }

                        Text(
                            text = "$peopleCount Orang",
                            fontFamily = RobotoMonoFontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        FilledTonalIconButton(
                            onClick = {
                                peopleCount++
                                notifyChange()
                            },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Tambah orang")
                        }
                    }

                    // Tip Chips
                    Text(
                        text = "Tip Pelayan / Layanan: ${tipPercent.toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(0f, 5f, 10f, 15f, 20f).forEach { tip ->
                            FilterChip(
                                selected = tipPercent == tip,
                                onClick = {
                                    tipPercent = tip
                                    notifyChange()
                                },
                                label = { Text("${tip.toInt()}%") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElectricCyan.copy(alpha = 0.2f),
                                    selectedLabelColor = ElectricCyan
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
