package com.billapp.ui

import android.app.DatePickerDialog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.billapp.data.TravelExpense
import com.billapp.data.TravelExpenseCategory
import com.billapp.data.TravelTrip
import com.billapp.data.TravelSummary
import com.billapp.data.TravelTripCreatorState
import com.billapp.data.TravelTripDraft
import com.billapp.data.TravelUiState
import com.billapp.data.buildTravelCopyText
import com.billapp.data.buildTravelSettlementLines
import com.billapp.data.buildTravelSummary
import com.billapp.data.formatMoney
import com.billapp.data.parseTravelMembers
import com.billapp.data.parseMoneyToCents
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

private data class TravelMetric(
    val label: String,
    val value: String,
)

private data class TravelCategoryVisual(
    val icon: ImageVector,
    val tint: Color,
    val background: Color,
)

private data class TravelCurrencyPreset(
    val code: String,
    val rateText: String,
)

@OptIn(
    ExperimentalMaterial3Api::class,
)
@Composable
fun TravelScreen(
    viewModel: BillViewModel,
    modifier: Modifier = Modifier,
) {
    val trip by viewModel.selectedTravelTrip.collectAsStateWithLifecycle()
    val travelWorkspace by viewModel.travelWorkspace.collectAsStateWithLifecycle()
    val uiState by viewModel.travelUiState.collectAsStateWithLifecycle()
    val creatorState by viewModel.travelTripCreatorState.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current
    val swipeThreshold = with(LocalDensity.current) { 72.dp.toPx() }
    var showDeleteTripDialog by rememberSaveable { mutableStateOf(false) }
    var showTripMembersSheet by rememberSaveable { mutableStateOf(false) }
    var travelDragAmount by remember { mutableStateOf(0f) }
    val currentTrip = trip
    val currentSummary = remember(currentTrip) { currentTrip?.let { buildTravelSummary(it) } }
    val travelTrips = travelWorkspace.trips
    val currentTripIndex = currentTrip?.let { selectedTrip ->
        travelTrips.indexOfFirst { it.id == selectedTrip.id }
    } ?: -1

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(travelTrips, currentTrip?.id) {
                    if (travelTrips.size <= 1 || currentTripIndex < 0) return@pointerInput

                    detectHorizontalDragGestures(
                        onDragStart = { travelDragAmount = 0f },
                        onHorizontalDrag = { _, dragAmount ->
                            travelDragAmount += dragAmount
                        },
                        onDragCancel = { travelDragAmount = 0f },
                        onDragEnd = {
                            val targetIndex = when {
                                travelDragAmount <= -swipeThreshold -> (currentTripIndex + 1) % travelTrips.size
                                travelDragAmount >= swipeThreshold -> (currentTripIndex - 1 + travelTrips.size) % travelTrips.size
                                else -> currentTripIndex
                            }

                            if (targetIndex != currentTripIndex) {
                                viewModel.selectTravelTrip(travelTrips[targetIndex].id)
                            }
                            travelDragAmount = 0f
                        },
                    )
                },
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                if (currentTrip != null && currentSummary != null) {
                    TravelHeroCard(
                        trip = currentTrip,
                        summary = currentSummary,
                        onManageMembers = { showTripMembersSheet = true },
                        onRequestDeleteTrip = { showDeleteTripDialog = true },
                    )
                } else {
                    TravelEmptyTripCard(onCreateTrip = viewModel::openTravelTripCreator)
                }
            }
            if (currentTrip != null && currentSummary != null) {
                item {
                    TravelMetricStrip(
                        metrics = listOf(
                            TravelMetric("已花", formatMoney(currentSummary.spentCents)),
                            TravelMetric("剩余", formatMoney(currentSummary.remainingCents)),
                            TravelMetric("日均", formatMoney(currentSummary.dailyAverageCents)),
                            TravelMetric("待结算", formatMoney(currentSummary.settlementDueCents)),
                        ),
                    )
                }
                item {
                    TravelQuickActions(
                        onAddExpense = viewModel::openTravelExpenseCreator,
                        onCopySummary = {
                            clipboardManager.setText(AnnotatedString(buildTravelCopyText(currentTrip)))
                        },
                    )
                }
                if (currentTrip.isOverseas) {
                    item {
                        TravelConverterCard()
                    }
                }
                item {
                    TravelExpenseSection(
                        trip = currentTrip,
                        onToggleSettled = viewModel::toggleTravelExpenseSettled,
                    )
                }
                item {
                    TravelSettlementSection(
                        trip = currentTrip,
                        summary = currentSummary,
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(buildTravelCopyText(currentTrip)))
                        },
                    )
                }
                item {
                    TravelChecklistSection(
                        trip = currentTrip,
                        onToggleItem = viewModel::toggleTravelChecklist,
                    )
                }
                item {
                    TravelReminderSection(trip = currentTrip)
                }
            }
        }

        if (showDeleteTripDialog && currentTrip != null) {
            AlertDialog(
                onDismissRequest = { showDeleteTripDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.error,
                    )
                },
                title = { Text("删除行程") },
                text = { Text("确认删除「${currentTrip.name}」吗？删除后无法恢复。") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteTravelTrip(currentTrip.id)
                            showDeleteTripDialog = false
                        },
                    ) {
                        Text("删除")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteTripDialog = false }) {
                        Text("取消")
                    }
                },
            )
        }

        if (showTripMembersSheet && currentTrip != null) {
            ModalBottomSheet(
                onDismissRequest = { showTripMembersSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            ) {
                TravelMembersSheet(
                    trip = currentTrip,
                    onDismiss = { showTripMembersSheet = false },
                    onAddMember = viewModel::addTravelMember,
                    onRemoveMember = viewModel::removeTravelMember,
                )
            }
        }

        if (uiState.open && currentTrip != null) {
            ModalBottomSheet(
                onDismissRequest = viewModel::closeTravelExpenseCreator,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            ) {
                TravelExpenseSheet(
                    uiState = uiState,
                    trip = currentTrip,
                    onDismiss = viewModel::closeTravelExpenseCreator,
                    onTitleChange = { title ->
                        viewModel.updateTravelDraft { it.copy(title = title) }
                    },
                    onAmountChange = { amountText ->
                        viewModel.updateTravelDraft { it.copy(amountText = amountText) }
                    },
                    onNoteChange = { note ->
                        viewModel.updateTravelDraft { it.copy(note = note) }
                    },
                    onCategorySelected = viewModel::selectTravelCategory,
                    onSelectPayer = viewModel::selectTravelPayer,
                    onSave = viewModel::saveTravelExpense,
                )
            }
        }

        if (creatorState.open) {
            ModalBottomSheet(
                onDismissRequest = viewModel::closeTravelTripCreator,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            ) {
                TravelTripSheet(
                    state = creatorState,
                    onDismiss = viewModel::closeTravelTripCreator,
                    onNameChange = { value ->
                        viewModel.updateTravelTripDraft { draft -> draft.copy(name = value) }
                    },
                    onDestinationChange = { value ->
                        viewModel.updateTravelTripDraft { draft -> draft.copy(destination = value) }
                    },
                    onIsOverseasChange = viewModel::selectTravelTripOverseas,
                    onStartDateChange = { value ->
                        viewModel.updateTravelTripDraft { draft -> draft.copy(startDate = value) }
                    },
                    onEndDateChange = { value ->
                        viewModel.updateTravelTripDraft { draft -> draft.copy(endDate = value) }
                    },
                    onBudgetChange = { value ->
                        viewModel.updateTravelTripDraft { draft -> draft.copy(budgetText = value) }
                    },
                    onMembersChange = { value ->
                        viewModel.updateTravelTripDraft { draft -> draft.copy(membersText = value) }
                    },
                    onSave = viewModel::saveTravelTrip,
                )
            }
        }
    }
}

@Composable
private fun TravelHeroCard(
    trip: TravelTrip,
    summary: TravelSummary,
    onManageMembers: () -> Unit,
    onRequestDeleteTrip: () -> Unit,
) {
    val heroBrush = Brush.linearGradient(
        listOf(
            Color(0xFFE8F8FF),
            Color(0xFFDFFBF1),
            Color(0xFFF0ECFF),
        ),
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .background(heroBrush)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = trip.name,
                        style = androidx.compose.material3.MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${trip.destination} · ${com.billapp.data.travelDateRangeLabel(trip)}",
                        color = Color(0xFF38536A),
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color.White.copy(alpha = 0.55f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flight,
                            contentDescription = null,
                            tint = Color(0xFF2C6CE0),
                        )
                    }
                    IconButton(onClick = onRequestDeleteTrip) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除行程",
                            tint = androidx.compose.material3.MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TravelPill(text = "第 ${summary.dayIndex}/${summary.totalDays} 天")
                TravelPill(
                    text = "${trip.members.size} 人同行",
                    onClick = onManageMembers,
                )
                TravelPill(text = "还剩 ${summary.remainingDays} 天")
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LinearProgressIndicator(
                    progress = { summary.budgetProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "预算 ${formatMoney(summary.budgetCents)}",
                        color = Color(0xFF38536A),
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = "${(summary.budgetProgress * 100).toInt().coerceAtMost(100)}%",
                        color = Color(0xFF38536A),
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun TravelEmptyTripCard(onCreateTrip: () -> Unit) {
    TravelSectionCard(
        title = "旅行助手",
        subtitle = "先创建一个出行计划，再开始记账和结算",
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "你还没有出行计划。",
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onCreateTrip) {
                Text("创建出行计划")
            }
        }
    }
}

@Composable
private fun TravelMetricStrip(metrics: List<TravelMetric>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
        shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            metrics.forEachIndexed { index, metric ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = metric.value,
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = metric.label,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    )
                }
                if (index != metrics.lastIndex) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(34.dp)
                            .background(Color(0xFFE8E3D4)),
                    )
                }
            }
        }
    }
}

@Composable
private fun TravelQuickActions(
    onAddExpense: () -> Unit,
    onCopySummary: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Button(
            onClick = onAddExpense,
            modifier = Modifier.weight(1f),
            shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.size(4.dp))
            Text("新增支出")
        }
        Button(
            onClick = onCopySummary,
            modifier = Modifier.weight(1f),
            shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
            colors = ButtonDefaults.buttonColors(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
                contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
            ),
            border = BorderStroke(1.dp, Color(0xFFD9D4EA)),
        ) {
            Icon(Icons.Default.ContentCopy, contentDescription = null)
            Spacer(modifier = Modifier.size(4.dp))
            Text("复制总结")
        }
    }
}

@Composable
private fun TravelConverterCard() {
    var amountText by rememberSaveable { mutableStateOf("100") }
    var currencyCode by rememberSaveable { mutableStateOf("USD") }
    var rateText by rememberSaveable { mutableStateOf("7.18") }
    val presets = remember {
        listOf(
            TravelCurrencyPreset("USD", "7.18"),
            TravelCurrencyPreset("JPY", "0.049"),
            TravelCurrencyPreset("EUR", "7.82"),
        )
    }

    val amount = amountText.trim().toBigDecimalOrNull()
    val rate = rateText.trim().toBigDecimalOrNull()
    val converted = remember(amount, rate) {
        if (amount == null || rate == null) null else amount.multiply(rate).setScale(2, RoundingMode.HALF_UP)
    }

    TravelSectionCard(
        title = "汇率速算",
        subtitle = "出发前先看清每笔外币消费折合多少钱",
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("外币金额") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = rateText,
                    onValueChange = { rateText = it },
                    label = { Text("汇率") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(presets, key = { it.code }) { preset ->
                    TravelPresetChip(
                        text = preset.code,
                        selected = preset.code == currencyCode,
                        onClick = {
                            currencyCode = preset.code
                            rateText = preset.rateText
                        },
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
                        shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "1 $currencyCode ≈ ¥$rateText",
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = converted?.let { "¥${it.toPlainString()}" } ?: "—",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Text(
                    text = if (converted == null) {
                        "请先输入正确的金额和汇率"
                    } else {
                        "这个金额大约折合人民币"
                    },
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun TravelExpenseSection(
    trip: TravelTrip,
    onToggleSettled: (String) -> Unit,
) {
    val expenses = remember(trip.expenses) {
        trip.expenses.sortedWith(
            compareByDescending<TravelExpense> { it.createdAt }
                .thenByDescending { it.amountCents },
        )
    }

    TravelSectionCard(
        title = "最近支出",
        subtitle = "共 ${expenses.size} 笔，默认按时间倒序展示",
    ) {
        if (expenses.isEmpty()) {
            Text(
                text = "还没有录入旅行支出",
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@TravelSectionCard
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            expenses.forEach { expense ->
                TravelExpenseRow(
                    expense = expense,
                    onToggleSettled = { onToggleSettled(expense.id) },
                )
            }
        }
    }
}

@Composable
private fun TravelExpenseRow(
    expense: TravelExpense,
    onToggleSettled: () -> Unit,
) {
    val visual = travelCategoryVisual(expense.category)

    Surface(
        color = androidx.compose.material3.MaterialTheme.colorScheme.surface,
        shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(visual.background, androidx.compose.material3.MaterialTheme.shapes.large),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = visual.icon,
                        contentDescription = expense.category.label,
                        tint = visual.tint,
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = expense.title,
                            style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        TravelTinyChip(
                            text = if (expense.settled) "已结清" else "待结算",
                            selected = expense.settled,
                        )
                    }
                    Text(
                        text = "${expense.category.label} · 付款人 ${expense.payer}",
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    )
                    if (expense.note.isNotBlank()) {
                        Text(
                            text = expense.note,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatMoney(expense.amountCents),
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                    TextButton(onClick = onToggleSettled) {
                        Text(if (expense.settled) "撤回" else "结清")
                    }
                }
            }
        }
    }
}

@Composable
private fun TravelSettlementSection(
    trip: TravelTrip,
    summary: TravelSummary,
    onCopy: () -> Unit,
) {
    val lines = remember(trip) { buildTravelSettlementLines(trip) }

    TravelSectionCard(
        title = "AA 结算",
        subtitle = "待结算总额 ${formatMoney(summary.settlementDueCents)}",
        trailing = {
            TextButton(onClick = onCopy) {
                Icon(Icons.Default.ContentCopy, contentDescription = null)
                Spacer(modifier = Modifier.size(4.dp))
                Text("复制")
            }
        },
    ) {
        if (lines.isEmpty()) {
            Text(
                text = "当前没有待结算项目",
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@TravelSectionCard
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            lines.forEach { line ->
                TravelSettlementLineRow(line = line)
            }
        }
    }
}

@Composable
private fun TravelSettlementLineRow(line: com.billapp.data.TravelSettlementLine) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${line.debtor} → ${line.creditor}",
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
            ),
        )
        TravelTinyChip(text = formatMoney(line.amountCents), selected = false)
    }
}

@Composable
private fun TravelChecklistSection(
    trip: TravelTrip,
    onToggleItem: (String) -> Unit,
) {
    val packedCount = remember(trip.checklist) { trip.checklist.count { it.packed } }

    TravelSectionCard(
        title = "打包清单",
        subtitle = "${packedCount}/${trip.checklist.size} 已完成",
    ) {
        if (trip.checklist.isEmpty()) {
            Text(
                text = "还没有旅行清单",
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@TravelSectionCard
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            trip.checklist.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleItem(item.id) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Checkbox(
                        checked = item.packed,
                        onCheckedChange = { onToggleItem(item.id) },
                    )
                    Text(
                        text = item.label,
                        modifier = Modifier.weight(1f),
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (item.packed) FontWeight.Normal else FontWeight.SemiBold,
                        ),
                        color = if (item.packed) {
                            androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                        },
                    )
                    TravelTinyChip(text = if (item.packed) "已装" else "待装", selected = item.packed)
                }
            }
        }
    }
}

@Composable
private fun TravelReminderSection(trip: TravelTrip) {
    TravelSectionCard(
        title = "行程提醒",
        subtitle = "把关键时间点放在这里，不容易漏",
    ) {
        if (trip.reminders.isEmpty()) {
            Text(
                text = "当前没有提醒事项",
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@TravelSectionCard
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(trip.reminders, key = { it }) { reminder ->
                TravelReminderChip(text = reminder)
            }
        }
    }
}

@Composable
private fun TravelExpenseSheet(
    uiState: TravelUiState,
    trip: TravelTrip,
    onDismiss: () -> Unit,
    onTitleChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onCategorySelected: (TravelExpenseCategory) -> Unit,
    onSelectPayer: (String) -> Unit,
    onSave: () -> Unit,
) {
    val draft = uiState.draft
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(width = 52.dp, height = 6.dp)
                .background(Color(0xFFE6E4EB), androidx.compose.material3.MaterialTheme.shapes.extraLarge),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "记一笔旅行支出",
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
                Text(
                    text = "默认按全员均摊，付款人可单独切换",
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                )
            }
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }

        OutlinedTextField(
            value = draft.title,
            onValueChange = onTitleChange,
            label = { Text("支出名称") },
            placeholder = { Text("如：酒店押金、机场打车") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        TravelSectionCard(title = "类别") {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(TravelExpenseCategory.entries, key = { it.name }) { category ->
                    TravelCategoryChip(
                        category = category,
                        selected = draft.category == category,
                        onClick = { onCategorySelected(category) },
                    )
                }
            }
        }

        OutlinedTextField(
            value = draft.amountText,
            onValueChange = onAmountChange,
            label = { Text("金额（人民币）") },
            placeholder = { Text("如 128.50") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        TravelSectionCard(title = "付款人") {
            if (trip.members.isEmpty()) {
                Text(
                    text = "请先添加同行成员",
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(trip.members, key = { it }) { member ->
                        TravelPayerChip(
                            text = member,
                            selected = draft.payer == member,
                            onClick = { onSelectPayer(member) },
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = draft.note,
            onValueChange = onNoteChange,
            label = { Text("备注") },
            placeholder = { Text("如：酒店定金、景点门票") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        uiState.error?.let { error ->
            Text(
                text = error,
                color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            )
        }

        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
        ) {
            Text("确认记账")
        }
    }
}

@Composable
private fun TravelTripSheet(
    state: TravelTripCreatorState,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onDestinationChange: (String) -> Unit,
    onIsOverseasChange: (Boolean) -> Unit,
    onStartDateChange: (LocalDate) -> Unit,
    onEndDateChange: (LocalDate) -> Unit,
    onBudgetChange: (String) -> Unit,
    onMembersChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    val draft = state.draft
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val members = remember(draft.membersText) { parseTravelMembers(draft.membersText) }
    var newMemberText by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(width = 52.dp, height = 6.dp)
                .background(Color(0xFFE6E4EB), androidx.compose.material3.MaterialTheme.shapes.extraLarge),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "新建出行计划",
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }

        OutlinedTextField(
            value = draft.name,
            onValueChange = onNameChange,
            label = { Text("行程名称") },
            placeholder = { Text("如：东京自由行") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        OutlinedTextField(
            value = draft.destination,
            onValueChange = onDestinationChange,
            label = { Text("目的地") },
            placeholder = { Text("如：东京、上海、重庆") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { onIsOverseasChange(true) },
                modifier = Modifier.weight(1f),
                shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (draft.isOverseas) Color(0xFFDDF1FF) else androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Text("国外旅行")
            }
            Button(
                onClick = { onIsOverseasChange(false) },
                modifier = Modifier.weight(1f),
                shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!draft.isOverseas) Color(0xFFDDF1FF) else androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Text("国内旅行")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            onStartDateChange(LocalDate.of(year, month + 1, dayOfMonth))
                        },
                        draft.startDate.year,
                        draft.startDate.monthValue - 1,
                        draft.startDate.dayOfMonth,
                    ).show()
                },
                modifier = Modifier.weight(1f),
                shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
                colors = ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Text("开始 ${draft.startDate}")
            }
            Button(
                onClick = {
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            onEndDateChange(LocalDate.of(year, month + 1, dayOfMonth))
                        },
                        draft.endDate.year,
                        draft.endDate.monthValue - 1,
                        draft.endDate.dayOfMonth,
                    ).show()
                },
                modifier = Modifier.weight(1f),
                shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
                colors = ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Text("结束 ${draft.endDate}")
            }
        }

        OutlinedTextField(
            value = draft.budgetText,
            onValueChange = onBudgetChange,
            label = { Text("预算（人民币）") },
            placeholder = { Text("如：8000") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        TravelSectionCard(
            title = "本次旅行人员",
            subtitle = "可自行添加，付款人会从这里选择",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = newMemberText,
                        onValueChange = { newMemberText = it },
                        label = { Text("添加成员") },
                        placeholder = { Text("如：张三") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    Button(
                        onClick = {
                            val candidate = newMemberText.trim()
                            if (candidate.isNotBlank()) {
                                val nextMembers = (members + candidate).distinct()
                                onMembersChange(nextMembers.joinToString("、"))
                                newMemberText = ""
                            }
                        },
                        shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
                    ) {
                        Text("添加")
                    }
                }

                if (members.isEmpty()) {
                    Text(
                        text = "至少添加 1 位同行人",
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(members, key = { it }) { member ->
                            TravelMemberChip(
                                text = member,
                                onClick = {
                                    val nextMembers = members.filterNot { it == member }
                                    onMembersChange(nextMembers.joinToString("、"))
                                },
                            )
                        }
                    }
                }
            }
        }

        OutlinedTextField(
            value = draft.membersText,
            onValueChange = { onMembersChange(it) },
            label = { Text("成员文本") },
            placeholder = { Text("支持逗号、顿号、空格分隔") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            minLines = 2,
        )

        state.error?.let { error ->
            Text(
                text = error,
                color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            )
        }

        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
        ) {
            Text("创建行程")
        }
    }
}

@Composable
private fun TravelMembersSheet(
    trip: TravelTrip,
    onDismiss: () -> Unit,
    onAddMember: (String) -> Unit,
    onRemoveMember: (String) -> Unit,
) {
    var newMemberText by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(width = 52.dp, height = 6.dp)
                .background(Color(0xFFE6E4EB), androidx.compose.material3.MaterialTheme.shapes.extraLarge),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "临时同行人员",
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
                Text(
                    text = "新增成员会参与后续新记账；已存在支出的参与人保持不变",
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                )
            }
            TextButton(onClick = onDismiss) {
                Text("完成")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = newMemberText,
                onValueChange = { newMemberText = it },
                label = { Text("添加成员") },
                placeholder = { Text("如：张三") },
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
            Button(
                onClick = {
                    val candidate = newMemberText.trim()
                    if (candidate.isNotBlank()) {
                        onAddMember(candidate)
                        newMemberText = ""
                    }
                },
                shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
            ) {
                Text("添加")
            }
        }

        if (trip.members.isEmpty()) {
            Text(
                text = "至少保留 1 位同行人",
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(trip.members, key = { it }) { member ->
                    TravelMemberChip(
                        text = if (trip.members.size == 1) member else "$member ×",
                        onClick = {
                            if (trip.members.size > 1) {
                                onRemoveMember(member)
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun TravelSectionCard(
    title: String,
    subtitle: String = "",
    trailing: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
        shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = title,
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                    if (subtitle.isNotBlank()) {
                        Text(
                            text = subtitle,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                trailing?.invoke()
            }
            content()
        }
    }
}

@Composable
private fun TravelCategoryChip(
    category: TravelExpenseCategory,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val visual = travelCategoryVisual(category)
    Surface(
        onClick = onClick,
        color = if (selected) visual.background else androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
        shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
        border = BorderStroke(1.dp, if (selected) visual.tint.copy(alpha = 0.55f) else Color.Transparent),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(visual.background, androidx.compose.material3.MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = visual.icon,
                    contentDescription = category.label,
                    tint = visual.tint,
                    modifier = Modifier.size(18.dp),
                )
            }
            Text(
                text = category.label,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun TravelPayerChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = if (selected) Color(0xFFDDF1FF) else androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
        shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
        border = BorderStroke(1.dp, if (selected) Color(0xFF81BDF0) else Color.Transparent),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun TravelPill(
    text: String,
    onClick: (() -> Unit)? = null,
) {
    Surface(
        color = Color.White.copy(alpha = 0.58f),
        shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
    ) {
        Text(
            text = text,
            modifier = Modifier
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun TravelTinyChip(
    text: String,
    selected: Boolean,
) {
    Surface(
        color = if (selected) Color(0xFFDDF1FF) else androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
        shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun TravelReminderChip(text: String) {
    Surface(
        color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
        shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun TravelPresetChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = if (selected) Color(0xFFDDF1FF) else androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
        shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
        border = BorderStroke(1.dp, if (selected) Color(0xFF81BDF0) else Color.Transparent),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            maxLines = 1,
        )
    }
}

@Composable
private fun TravelMemberChip(
    text: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = Color(0xFFF4F2F7),
        shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
        border = BorderStroke(1.dp, Color(0xFFE1DDEA)),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}

private fun travelCategoryVisual(category: TravelExpenseCategory): TravelCategoryVisual {
    return when (category) {
        TravelExpenseCategory.Transport -> TravelCategoryVisual(
            icon = Icons.Default.Flight,
            tint = Color(0xFF2C6CE0),
            background = Color(0xFFDFF0FF),
        )

        TravelExpenseCategory.Stay -> TravelCategoryVisual(
            icon = Icons.Default.Home,
            tint = Color(0xFF7A57E8),
            background = Color(0xFFEDE7FF),
        )

        TravelExpenseCategory.Food -> TravelCategoryVisual(
            icon = Icons.Default.Fastfood,
            tint = Color(0xFFE57A2E),
            background = Color(0xFFFFE8D9),
        )

        TravelExpenseCategory.Tickets -> TravelCategoryVisual(
            icon = Icons.AutoMirrored.Filled.ReceiptLong,
            tint = Color(0xFF2BA06A),
            background = Color(0xFFE3F7EC),
        )

        TravelExpenseCategory.Shopping -> TravelCategoryVisual(
            icon = Icons.Default.LocalMall,
            tint = Color(0xFFE04F78),
            background = Color(0xFFFFE4ED),
        )

        TravelExpenseCategory.Other -> TravelCategoryVisual(
            icon = Icons.Default.Payments,
            tint = Color(0xFF8A7EE8),
            background = Color(0xFFF0EBFF),
        )
    }
}
