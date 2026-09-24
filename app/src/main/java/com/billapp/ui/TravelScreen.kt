package com.billapp.ui

import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.widget.Toast

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.billapp.R
import com.billapp.data.TravelExpense
import com.billapp.data.TravelExpenseCategory
import com.billapp.data.TravelTrip
import com.billapp.data.TravelSummary
import com.billapp.data.TravelTripCreatorState
import com.billapp.data.TravelTripDraft
import com.billapp.data.TravelUiState
import com.billapp.data.appendAmountInput
import com.billapp.data.buildTravelCategoryStats
import com.billapp.data.buildTravelCopyText
import com.billapp.data.buildTravelFullBillXlsx
import com.billapp.data.buildTravelSettlementLines
import com.billapp.data.buildTravelSummary
import com.billapp.data.formatDraftAmount
import com.billapp.data.formatMoney
import com.billapp.data.parseTravelMembers
import com.billapp.data.parseMoneyToCents
import com.billapp.data.removeLastAmountInput
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

private data class TravelMetric(
    val label: String,
    val value: String,
    val supporting: String = "",
    val highlight: Boolean = false,
    val warning: Boolean = false,
)

private data class TravelCategoryVisual(
    val icon: ImageVector,
    val tint: Color,
    val background: Color,
)

private data class TravelTitleInputSpec(
    val label: String,
    val placeholder: String,
    val suggestions: List<String>,
)

private data class TravelCurrencyPreset(
    val code: String,
    val rateText: String,
)

private data class TravelDestinationTheme(
    val title: String,
    val imageRes: Int,
    val skyTop: Color,
    val skyBottom: Color,
    val accent: Color,
    val deep: Color,
    val warm: Color,
)

private val TravelSurfaceWarm = Color(0xFFFFFCF6)
private val TravelPanelSoft = Color(0xFFF8F5FF)
private val TravelDivider = Color(0xFFE7E1F1)
private val TravelPrimaryGold = Color(0xFFF0B400)
private val TravelPrimaryGoldDark = Color(0xFF9A6F00)
private val TravelMutedText = Color(0xFF8C8799)
private val TravelHeroText = Color(0xFF365066)
private val TravelDangerSoft = Color(0xFFFFEEF0)
private val TravelSkyTop = Color(0xFFFFFEF8)
private val TravelSkyBottom = Color(0xFFF4F0FF)
private val TravelRouteBlue = Color(0xFF2C7BE5)
private val TravelFreshGreen = Color(0xFF42B883)
private val TravelInk = Color(0xFF172126)
private val TravelTeal = Color(0xFF0F7B78)
private val TravelTerracotta = Color(0xFFC95E32)

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
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val swipeThreshold = with(LocalDensity.current) { 40.dp.toPx() }
    var showDeleteTripDialog by rememberSaveable { mutableStateOf(false) }
    var showTripMembersSheet by rememberSaveable { mutableStateOf(false) }
    var showTravelCategoryDetailsSheet by rememberSaveable { mutableStateOf(false) }
    var showTravelSettlementDetailsSheet by rememberSaveable { mutableStateOf(false) }
    var travelBillActionTripId by rememberSaveable { mutableStateOf<String?>(null) }
    var tripMemberSheetText by rememberSaveable { mutableStateOf("") }
    var tripCreatorMemberText by rememberSaveable { mutableStateOf("") }
    var travelDragAmount by remember { mutableStateOf(0f) }
    var travelTransitionDirection by remember { mutableStateOf(1) }
    val selectedTravelTrip = trip
    val travelTrips = travelWorkspace.trips
    val travelBillActionTrip = travelBillActionTripId?.let { tripId ->
        travelTrips.firstOrNull { it.id == tripId }
    }
    val currentTripIndex = selectedTravelTrip?.let { selectedTrip ->
        travelTrips.indexOfFirst { it.id == selectedTrip.id }
    } ?: -1

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = selectedTravelTrip?.id,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(travelTrips, selectedTravelTrip?.id) {
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
                            val targetDirection = when {
                                travelDragAmount <= -swipeThreshold -> 1
                                travelDragAmount >= swipeThreshold -> -1
                                else -> travelTransitionDirection
                            }

                            if (targetIndex != currentTripIndex) {
                                travelTransitionDirection = targetDirection
                                viewModel.selectTravelTrip(travelTrips[targetIndex].id)
                            }
                            travelDragAmount = 0f
                        },
                    )
                },
            transitionSpec = {
                val direction = travelTransitionDirection.coerceIn(-1, 1).takeIf { it != 0 } ?: 1
                (
                    slideInHorizontally { fullWidth -> direction * fullWidth } + fadeIn()
                ).togetherWith(
                    slideOutHorizontally { fullWidth -> -direction * fullWidth } + fadeOut()
                ).using(SizeTransform(clip = false))
            },
            label = "TravelTripPager",
        ) { pageTripId ->
            val pageTrip = travelTrips.firstOrNull { it.id == pageTripId }
            val pageTripIndex = travelTrips.indexOfFirst { it.id == pageTripId }
            val pageSummary = remember(pageTrip) { pageTrip?.let { buildTravelSummary(it) } }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                TravelSkyTop,
                                TravelSkyBottom,
                            ),
                        ),
                    ),
                contentPadding = PaddingValues(start = 16.dp, top = 18.dp, end = 16.dp, bottom = 116.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                item {
                    if (pageTrip != null && pageSummary != null) {
                        TravelHeroCard(
                            trip = pageTrip,
                            summary = pageSummary,
                            onEditTrip = viewModel::openTravelTripEditor,
                            onManageMembers = { showTripMembersSheet = true },
                            onRequestDeleteTrip = { showDeleteTripDialog = true },
                        )
                    } else {
                        TravelEmptyTripCard(onCreateTrip = viewModel::openTravelTripCreator)
                    }
                }
                if (travelTrips.size > 1 && pageTripIndex >= 0) {
                    item {
                        TravelTripPageIndicator(
                            pageCount = travelTrips.size,
                            currentPage = pageTripIndex,
                        )
                    }
                }
                if (pageTrip != null && pageSummary != null) {
                    val currentTrip = pageTrip
                    val currentSummary = pageSummary
                    item {
                        TravelBudgetDashboard(
                            summary = currentSummary,
                            onAddExpense = viewModel::openTravelExpenseCreator,
                            onCopySummary = {
                                travelBillActionTripId = currentTrip.id
                            },
                        )
                    }
                    if (currentTrip.isOverseas) {
                        item {
                            TravelConverterCard()
                        }
                    }
                    item {
                        TravelInsightGrid(
                            trip = currentTrip,
                            summary = currentSummary,
                            onCopySettlement = {
                                travelBillActionTripId = currentTrip.id
                            },
                            onOpenCategoryDetails = {
                                showTravelCategoryDetailsSheet = true
                            },
                            onOpenSettlementDetails = {
                                showTravelSettlementDetailsSheet = true
                            },
                        )
                    }
                    item {
                        TravelExpenseSection(
                            trip = currentTrip,
                            onEditExpense = viewModel::openTravelExpenseEditor,
                            onDeleteExpense = viewModel::deleteTravelExpense,
                            onToggleSettled = viewModel::toggleTravelExpenseSettled,
                        )
                    }
                    item {
                        TravelChecklistSection(
                            trip = currentTrip,
                            onToggleItem = viewModel::toggleTravelChecklist,
                            onAddItem = viewModel::addTravelChecklistItem,
                            onDeleteItem = viewModel::deleteTravelChecklistItem,
                        )
                    }
                    item {
                        TravelReminderSection(
                            trip = currentTrip,
                            onAddReminder = viewModel::addTravelReminder,
                            onDeleteReminder = viewModel::deleteTravelReminder,
                        )
                    }
                }
            }
        }

        if (showDeleteTripDialog && selectedTravelTrip != null) {
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
                text = { Text("确认删除「${selectedTravelTrip.name}」吗？删除后无法恢复。") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteTravelTrip(selectedTravelTrip.id)
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

        if (travelBillActionTrip != null) {
            AlertDialog(
                onDismissRequest = { travelBillActionTripId = null },
                title = { Text("AA 账单") },
                text = { Text("选择要导出的账单形式。") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val exported = shareTravelFullBill(context, travelBillActionTrip)
                            travelBillActionTripId = null
                            Toast.makeText(
                                context,
                                if (exported) "已生成完整账单" else "未找到可打开 Excel 的应用",
                                Toast.LENGTH_SHORT,
                            ).show()
                        },
                    ) {
                        Text("导出完整账单")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(buildTravelCopyText(travelBillActionTrip)))
                            travelBillActionTripId = null
                            Toast.makeText(context, "已复制简洁账单", Toast.LENGTH_SHORT).show()
                        },
                    ) {
                        Text("复制简洁账单")
                    }
                },
            )
        }

        if (showTripMembersSheet && selectedTravelTrip != null) {
            ModalBottomSheet(
                onDismissRequest = { showTripMembersSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            ) {
                TravelMembersSheet(
                    trip = selectedTravelTrip,
                    newMemberText = tripMemberSheetText,
                    onNewMemberTextChange = { tripMemberSheetText = it },
                    onDismiss = { showTripMembersSheet = false },
                    onAddMember = viewModel::addTravelMember,
                    onRemoveMember = viewModel::removeTravelMember,
                )
            }
        }

        if (showTravelCategoryDetailsSheet && selectedTravelTrip != null) {
            ModalBottomSheet(
                onDismissRequest = { showTravelCategoryDetailsSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            ) {
                TravelCategoryDetailsSheet(
                    trip = selectedTravelTrip,
                    onDismiss = { showTravelCategoryDetailsSheet = false },
                )
            }
        }

        if (showTravelSettlementDetailsSheet && selectedTravelTrip != null) {
            ModalBottomSheet(
                onDismissRequest = { showTravelSettlementDetailsSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            ) {
                TravelSettlementDetailsSheet(
                    trip = selectedTravelTrip,
                    summary = buildTravelSummary(selectedTravelTrip),
                    onDismiss = { showTravelSettlementDetailsSheet = false },
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(buildTravelCopyText(selectedTravelTrip)))
                        showTravelSettlementDetailsSheet = false
                        Toast.makeText(context, "已复制简洁账单", Toast.LENGTH_SHORT).show()
                    },
                )
            }
        }

        if (uiState.open && selectedTravelTrip != null) {
            ModalBottomSheet(
                onDismissRequest = viewModel::closeTravelExpenseCreator,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            ) {
                TravelExpenseSheet(
                    uiState = uiState,
                    trip = selectedTravelTrip,
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
                    onToggleParticipant = viewModel::toggleTravelParticipant,
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
                    newMemberText = tripCreatorMemberText,
                    onNewMemberTextChange = { tripCreatorMemberText = it },
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
private fun TravelTripPageIndicator(
    pageCount: Int,
    currentPage: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(7.dp)
                    .background(
                        color = if (index == currentPage) {
                            androidx.compose.material3.MaterialTheme.colorScheme.primary
                        } else {
                            Color(0xFFD2D8E2)
                        },
                        shape = CircleShape,
                    ),
            )
        }
    }
}

@Composable
private fun TravelHeroCard(
    trip: TravelTrip,
    summary: TravelSummary,
    onEditTrip: () -> Unit,
    onManageMembers: () -> Unit,
    onRequestDeleteTrip: () -> Unit,
) {
    val theme = remember(trip.destination, trip.name) {
        travelDestinationTheme("${trip.destination} ${trip.name}")
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = androidx.compose.material3.MaterialTheme.shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEditTrip),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(282.dp)
                .background(Brush.verticalGradient(listOf(theme.skyTop, theme.skyBottom))),
        ) {
            Image(
                painter = painterResource(id = theme.imageRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.08f),
                                Color.Black.copy(alpha = 0.12f),
                                Color.Black.copy(alpha = 0.62f),
                            ),
                        ),
                    ),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.28f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    TravelHeroTag(
                        icon = Icons.Default.LocationOn,
                        text = trip.destination.ifBlank { theme.title },
                        tint = theme.deep,
                    )

                    IconButton(onClick = onRequestDeleteTrip) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除行程",
                            tint = Color.White.copy(alpha = 0.82f),
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = trip.name,
                            color = Color.White,
                            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.88f),
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = com.billapp.data.travelDateRangeLabel(trip),
                                color = Color.White.copy(alpha = 0.92f),
                                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TravelHeroTag(
                            icon = Icons.Default.CalendarMonth,
                            text = "第 ${summary.dayIndex}/${summary.totalDays} 天",
                            tint = theme.accent,
                        )
                        TravelHeroTag(
                            icon = Icons.Default.Groups,
                            text = "${trip.members.size} 人同行",
                            tint = TravelTeal,
                            onClick = onManageMembers,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TravelHeroTag(
    icon: ImageVector,
    text: String,
    tint: Color,
    onClick: (() -> Unit)? = null,
) {
    Surface(
        color = Color.White.copy(alpha = 0.86f),
        shape = androidx.compose.material3.MaterialTheme.shapes.medium,
        modifier = if (onClick == null) Modifier else Modifier.clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = text,
                color = TravelInk,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun TravelBudgetDashboard(
    summary: TravelSummary,
    onAddExpense: () -> Unit,
    onCopySummary: () -> Unit,
) {
    val hasBudget = summary.budgetCents > 0L
    val isOverBudget = hasBudget && summary.remainingCents < 0L

    Card(
        colors = CardDefaults.cardColors(containerColor = TravelSurfaceWarm),
        shape = androidx.compose.material3.MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, Color(0xFFE8DDC7)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TravelBudgetMetric(
                    label = "已花",
                    value = formatMoney(summary.spentCents),
                    modifier = Modifier.weight(1f),
                )
                TravelBudgetMetric(
                    label = "预算",
                    value = if (hasBudget) formatMoney(summary.budgetCents) else "未设置",
                    modifier = Modifier.weight(1f),
                )
                TravelBudgetMetric(
                    label = "剩余",
                    value = travelRemainingValue(summary),
                    valueColor = if (isOverBudget) TravelTerracotta else TravelTeal,
                    modifier = Modifier.weight(1f),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LinearProgressIndicator(
                    progress = { summary.budgetProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = if (isOverBudget) TravelTerracotta else TravelPrimaryGold,
                    trackColor = Color(0xFFE8E0D0),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${summary.expenseCount} 笔支出 · 日均 ${formatMoney(summary.dailyAverageCents)}",
                        color = TravelMutedText,
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = when {
                            !hasBudget -> "先记录支出"
                            isOverBudget -> "预算超出"
                            else -> "${(summary.budgetProgress * 100).toInt().coerceAtMost(100)}%"
                        },
                        color = if (isOverBudget) TravelTerracotta else TravelTeal,
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                }
            }

            TravelQuickActions(
                onAddExpense = onAddExpense,
                onCopySummary = onCopySummary,
            )
        }
    }
}

@Composable
private fun TravelBudgetMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = TravelInk,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            color = TravelMutedText,
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            maxLines = 1,
        )
        Text(
            text = value,
            color = valueColor,
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
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
        colors = CardDefaults.cardColors(containerColor = TravelSurfaceWarm),
        shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            metrics.chunked(2).forEach { rowMetrics ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    rowMetrics.forEach { metric ->
                        TravelMetricCell(
                            metric = metric,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (rowMetrics.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun TravelMetricCell(
    metric: TravelMetric,
    modifier: Modifier = Modifier,
) {
    val valueColor = when {
        metric.warning -> androidx.compose.material3.MaterialTheme.colorScheme.error
        metric.highlight -> androidx.compose.material3.MaterialTheme.colorScheme.onSurface
        else -> TravelHeroText
    }

    Surface(
        color = if (metric.highlight || metric.warning) Color.White.copy(alpha = 0.74f) else TravelPanelSoft,
        shape = androidx.compose.material3.MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, if (metric.warning) TravelDangerSoft else TravelDivider.copy(alpha = 0.55f)),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = metric.label,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = metric.value,
                color = valueColor,
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (metric.supporting.isNotBlank()) {
                Text(
                    text = metric.supporting,
                    color = TravelMutedText,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
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
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = androidx.compose.material3.MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(
                containerColor = TravelPrimaryGold,
                contentColor = Color(0xFF3D2A00),
            ),
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text("新增支出")
        }
        Button(
            onClick = onCopySummary,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = androidx.compose.material3.MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(
                containerColor = TravelSurfaceWarm,
                contentColor = TravelTeal,
            ),
            border = BorderStroke(1.dp, TravelTeal.copy(alpha = 0.62f)),
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text("导出账单")
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
    onEditExpense: (String) -> Unit,
    onDeleteExpense: (String) -> Unit,
    onToggleSettled: (String) -> Unit,
) {
    var expanded by rememberSaveable(trip.id) { mutableStateOf(false) }
    val expenses = remember(trip.expenses) {
        trip.expenses.sortedWith(
            compareByDescending<TravelExpense> { it.createdAt }
                .thenByDescending { it.amountCents },
        )
    }
    val collapsedExpenseCount = 3
    val canExpand = expenses.size > collapsedExpenseCount
    val visibleExpenses = remember(expenses, expanded) {
        if (expanded || !canExpand) expenses else expenses.take(collapsedExpenseCount)
    }

    TravelSectionCard(
        title = "最近支出",
        subtitle = if (canExpand && !expanded) {
            "共 ${expenses.size} 笔，已显示最新 $collapsedExpenseCount 笔"
        } else {
            "共 ${expenses.size} 笔，按时间倒序展示"
        },
    ) {
        if (expenses.isEmpty()) {
            Text(
                text = "还没有录入旅行支出",
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@TravelSectionCard
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            visibleExpenses.forEach { expense ->
                TravelExpenseRow(
                    expense = expense,
                    onEdit = { onEditExpense(expense.id) },
                    onDelete = { onDeleteExpense(expense.id) },
                    onToggleSettled = { onToggleSettled(expense.id) },
                )
            }
            if (canExpand) {
                TextButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (expanded) "收起支出" else "展开全部 ${expenses.size} 笔支出")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TravelExpenseRow(
    expense: TravelExpense,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleSettled: () -> Unit,
) {
    val visual = travelCategoryVisual(expense.category)

    Surface(
        color = TravelPanelSoft,
        shape = androidx.compose.material3.MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, TravelDivider.copy(alpha = 0.55f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(visual.background, androidx.compose.material3.MaterialTheme.shapes.large),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = visual.icon,
                    contentDescription = expense.category.label,
                    tint = visual.tint,
                    modifier = Modifier.size(24.dp),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = expense.title,
                            modifier = Modifier.fillMaxWidth(),
                            style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TravelTinyChip(
                                text = if (expense.settled) "已结清" else "待结算",
                                selected = expense.settled,
                            )
                            Text(
                                text = expense.category.label,
                                color = TravelMutedText,
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                            )
                        }
                    }

                    Text(
                        text = formatMoney(expense.amountCents),
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Text(
                    text = "付款人 ${expense.payer} · ${travelParticipantsLabel(expense.members)}",
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (expense.note.isNotBlank()) {
                    Text(
                        text = expense.note,
                        color = TravelMutedText,
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                TravelExpenseActions(
                    settled = expense.settled,
                    onEdit = onEdit,
                    onToggleSettled = onToggleSettled,
                    onDelete = onDelete,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TravelExpenseActions(
    settled: Boolean,
    onEdit: () -> Unit,
    onToggleSettled: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        TextButton(
            onClick = onEdit,
            modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 40.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Text("编辑", maxLines = 1)
        }
        TextButton(
            onClick = onToggleSettled,
            modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 40.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Text(if (settled) "撤回结清" else "标为结清", maxLines = 1)
        }
        TextButton(
            onClick = onDelete,
            modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 40.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "删除支出",
                tint = androidx.compose.material3.MaterialTheme.colorScheme.error.copy(alpha = 0.72f),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun TravelInsightGrid(
    trip: TravelTrip,
    summary: TravelSummary,
    onCopySettlement: () -> Unit,
    onOpenCategoryDetails: () -> Unit,
    onOpenSettlementDetails: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TravelCategoryCompactPanel(
            trip = trip,
            onOpenDetails = onOpenCategoryDetails,
            modifier = Modifier.weight(1f),
        )
        TravelSettlementCompactPanel(
            trip = trip,
            summary = summary,
            onCopy = onCopySettlement,
            onOpenDetails = onOpenSettlementDetails,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun TravelCategoryCompactPanel(
    trip: TravelTrip,
    onOpenDetails: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val allStats = remember(trip.expenses) { buildTravelCategoryStats(trip) }
    val stats = remember(allStats) { allStats.take(3) }

    Card(
        colors = CardDefaults.cardColors(containerColor = TravelSurfaceWarm),
        shape = androidx.compose.material3.MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, Color(0xFFE8DDC7)),
        modifier = modifier.clickable(
            enabled = allStats.isNotEmpty(),
            onClick = onOpenDetails,
        ),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "支出分类",
                    modifier = Modifier.weight(1f),
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (allStats.size > stats.size) {
                    Text(
                        text = "${stats.size}/${allStats.size}",
                        color = TravelMutedText,
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                    )
                }
            }
            if (stats.isEmpty()) {
                Text(
                    text = "暂无支出",
                    color = TravelMutedText,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                )
                return@Column
            }
            stats.forEach { stat ->
                val visual = travelCategoryVisual(stat.category)
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stat.category.label,
                            modifier = Modifier.weight(0.46f),
                            color = TravelInk,
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = formatMoney(stat.amountCents),
                            modifier = Modifier.weight(0.54f),
                            color = TravelInk,
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    LinearProgressIndicator(
                        progress = { stat.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = visual.tint,
                        trackColor = Color(0xFFE8E0D0),
                    )
                }
            }
            if (allStats.size > stats.size) {
                Text(
                    text = "点击查看全部分类",
                    modifier = Modifier.fillMaxWidth(),
                    color = TravelTeal,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    textAlign = TextAlign.End,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun TravelSettlementCompactPanel(
    trip: TravelTrip,
    summary: TravelSummary,
    onCopy: () -> Unit,
    onOpenDetails: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val allLines = remember(trip) { buildTravelSettlementLines(trip) }
    val lines = remember(allLines) { allLines.take(2) }

    Card(
        colors = CardDefaults.cardColors(containerColor = TravelSurfaceWarm),
        shape = androidx.compose.material3.MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, Color(0xFFE8DDC7)),
        modifier = modifier.clickable(
            enabled = allLines.isNotEmpty(),
            onClick = onOpenDetails,
        ),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "AA 待结算",
                    modifier = Modifier.weight(1f),
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                TextButton(
                    onClick = onCopy,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                ) {
                    Text("复制", maxLines = 1)
                }
            }
            Text(
                text = formatMoney(summary.settlementDueCents),
                color = if (summary.settlementDueCents > 0L) TravelTerracotta else TravelTeal,
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (lines.isEmpty()) {
                Text(
                    text = "当前没有待结算项目",
                    color = TravelMutedText,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                )
                return@Column
            }
            lines.forEach { line ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${line.debtor} → ${line.creditor}",
                        modifier = Modifier.weight(0.48f),
                        color = TravelInk,
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = formatMoney(line.amountCents),
                        modifier = Modifier.weight(0.52f),
                        color = TravelTerracotta,
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (allLines.size > lines.size) {
                Text(
                    text = "点击查看全部 ${allLines.size} 条",
                    modifier = Modifier.fillMaxWidth(),
                    color = TravelTeal,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    textAlign = TextAlign.End,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun TravelSheetHeader(
    title: String,
    subtitle: String,
    onDismiss: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = title,
                color = TravelInk,
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                color = TravelMutedText,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "关闭",
            )
        }
    }
}

@Composable
private fun TravelCategoryDetailsSheet(
    trip: TravelTrip,
    onDismiss: () -> Unit,
) {
    val stats = remember(trip.expenses) { buildTravelCategoryStats(trip) }

    Column(
        modifier = Modifier
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, bottom = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        TravelSheetHeader(
            title = "支出分类详情",
            subtitle = "${stats.size} 个分类",
            onDismiss = onDismiss,
        )
        if (stats.isEmpty()) {
            Text(
                text = "暂无支出",
                color = TravelMutedText,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            )
            return@Column
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            stats.forEach { stat ->
                val visual = travelCategoryVisual(stat.category)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(visual.background, androidx.compose.material3.MaterialTheme.shapes.medium),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = visual.icon,
                                    contentDescription = stat.category.label,
                                    tint = visual.tint,
                                    modifier = Modifier.size(19.dp),
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = stat.category.label,
                                    color = TravelInk,
                                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = "${stat.expenseCount} 笔",
                                    color = TravelMutedText,
                                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                        Text(
                            text = formatMoney(stat.amountCents),
                            color = TravelInk,
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            maxLines = 1,
                        )
                    }
                    LinearProgressIndicator(
                        progress = { stat.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp),
                        color = visual.tint,
                        trackColor = Color(0xFFE8E0D0),
                    )
                }
            }
        }
    }
}

@Composable
private fun TravelSettlementDetailsSheet(
    trip: TravelTrip,
    summary: TravelSummary,
    onDismiss: () -> Unit,
    onCopy: () -> Unit,
) {
    val lines = remember(trip) { buildTravelSettlementLines(trip) }

    Column(
        modifier = Modifier
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, bottom = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        TravelSheetHeader(
            title = "AA 待结算详情",
            subtitle = "待结算总额 ${formatMoney(summary.settlementDueCents)}",
            onDismiss = onDismiss,
        )
        TextButton(
            onClick = onCopy,
            modifier = Modifier.align(Alignment.End),
        ) {
            Icon(Icons.Default.ContentCopy, contentDescription = null)
            Spacer(modifier = Modifier.size(4.dp))
            Text("复制")
        }
        if (lines.isEmpty()) {
            Text(
                text = "当前没有待结算项目",
                color = TravelMutedText,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            )
            return@Column
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            lines.forEach { line ->
                TravelSettlementLineRow(line = line)
            }
        }
    }
}

@Composable
private fun TravelCategoryStatsSection(trip: TravelTrip) {
    val stats = remember(trip.expenses) { buildTravelCategoryStats(trip) }

    TravelSectionCard(
        title = "类别统计",
        subtitle = "按支出类别汇总，方便旅行复盘",
    ) {
        if (stats.isEmpty()) {
            Text(
                text = "暂无可统计的支出，点上方“新增支出”开始记录。",
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@TravelSectionCard
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            stats.forEach { stat ->
                val visual = travelCategoryVisual(stat.category)
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(visual.background, androidx.compose.material3.MaterialTheme.shapes.medium),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = visual.icon,
                                    contentDescription = stat.category.label,
                                    tint = visual.tint,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = stat.category.label,
                                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                                )
                                Text(
                                    text = "${stat.expenseCount} 笔",
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                        Text(
                            text = formatMoney(stat.amountCents),
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                        )
                    }
                    LinearProgressIndicator(
                        progress = { stat.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp),
                            color = visual.tint,
                            trackColor = TravelDivider.copy(alpha = 0.62f),
                    )
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
    onAddItem: (String) -> Unit,
    onDeleteItem: (String) -> Unit,
) {
    val packedCount = remember(trip.checklist) { trip.checklist.count { it.packed } }
    var newItemText by rememberSaveable { mutableStateOf("") }

    TravelSectionCard(
        title = "打包清单",
        subtitle = "${packedCount}/${trip.checklist.size} 已完成",
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = newItemText,
                onValueChange = { newItemText = it },
                label = { Text("新增清单") },
                placeholder = { Text("如：雨伞、泳衣") },
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
            Button(
                onClick = {
                    val candidate = newItemText.trim()
                    if (candidate.isNotBlank()) {
                        onAddItem(candidate)
                        newItemText = ""
                    }
                },
                modifier = Modifier.height(52.dp),
                shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
            ) {
                Text("添加")
            }
        }

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
                            textDecoration = if (item.packed) TextDecoration.LineThrough else TextDecoration.None,
                        ),
                        color = if (item.packed) {
                            androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                        },
                    )
                    TravelTinyChip(text = if (item.packed) "已装" else "待装", selected = item.packed)
                    TextButton(onClick = { onDeleteItem(item.id) }) {
                        Text(
                            text = "删除",
                            color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TravelReminderSection(
    trip: TravelTrip,
    onAddReminder: (String) -> Unit,
    onDeleteReminder: (String) -> Unit,
) {
    var newReminderText by rememberSaveable { mutableStateOf("") }

    TravelSectionCard(
        title = "行程提醒",
        subtitle = "把关键时间点放在这里，不容易漏",
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = newReminderText,
                onValueChange = { newReminderText = it },
                label = { Text("新增提醒") },
                placeholder = { Text("如：提前 30 分钟出门") },
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
            Button(
                onClick = {
                    val candidate = newReminderText.trim()
                    if (candidate.isNotBlank()) {
                        onAddReminder(candidate)
                        newReminderText = ""
                    }
                },
                modifier = Modifier.height(52.dp),
                shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
            ) {
                Text("添加")
            }
        }

        if (trip.reminders.isEmpty()) {
            Text(
                text = "当前没有提醒事项",
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@TravelSectionCard
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            trip.reminders.forEach { reminder ->
                TravelReminderChip(
                    text = "$reminder ×",
                    onClick = { onDeleteReminder(reminder) },
                )
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
    onToggleParticipant: (String) -> Unit,
    onSave: () -> Unit,
) {
    val draft = uiState.draft
    val scrollState = rememberScrollState()
    val isEditing = uiState.editingExpenseId != null
    val titleInputSpec = remember(draft.category) { travelTitleInputSpec(draft.category) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        TravelSheetHandle()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (isEditing) "编辑旅行支出" else "记一笔旅行支出",
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
                Text(
                    text = "可按实际参与人分摊，付款人需在参与人中",
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
            label = { Text(titleInputSpec.label) },
            placeholder = { Text(titleInputSpec.placeholder) },
            trailingIcon = {
                if (draft.title.isNotBlank()) {
                    IconButton(onClick = { onTitleChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "清空名称",
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(titleInputSpec.suggestions, key = { it }) { suggestion ->
                TravelPresetChip(
                    text = suggestion,
                    selected = draft.title == suggestion,
                    onClick = {
                        val currentTitle = draft.title.trim()
                        val titleFromCurrentSuggestions = currentTitle in titleInputSpec.suggestions
                        when {
                            currentTitle.isBlank() || titleFromCurrentSuggestions -> onTitleChange(suggestion)
                            suggestion !in currentTitle -> {
                                onTitleChange("$currentTitle$suggestion")
                            }
                        }
                    },
                )
            }
        }

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

        TravelAmountInputCard(amountText = draft.amountText)
        AmountKeyboard(
            onKeyPress = { token -> onAmountChange(appendAmountInput(draft.amountText, token)) },
            onClear = { onAmountChange("") },
            onDelete = { onAmountChange(removeLastAmountInput(draft.amountText)) },
            onConfirm = onSave,
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

        TravelSectionCard(
            title = "参与人",
            subtitle = "只给实际参与的人均摊，付款人必选",
        ) {
            if (trip.members.isEmpty()) {
                Text(
                    text = "请先添加同行成员",
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(trip.members, key = { it }) { member ->
                        TravelParticipantChip(
                            text = member,
                            selected = member in draft.participantMembers,
                            required = member == draft.payer,
                            onClick = { if (member != draft.payer) onToggleParticipant(member) },
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
            Text(if (isEditing) "保存修改" else "确认记账")
        }
    }
}

@Composable
private fun TravelAmountInputCard(amountText: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
        shape = androidx.compose.material3.MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "金额（人民币）",
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "¥${formatDraftAmount(amountText)}",
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                style = androidx.compose.material3.MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                ),
            )
        }
    }
}

@Composable
private fun TravelTripSheet(
    state: TravelTripCreatorState,
    newMemberText: String,
    onNewMemberTextChange: (String) -> Unit,
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
    val isEditing = state.editingTripId != null
    val members = remember(draft.membersText) { parseTravelMembers(draft.membersText) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        TravelSheetHandle()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (isEditing) "编辑出行计划" else "新建出行计划",
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

        if (!isEditing) {
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
                            onValueChange = onNewMemberTextChange,
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
                                    onNewMemberTextChange("")
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
        }

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
            Text(if (isEditing) "保存修改" else "创建行程")
        }
    }
}

@Composable
private fun TravelMembersSheet(
    trip: TravelTrip,
    newMemberText: String,
    onNewMemberTextChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onAddMember: (String) -> Unit,
    onRemoveMember: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        TravelSheetHandle()

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
                onValueChange = onNewMemberTextChange,
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
                        onNewMemberTextChange("")
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
        colors = CardDefaults.cardColors(containerColor = TravelSurfaceWarm),
        shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.65f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 4.dp, height = 20.dp)
                                .background(TravelPrimaryGold, androidx.compose.material3.MaterialTheme.shapes.extraLarge),
                        )
                        Text(
                            text = title,
                            style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                        )
                    }
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
private fun TravelSheetHandle() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = 52.dp, height = 6.dp)
                .background(TravelDivider, androidx.compose.material3.MaterialTheme.shapes.extraLarge),
        )
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
private fun TravelParticipantChip(
    text: String,
    selected: Boolean,
    required: Boolean = false,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = !required,
        color = if (selected) Color(0xFFE3F7EC) else androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
        shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
        border = BorderStroke(1.dp, if (selected) Color(0xFF2BA06A) else Color.Transparent),
    ) {
        Text(
            text = when {
                required -> "$text ✓ 必选"
                selected -> "$text ✓"
                else -> text
            },
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
private fun TravelReminderChip(
    text: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
        shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
        modifier = Modifier.widthIn(max = 260.dp),
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

private fun travelDestinationTheme(rawDestination: String): TravelDestinationTheme {
    val text = rawDestination.trim()
    return when {
        text.containsAny("苏州", "杭州", "南京", "无锡", "扬州", "乌镇", "西塘", "绍兴", "周庄", "同里", "南浔", "嘉兴", "湖州") -> TravelDestinationTheme(
            title = "江南",
            imageRes = R.drawable.travel_hero_jiangnan,
            skyTop = Color(0xFFEAF7EF),
            skyBottom = Color(0xFFBFDCD2),
            accent = Color(0xFFC69A30),
            deep = Color(0xFF24584E),
            warm = Color(0xFFFFD98A),
        )

        text.containsAny("三亚", "厦门", "青岛", "珠海", "北海", "舟山", "威海", "大连", "海口", "深圳", "香港", "澳门", "烟台", "惠州", "汕头", "湛江", "陵水", "万宁", "日照") -> TravelDestinationTheme(
            title = "海滨",
            imageRes = R.drawable.travel_hero_coast,
            skyTop = Color(0xFFE6F7F4),
            skyBottom = Color(0xFFA8D8CE),
            accent = Color(0xFFF0B03A),
            deep = Color(0xFF0D5F68),
            warm = Color(0xFFFFC16F),
        )

        text.containsAny("重庆", "成都", "贵阳", "张家界", "武夷山", "黄山", "恩施", "峨眉山", "乐山", "都江堰", "青城山", "梵净山") -> TravelDestinationTheme(
            title = "山城",
            imageRes = R.drawable.travel_hero_mountain_city,
            skyTop = Color(0xFFFFF1DE),
            skyBottom = Color(0xFFD7A581),
            accent = Color(0xFFC95E32),
            deep = Color(0xFF593625),
            warm = Color(0xFFFFC46F),
        )

        text.containsAny("北京", "西安", "洛阳", "开封", "大同", "平遥", "敦煌", "泉州", "天水", "喀什", "吐鲁番", "银川", "承德", "曲阜") -> TravelDestinationTheme(
            title = "古都",
            imageRes = R.drawable.travel_hero_ancient_city,
            skyTop = Color(0xFFFFF0D8),
            skyBottom = Color(0xFFD8AA74),
            accent = Color(0xFFB45C2A),
            deep = Color(0xFF5B3424),
            warm = Color(0xFFFFD27A),
        )

        text.containsAny("川西", "甘南", "阿坝", "甘孜", "理塘", "色达", "稻城", "亚丁", "四姑娘山", "毕棚沟", "九寨沟", "黄龙", "若尔盖", "红原", "玛曲", "郎木寺", "扎尕那", "夏河", "拉卜楞寺", "塔公", "新都桥", "康定", "丹巴", "松潘") -> TravelDestinationTheme(
            title = "高原",
            imageRes = R.drawable.travel_hero_west_sichuan,
            skyTop = Color(0xFFEAF6FF),
            skyBottom = Color(0xFFC8D9B8),
            accent = Color(0xFFB88732),
            deep = Color(0xFF2F5A4C),
            warm = Color(0xFFFFD58E),
        )

        text.containsAny("拉萨", "西宁", "香格里拉", "大理", "丽江", "昆明", "桂林", "阳朔", "泸沽湖", "洱海", "玉龙雪山", "西双版纳", "腾冲", "普洱", "林芝", "纳木错") -> TravelDestinationTheme(
            title = "山湖",
            imageRes = R.drawable.travel_hero_highland,
            skyTop = Color(0xFFEAF6FF),
            skyBottom = Color(0xFFC8D9B8),
            accent = Color(0xFFB88732),
            deep = Color(0xFF2F5A4C),
            warm = Color(0xFFFFD58E),
        )

        text.containsAny("哈尔滨", "长春", "沈阳", "雪乡", "漠河", "阿勒泰", "长白山", "延吉", "吉林", "伊春", "牡丹江", "呼伦贝尔", "满洲里") -> TravelDestinationTheme(
            title = "雪境",
            imageRes = R.drawable.travel_hero_snow,
            skyTop = Color(0xFFE8F4FF),
            skyBottom = Color(0xFFBFD1E3),
            accent = Color(0xFF4B87A9),
            deep = Color(0xFF294762),
            warm = Color(0xFFFFD28D),
        )

        text.containsAny("上海", "广州", "武汉", "长沙", "天津", "宁波", "郑州", "合肥", "福州", "南昌", "沈阳", "石家庄", "济南", "太原", "兰州", "南宁", "佛山", "东莞") -> TravelDestinationTheme(
            title = "城市",
            imageRes = R.drawable.travel_hero_metropolis,
            skyTop = Color(0xFFEAF4F1),
            skyBottom = Color(0xFFBACFCB),
            accent = Color(0xFFD69A25),
            deep = Color(0xFF1F4F57),
            warm = Color(0xFFFFCF76),
        )

        text.containsAny("东京", "大阪", "京都", "首尔", "曼谷", "新加坡", "巴黎", "伦敦", "纽约") -> TravelDestinationTheme(
            title = "海外",
            imageRes = if (text.containsAny("东京")) R.drawable.travel_hero_tokyo else R.drawable.travel_hero_metropolis,
            skyTop = Color(0xFFEAF7FF),
            skyBottom = Color(0xFFC8D7DF),
            accent = Color(0xFFE0A321),
            deep = Color(0xFF244B5C),
            warm = Color(0xFFFFC979),
        )

        else -> TravelDestinationTheme(
            title = "目的地",
            imageRes = R.drawable.travel_hero_metropolis,
            skyTop = Color(0xFFFFF7E8),
            skyBottom = Color(0xFFD7D8C4),
            accent = TravelPrimaryGold,
            deep = Color(0xFF3D514A),
            warm = Color(0xFFFFD786),
        )
    }
}

private fun String.containsAny(vararg keywords: String): Boolean {
    return keywords.any { keyword -> contains(keyword, ignoreCase = true) }
}

private fun shareTravelFullBill(
    context: Context,
    trip: TravelTrip,
): Boolean {
    val exportDir = File(context.cacheDir, "travel_exports").apply { mkdirs() }
    val fileName = "${safeTravelExportName(trip.name)}-${System.currentTimeMillis()}.xlsx"
    val exportFile = File(exportDir, fileName)
    exportFile.writeBytes(buildTravelFullBillXlsx(trip))

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        exportFile,
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "${trip.name} 完整账单")
        clipData = ClipData.newRawUri("完整账单", uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    return try {
        context.startActivity(Intent.createChooser(intent, "导出完整账单"))
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}

private fun safeTravelExportName(name: String): String {
    val safeName = name
        .trim()
        .replace(Regex("""[\\/:*?"<>|]"""), "_")
        .take(40)
    return safeName.ifBlank { "旅行账单" }
}

private fun travelRemainingValue(summary: TravelSummary): String {
    return when {
        summary.budgetCents <= 0L -> "未设置"
        summary.remainingCents < 0L -> "超出 ${formatMoney(-summary.remainingCents)}"
        else -> formatMoney(summary.remainingCents)
    }
}

private fun travelBudgetStatusText(summary: TravelSummary): String {
    return when {
        summary.budgetCents <= 0L -> "设置预算后显示剩余"
        summary.remainingCents < 0L -> "预算已超出"
        else -> "预算内可用"
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

private fun travelTitleInputSpec(category: TravelExpenseCategory): TravelTitleInputSpec {
    return when (category) {
        TravelExpenseCategory.Transport -> TravelTitleInputSpec(
            label = "交通支出",
            placeholder = "如：上海 → 杭州、机场打车、加油费、停车费",
            suggestions = listOf("打车", "加油费", "停车费", "高铁票", "机票"),
        )

        TravelExpenseCategory.Stay -> TravelTitleInputSpec(
            label = "住宿名称",
            placeholder = "如：酒店押金、民宿 2 晚",
            suggestions = listOf("酒店", "民宿", "酒店押金"),
        )

        TravelExpenseCategory.Food -> TravelTitleInputSpec(
            label = "餐饮支出",
            placeholder = "如：早餐、晚餐、咖啡",
            suggestions = listOf("早餐", "午餐", "晚餐", "咖啡"),
        )

        TravelExpenseCategory.Tickets -> TravelTitleInputSpec(
            label = "景点 / 门票名称",
            placeholder = "如：故宫、迪士尼门票",
            suggestions = listOf("门票", "景区门票", "演出门票"),
        )

        TravelExpenseCategory.Shopping -> TravelTitleInputSpec(
            label = "购物支出",
            placeholder = "如：伴手礼、药妆、纪念品",
            suggestions = listOf("伴手礼", "纪念品", "药妆"),
        )

        TravelExpenseCategory.Other -> TravelTitleInputSpec(
            label = "支出名称",
            placeholder = "如：行李寄存、电话卡、手续费",
            suggestions = listOf("行李寄存", "电话卡", "手续费"),
        )
    }
}

private fun travelParticipantsLabel(members: List<String>): String {
    return when (members.size) {
        0 -> "全员均摊"
        1 -> "仅 ${members.first()}"
        else -> "${members.size} 人参与"
    }
}
