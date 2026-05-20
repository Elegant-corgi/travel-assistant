package com.billapp.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardBackspace
import androidx.compose.material.icons.filled.Liquor
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import com.billapp.data.BillDraft
import com.billapp.data.BillEntry
import com.billapp.data.BillStats
import com.billapp.data.CategoryAmount
import com.billapp.data.StatPoint
import com.billapp.data.StatRange
import com.billapp.data.appendAmountInput
import com.billapp.data.defaultCategories
import com.billapp.data.formatDraftAmount
import com.billapp.data.formatMoney
import com.billapp.data.removeLastAmountInput
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

private const val APP_TITLE = "\u4e2a\u4eba\u8d26\u5355"
private const val AA_TITLE = "AA\u5206\u8d26"
private const val OPEN_MENU = "\u6253\u5f00\u529f\u80fd\u5217\u8868"
private const val ADD_BILL = "\u65b0\u589e\u8d26\u5355"
private const val ADD_AA = "新建AA\u5206\u8d26"
private const val DRAWER_TITLE = "\u529f\u80fd\u5217\u8868"
private const val EMPTY_TITLE = "\u8fd8\u6ca1\u6709\u8d26\u5355"
private const val EMPTY_HINT = "\u70b9\u53f3\u4e0b\u89d2\u6309\u94ae\u5148\u8bb0\u4e00\u7b14"
private const val NO_NOTE = "\u65e0\u5907\u6ce8"
private const val EDIT_TEXT = "\u7f16\u8f91"
private const val DELETE_TEXT = "\u5220\u9664"
private const val SHEET_TITLE = "\u8bb0\u4e00\u7b14"
private const val CANCEL_TEXT = "\u53d6\u6d88"
private const val CATEGORY_HINT = "\u5148\u9009\u62e9\u8fd9\u7b14\u8d26\u7684\u7c7b\u578b"
private const val NOTE_LABEL = "\u5907\u6ce8"
private const val NOTE_PLACEHOLDER = "\u8fd9\u91cc\u8f93\u5165\u5907\u6ce8"
private const val DATE_TEXT = "\u65e5\u671f"
private const val STATS_COUNT_TEXT = "\u7edf\u8ba1\u8303\u56f4\u5185\u5171 %d \u7b14\u652f\u51fa"
private const val TOTAL_EXPENSE_TEXT = "\u603b\u652f\u51fa"
private const val AVERAGE_TEXT = "\u65e5\u5747"
private const val BILL_COUNT_TEXT = "\u7b14\u6570"
private const val CATEGORY_RATIO_TEXT = "\u5206\u7c7b\u6392\u884c"
private const val NO_DATA_TEXT = "\u6682\u65e0\u6570\u636e"
private const val CLEAR_TEXT = "\u6e05\u7a7a"
private const val BACKSPACE_TEXT = "\u5220\u9664"
private const val CONFIRM_TEXT = "\u786e\u8ba4"
private const val CHANGE_CATEGORY_TEXT = "\u91cd\u9009\u5206\u7c7b"
private const val CATEGORY_PAGE_SIZE = 10
private const val CATEGORY_ROW_SIZE = 5
private val CategoryCardHeight = 118.dp
private val CompactCategoryCardHeight = 84.dp
private val CompactCategoryStripItemWidth = 64.dp
private val DrawerSheetWidth = 220.dp

private val SheetBackground = Color(0xFFFFFCF6)
private val SheetPanel = Color(0xFFF5F4FF)
private val SheetCard = Color(0xFFFFFFFF)
private val SoftText = Color(0xFF8E8B9C)
private val HighlightYellow = Color(0xFFFFD558)
private val HighlightYellowDark = Color(0xFFF4B400)
private val KeyboardKey = Color(0xFFFFFFFF)
private val KeyboardAccent = Color(0xFFFFD34F)
private val KeyboardAction = Color(0xFFF3F0FF)
private val CategoryBase = Color(0xFFF7F7FB)

private data class CategoryVisual(
    val label: String,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBackground: Color,
)

private val categoryVisuals = listOf(
    CategoryVisual("\u9910\u996e", Icons.Default.Fastfood, Color(0xFFFF9A4D), Color(0xFFFFE8BF)),
    CategoryVisual("\u852c\u83dc", Icons.Default.Spa, Color(0xFFFF9F58), Color(0xFFFFE8C8)),
    CategoryVisual("\u6c34\u679c", Icons.Default.Park, Color(0xFFFFB04E), Color(0xFFFFEBC5)),
    CategoryVisual("\u4ea4\u901a", Icons.Default.Train, Color(0xFFFF9D46), Color(0xFFFFE9C9)),
    CategoryVisual("\u8d2d\u7269", Icons.Default.LocalMall, Color(0xFFFFB33E), Color(0xFFFFF0C9)),
    CategoryVisual("\u96f6\u98df", Icons.Default.Redeem, Color(0xFFFFA251), Color(0xFFFFE4C6)),
    CategoryVisual("\u751f\u6d3b\u7f34\u8d39", Icons.Default.WaterDrop, Color(0xFFFFA266), Color(0xFFFFE6D6)),
    CategoryVisual("\u670d\u88c5", Icons.Default.Checkroom, Color(0xFFFFBC58), Color(0xFFFFEDC8)),
    CategoryVisual("\u7f8e\u5bb9", Icons.Default.AutoAwesome, Color(0xFFFFA170), Color(0xFFFFE7D8)),
    CategoryVisual("\u8fd0\u52a8", Icons.Default.SportsBasketball, Color(0xFFFFB053), Color(0xFFFFECC9)),
    CategoryVisual("\u4f4f\u623f", Icons.Default.Home, Color(0xFFFFAE47), Color(0xFFFFECCB)),
    CategoryVisual("\u5a31\u4e50", Icons.Default.Movie, Color(0xFFFFA457), Color(0xFFFFE1D2)),
    CategoryVisual("\u901a\u8baf", Icons.Default.Call, Color(0xFFE0B645), Color(0xFFFFEFC1)),
    CategoryVisual("\u8f6c\u8d26", Icons.Default.Payments, Color(0xFFFFA66C), Color(0xFFFFE7D6)),
    CategoryVisual("\u533b\u7597", Icons.Default.LocalHospital, Color(0xFFFF9364), Color(0xFFFFDDD5)),
    CategoryVisual("\u5b66\u4e60", Icons.Default.School, Color(0xFFFFB44B), Color(0xFFFFEDCB)),
    CategoryVisual("\u65e5\u7528", Icons.Default.AutoAwesome, Color(0xFFFFA05E), Color(0xFFFFE7D3)),
    CategoryVisual("\u65c5\u884c", Icons.Default.Flight, Color(0xFFFF9F61), Color(0xFFFFE4D7)),
    CategoryVisual("\u996e\u54c1", Icons.Default.Liquor, Color(0xFF8F8AF7), Color(0xFFEAE9FF)),
    CategoryVisual("\u6570\u7801", Icons.Default.PhoneAndroid, Color(0xFFFFC04F), Color(0xFFFFEFC4)),
    CategoryVisual("\u7f51\u7edc\u865a\u62df", Icons.Default.Star, Color(0xFFFFB45B), Color(0xFFFFE8D1)),
    CategoryVisual("\u529e\u516c", Icons.Default.Book, Color(0xFFFFAD5B), Color(0xFFFFE6CB)),
    CategoryVisual("\u5b69\u5b50", Icons.Default.Favorite, Color(0xFFFFC762), Color(0xFFFFF0CB)),
    CategoryVisual("\u957f\u8f88", Icons.Default.VolunteerActivism, Color(0xFFFFAA73), Color(0xFFFFE3D8)),
    CategoryVisual("\u5ba0\u7269", Icons.Default.Pets, Color(0xFFFFA65F), Color(0xFFFFE4CC)),
    CategoryVisual("\u9c9c\u82b1", Icons.Default.Spa, Color(0xFFFF9B77), Color(0xFFFFE0DA)),
    CategoryVisual("\u793e\u4ea4", Icons.Default.Favorite, Color(0xFFFFB15A), Color(0xFFFFE8C9)),
    CategoryVisual("\u8ffd\u661f", Icons.Default.Star, Color(0xFF9A92FF), Color(0xFFE8E5FF)),
    CategoryVisual("\u4eb2\u53cb", Icons.Default.VolunteerActivism, Color(0xFFFFB769), Color(0xFFFFEBCF)),
    CategoryVisual("\u9996\u9970", Icons.Default.AutoAwesome, Color(0xFF9E95FF), Color(0xFFECE9FF)),
    CategoryVisual("\u5176\u4ed6", Icons.Default.Payments, Color(0xFF8F8AF7), Color(0xFFE9E7FF)),
)

private fun visualForCategory(label: String): CategoryVisual {
    return categoryVisuals.firstOrNull { it.label == label }
        ?: CategoryVisual(label, Icons.Default.Favorite, Color(0xFFFFA05C), Color(0xFFFFE7D1))
}

private fun sectionTitle(section: AppSection): String = when (section) {
    AppSection.Billing -> APP_TITLE
    AppSection.Travel -> "旅行助手"
    AppSection.AaSplit -> AA_TITLE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillApp(viewModel: BillViewModel) {
    val bills by viewModel.bills.collectAsStateWithLifecycle()
    val selectedSection by viewModel.selectedSection.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val range by viewModel.statRange.collectAsStateWithLifecycle()
    val editorState by viewModel.editorState.collectAsStateWithLifecycle()
    val prioritizedCategories by viewModel.prioritizedCategories.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(DrawerSheetWidth),
            ) {
                AppDrawerContent(
                    selectedSection = selectedSection,
                    onSectionClick = { section ->
                        viewModel.selectSection(section)
                        coroutineScope.launch { drawerState.close() }
                    },
                )
            }
        },
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = sectionTitle(selectedSection),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                coroutineScope.launch { drawerState.open() }
                            },
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = OPEN_MENU)
                        }
                    },
                    actions = {
                        if (selectedSection == AppSection.Travel) {
                            IconButton(onClick = viewModel::openTravelTripCreator) {
                                Icon(Icons.Default.Add, contentDescription = "创建出行计划")
                            }
                        }
                    },
                )
            },
            floatingActionButton = {
                when (selectedSection) {
                    AppSection.Billing -> {
                        if (selectedTab == AppTab.Ledger) {
                            FloatingActionButton(onClick = viewModel::openNewBill) {
                                Icon(Icons.Default.Add, contentDescription = ADD_BILL)
                            }
                        }
                    }

                    AppSection.Travel -> {
                        FloatingActionButton(onClick = viewModel::openTravelExpenseCreator) {
                            Icon(Icons.Default.Add, contentDescription = "新增旅行支出")
                        }
                    }

                    AppSection.AaSplit -> {
                        FloatingActionButton(onClick = viewModel::openAaCreator) {
                            Icon(Icons.Default.Add, contentDescription = ADD_AA)
                        }
                    }
                }
            },
            bottomBar = {
                if (selectedSection == AppSection.Billing) {
                    NavigationBar {
                        AppTab.entries.forEach { tab ->
                            NavigationBarItem(
                                selected = selectedTab == tab,
                                onClick = { viewModel.selectTab(tab) },
                                icon = {
                                    Icon(
                                        imageVector = when (tab) {
                                            AppTab.Ledger -> Icons.AutoMirrored.Filled.ReceiptLong
                                            AppTab.Stats -> Icons.Default.CalendarMonth
                                        },
                                        contentDescription = tab.label,
                                    )
                                },
                                label = { Text(tab.label) },
                            )
                        }
                    }
                }
            },
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.background,
                            ),
                        ),
                    ),
            ) {
                when (selectedSection) {
                    AppSection.Billing -> when (selectedTab) {
                        AppTab.Ledger -> LedgerScreen(
                            bills = bills,
                            onEdit = viewModel::editBill,
                            onDelete = viewModel::deleteBill,
                            modifier = Modifier.fillMaxSize(),
                        )

                        AppTab.Stats -> StatsScreen(
                            stats = stats,
                            range = range,
                            onRangeChange = viewModel::selectRange,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    AppSection.Travel -> TravelScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize(),
                    )

                    AppSection.AaSplit -> AaSplitScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }

    if (editorState.open) {
        ModalBottomSheet(
            onDismissRequest = viewModel::closeEditor,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            BillEditorSheet(
                state = editorState,
                categories = prioritizedCategories,
                onDraftChange = viewModel::updateDraft,
                onSave = viewModel::saveDraft,
                onCancel = viewModel::closeEditor,
            )
        }
    }
}

@Composable
private fun AppDrawerContent(
    selectedSection: AppSection,
    onSectionClick: (AppSection) -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = DRAWER_TITLE,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        AppSection.entries.forEach { section ->
            NavigationDrawerItem(
                selected = selectedSection == section,
                onClick = { onSectionClick(section) },
                icon = {
                    Icon(
                        imageVector = when (section) {
                            AppSection.Billing -> Icons.AutoMirrored.Filled.ReceiptLong
                            AppSection.Travel -> Icons.Default.Flight
                            AppSection.AaSplit -> Icons.Default.Payments
                        },
                        contentDescription = section.label,
                    )
                },
                label = { Text(section.label) },
            )
        }
    }
}

@Composable
private fun LedgerScreen(
    bills: List<BillEntry>,
    onEdit: (BillEntry) -> Unit,
    onDelete: (BillEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dayGroups = remember(bills) {
        bills.map { bill -> bill to LocalDate.parse(bill.dateIso) }
            .sortedWith(
                compareByDescending<Pair<BillEntry, LocalDate>> { it.second }
                    .thenByDescending { it.first.updatedAt },
            )
            .groupBy { it.second }
            .map { (date, items) ->
                BillDayGroup(
                    date = date,
                    bills = items.map { it.first },
                    totalCents = items.sumOf { it.first.amountCents },
                )
            }
    }

    if (dayGroups.isEmpty()) {
        EmptyState(modifier = modifier)
        return
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        items(dayGroups, key = { it.date.toString() }) { group ->
            BillDaySection(
                group = group,
                onEdit = onEdit,
                onDelete = onDelete,
            )
        }
    }
}

@Composable
private fun StatsScreen(
    stats: BillStats,
    range: StatRange,
    onRangeChange: (StatRange) -> Unit,
    modifier: Modifier = Modifier,
) {
    val visiblePoints = remember(stats.points, range) {
        visibleStatPoints(stats, range)
    }
    val selectedIndexState = remember(range, visiblePoints.size) {
        mutableIntStateOf(defaultStatSelectionIndex(range, visiblePoints.size))
    }
    val selectedIndex = selectedIndexState.intValue.coerceIn(0, visiblePoints.lastIndex.coerceAtLeast(0))
    val selectedPoint = visiblePoints.getOrNull(selectedIndex)

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = stats.title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                )
                StatRangeSelector(
                    range = range,
                    onRangeChange = onRangeChange,
                )
            }
        }
        item {
            StatOverviewStrip(
                metrics = listOf(
                    StatMetric(
                        label = TOTAL_EXPENSE_TEXT,
                        value = formatMoney(stats.totalCents),
                    ),
                    StatMetric(
                        label = AVERAGE_TEXT,
                        value = formatMoney(stats.averagePerDayCents),
                    ),
                    StatMetric(
                        label = BILL_COUNT_TEXT,
                        value = stats.totalCount.toString(),
                    ),
                ),
            )
        }
        item {
            SectionCard(title = trendSectionTitle(range)) {
                if (visiblePoints.isEmpty() || selectedPoint == null) {
                    Text(NO_DATA_TEXT)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatSelectionChip(
                            text = trendSelectionText(
                                stats = stats,
                                range = range,
                                selectedIndex = selectedIndex,
                                selectedPoint = selectedPoint,
                            ),
                        )
                        StatTrendChart(
                            points = visiblePoints,
                            selectedIndex = selectedIndex,
                            onPointSelected = { index ->
                                selectedIndexState.intValue = index
                            },
                        )
                    }
                }
            }
        }
        item {
            SectionCard(title = CATEGORY_RATIO_TEXT) {
                if (stats.categories.isEmpty()) {
                    Text(NO_DATA_TEXT)
                } else {
                    CategoryList(categories = stats.categories, total = stats.totalCents)
                }
            }
        }
    }
}

@Composable
private fun StatRangeSelector(
    range: StatRange,
    onRangeChange: (StatRange) -> Unit,
) {
    Surface(
        color = Color(0xFFF8F7FB),
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            StatRange.entries.forEach { item ->
                val selected = range == item
                Surface(
                    onClick = { onRangeChange(item) },
                    color = if (selected) Color(0xFF222222) else Color.Transparent,
                    contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier.weight(1f),
                ) {
                    Box(
                        modifier = Modifier.height(48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        )
                    }
                }
            }
        }
    }
}

private data class StatMetric(
    val label: String,
    val value: String,
)

@Composable
private fun StatOverviewStrip(
    metrics: List<StatMetric>,
) {
    ElevatedCard(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 18.dp),
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
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    )
                    Text(
                        text = metric.label,
                        color = SoftText,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                if (index != metrics.lastIndex) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(32.dp)
                            .background(Color(0xFFEAE7F0)),
                    )
                }
            }
        }
    }
}

@Composable
private fun StatSelectionChip(
    text: String,
) {
    Surface(
        color = SheetPanel,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun StatTrendChart(
    points: List<StatPoint>,
    selectedIndex: Int,
    onPointSelected: (Int) -> Unit,
) {
    val max = points.maxOfOrNull { it.amountCents }?.coerceAtLeast(1L) ?: 1L
    val chartHeight = 170.dp

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        points.forEachIndexed { index, point ->
            val selected = index == selectedIndex
            val ratio = point.amountCents.toFloat() / max.toFloat()
            val barHeight = if (point.amountCents == 0L) {
                0.dp
            } else {
                (chartHeight * ratio).coerceAtLeast(if (selected) 18.dp else 12.dp)
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onPointSelected(index) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .height(chartHeight)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Box(
                        modifier = Modifier
                            .width(if (selected) 30.dp else 20.dp)
                            .height(barHeight)
                            .background(
                                brush = if (selected) {
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0xFFFFF6CF),
                                            HighlightYellow,
                                        ),
                                    )
                                } else {
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0xFFF1EFF8),
                                            Color(0xFFE7E1EF),
                                        ),
                                    )
                                },
                                shape = MaterialTheme.shapes.extraLarge,
                            ),
                    )
                }

                Surface(
                    color = if (selected) HighlightYellow.copy(alpha = 0.22f) else Color.Transparent,
                    shape = MaterialTheme.shapes.extraLarge,
                ) {
                    Text(
                        text = point.label,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        ),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp),
            )
            Text(
                text = EMPTY_TITLE,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Text(
                text = EMPTY_HINT,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BillDaySection(
    group: BillDayGroup,
    onEdit: (BillEntry) -> Unit,
    onDelete: (BillEntry) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = formatLedgerDateTitle(group.date),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                )
                Text(
                    text = formatLedgerWeekday(group.date),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Text(
                text = "支出:${formatLedgerMoney(group.totalCents)}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            group.bills.forEach { bill ->
                BillLedgerItem(
                    bill = bill,
                    onEdit = { onEdit(bill) },
                    onDelete = { onDelete(bill) },
                )
            }
        }
    }
}

@Composable
private fun BillLedgerItem(
    bill: BillEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val visual = visualForCategory(bill.category.ifBlank { defaultCategories.first() })

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(
                    color = visual.iconBackground,
                    shape = MaterialTheme.shapes.extraLarge,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = visual.icon,
                contentDescription = bill.category,
                tint = visual.iconTint,
                modifier = Modifier.size(28.dp),
            )
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = bill.category.ifBlank { defaultCategories.first() },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    )
                    Text(
                        text = bill.note.ifBlank { NO_NOTE },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = formatLedgerMoney(bill.amountCents, signed = true),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Text(EDIT_TEXT)
                }
                TextButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Text(DELETE_TEXT)
                }
            }
        }
    }
}

private data class BillDayGroup(
    val date: LocalDate,
    val bills: List<BillEntry>,
    val totalCents: Long,
)

private fun formatLedgerDateTitle(date: LocalDate): String {
    return "${date.monthValue}\u6708${date.dayOfMonth}\u65e5"
}

private fun formatLedgerWeekday(date: LocalDate): String {
    val weekDay = when (date.dayOfWeek) {
        DayOfWeek.MONDAY -> "\u5468\u4e00"
        DayOfWeek.TUESDAY -> "\u5468\u4e8c"
        DayOfWeek.WEDNESDAY -> "\u5468\u4e09"
        DayOfWeek.THURSDAY -> "\u5468\u56db"
        DayOfWeek.FRIDAY -> "\u5468\u4e94"
        DayOfWeek.SATURDAY -> "\u5468\u516d"
        DayOfWeek.SUNDAY -> "\u5468\u65e5"
    }
    return "($weekDay)"
}

private fun formatLedgerMoney(cents: Long, signed: Boolean = false): String {
    val raw = formatMoney(cents).removePrefix("¥")
    return if (signed) {
        "-$raw"
    } else {
        raw
    }
}

@Composable
private fun BillEditorSheet(
    state: EditorState,
    categories: List<String>,
    onDraftChange: ((BillDraft) -> BillDraft) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    val draft = state.draft
    val hasSelectedCategory = draft.category.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .background(SheetBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (hasSelectedCategory) {
            AmountEditorContent(
                draft = draft,
                categories = categories,
                error = state.error,
                onDraftChange = onDraftChange,
                onBackToCategory = {
                    onDraftChange { it.copy(category = "") }
                },
                onSave = onSave,
                onCancel = onCancel,
            )
        } else {
            CategoryPickerContent(
                draft = draft,
                categories = categories,
                error = state.error,
                onDraftChange = onDraftChange,
                onCancel = onCancel,
            )
        }
    }
}

@Composable
private fun CategoryPickerContent(
    draft: BillDraft,
    categories: List<String>,
    error: String?,
    onDraftChange: ((BillDraft) -> BillDraft) -> Unit,
    onCancel: () -> Unit,
) {
    val categoryPages = remember(categories) { categories.chunked(CATEGORY_PAGE_SIZE) }

    Column(
        verticalArrangement = Arrangement.spacedBy(18.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(width = 52.dp, height = 6.dp)
                .background(
                    color = Color(0xFFE6E4EB),
                    shape = MaterialTheme.shapes.extraLarge,
                ),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = SHEET_TITLE,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            )
            TextButton(onClick = onCancel) {
                Text(CANCEL_TEXT)
            }
        }

        Text(
            text = CATEGORY_HINT,
            style = MaterialTheme.typography.bodyLarge,
            color = SoftText,
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SheetBackground,
        ) {
            CategoryPager(
                categories = categoryPages,
                selectedCategory = draft.category,
                onCategoryClick = { category ->
                    onDraftChange { it.copy(category = category) }
                },
            )
        }

        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun CategoryOptionCard(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onClick: () -> Unit,
) {
    val visual = visualForCategory(label)
    val containerColor = if (selected) {
        Color(0xFFFFF1BF)
    } else {
        CategoryBase
    }
    val borderColor = if (selected) {
        HighlightYellowDark.copy(alpha = 0.55f)
    } else {
        Color.Transparent
    }
    val cardHeight = if (compact) CompactCategoryCardHeight else CategoryCardHeight
    val iconBoxSize = if (compact) 42.dp else 58.dp
    val iconSize = if (compact) 22.dp else 30.dp
    val verticalPadding = if (compact) 10.dp else 16.dp
    val contentSpacing = if (compact) 8.dp else 12.dp
    val textStyle = if (compact) {
        MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
    } else {
        MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
    }

    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = MaterialTheme.shapes.extraLarge,
        color = containerColor,
        tonalElevation = if (selected) 1.dp else 0.dp,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Column(
            modifier = Modifier
                .height(cardHeight)
                .padding(horizontal = 8.dp, vertical = verticalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(contentSpacing),
        ) {
            Box(
                modifier = Modifier
                    .size(iconBoxSize)
                    .background(
                        color = visual.iconBackground,
                        shape = MaterialTheme.shapes.large,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = visual.icon,
                    contentDescription = label,
                    tint = visual.iconTint,
                    modifier = Modifier.size(iconSize),
                )
            }
            Text(
                text = visual.label,
                style = textStyle,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun AmountEditorContent(
    draft: BillDraft,
    categories: List<String>,
    error: String?,
    onDraftChange: ((BillDraft) -> BillDraft) -> Unit,
    onBackToCategory: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    val context = LocalContext.current
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(width = 52.dp, height = 6.dp)
                .background(
                    color = Color(0xFFE6E4EB),
                    shape = MaterialTheme.shapes.extraLarge,
                ),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = SHEET_TITLE,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            )
            TextButton(onClick = onCancel) {
                Text(CANCEL_TEXT)
            }
        }

        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(
                containerColor = SheetPanel,
            ),
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CategoryBadge(
                            label = draft.category,
                            onClick = onBackToCategory,
                        )

                        Button(
                            onClick = {
                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        onDraftChange {
                                            it.copy(date = LocalDate.of(year, month + 1, dayOfMonth))
                                        }
                                    },
                                    draft.date.year,
                                    draft.date.monthValue - 1,
                                    draft.date.dayOfMonth,
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SheetCard,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                            ),
                            shape = MaterialTheme.shapes.extraLarge,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null)
                            Spacer(modifier = Modifier.size(4.dp))
                            Text("$DATE_TEXT ${draft.date.format(dateFormatter)}")
                            Spacer(modifier = Modifier.size(2.dp))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }

                    Surface(
                        color = Color.Transparent,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        CompactCategoryStrip(
                            categories = categories,
                            selectedCategory = draft.category,
                            onCategoryClick = { category ->
                                onDraftChange { it.copy(category = category) }
                            },
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = SheetCard),
                    shape = MaterialTheme.shapes.extraLarge,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = "\u00a5${formatDraftAmount(draft.amountText)}",
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0xFFF0EEF5)),
                        )
                        OutlinedTextField(
                            value = draft.note,
                            onValueChange = { value ->
                                onDraftChange { it.copy(note = value) }
                            },
                            label = { Text(NOTE_LABEL) },
                            placeholder = { Text(NOTE_PLACEHOLDER) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = MaterialTheme.shapes.large,
                        )
                    }
                }

                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                AmountKeyboard(
                    onKeyPress = { token ->
                        onDraftChange { current ->
                            current.copy(amountText = appendAmountInput(current.amountText, token))
                        }
                    },
                    onClear = {
                        onDraftChange { current ->
                            current.copy(amountText = "")
                        }
                    },
                    onDelete = {
                        onDraftChange { current ->
                            current.copy(amountText = removeLastAmountInput(current.amountText))
                        }
                    },
                    onConfirm = onSave,
                )
            }
        }
    }
}

@Composable
private fun AmountKeyboard(
    onKeyPress: (String) -> Unit,
    onClear: () -> Unit,
    onDelete: () -> Unit,
    onConfirm: () -> Unit,
) {
    val rows = listOf(
        listOf("7", "8", "9", BACKSPACE_TEXT),
        listOf("4", "5", "6", CLEAR_TEXT),
        listOf("1", "2", "3", CONFIRM_TEXT),
        listOf(".", "0", "00"),
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                row.forEach { key ->
                    val isWideLabel = key.length > 1
                    KeyboardButton(
                        text = key,
                        modifier = Modifier.weight(1f),
                        containerColor = if (key == CONFIRM_TEXT) {
                            KeyboardAccent
                        } else if (key == CLEAR_TEXT || key == BACKSPACE_TEXT) {
                            KeyboardAction
                        } else {
                            KeyboardKey
                        },
                        contentColor = if (key == CONFIRM_TEXT) {
                            Color(0xFF3A2F00)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        compactLabel = isWideLabel,
                        onClick = when (key) {
                            BACKSPACE_TEXT -> onDelete
                            CLEAR_TEXT -> onClear
                            CONFIRM_TEXT -> onConfirm
                            else -> { { onKeyPress(key) } }
                        },
                    )
                }
                if (row.size < 4) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun KeyboardButton(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    compactLabel: Boolean = false,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(60.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        contentPadding = if (compactLabel) {
            PaddingValues(horizontal = 4.dp, vertical = 8.dp)
        } else {
            ButtonDefaults.ContentPadding
        },
        shape = MaterialTheme.shapes.large,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
    ) {
        Text(
            text = text,
            style = if (compactLabel) {
                MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            } else {
                MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
            },
            maxLines = 1,
            overflow = TextOverflow.Clip,
            softWrap = false,
        )
    }
}

@Composable
private fun CategoryBadge(
    label: String,
    onClick: () -> Unit,
) {
    val visual = visualForCategory(label)
    Surface(
        onClick = onClick,
        color = SheetCard,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(visual.iconBackground, MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = visual.icon,
                    contentDescription = label,
                    tint = visual.iconTint,
                    modifier = Modifier.size(18.dp),
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Text(
                text = CHANGE_CATEGORY_TEXT,
                color = SoftText,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun CompactCategoryStrip(
    categories: List<String>,
    selectedCategory: String,
    onCategoryClick: (String) -> Unit,
) {
    val listState = rememberLazyListState()
    val selectedIndex = remember(categories, selectedCategory) {
        categories.indexOf(selectedCategory).takeIf { it >= 0 } ?: 0
    }

    LaunchedEffect(selectedIndex) {
        listState.animateScrollToItem(selectedIndex.coerceAtLeast(0))
    }

    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 2.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        items(categories, key = { it }) { category ->
            CompactCategoryItem(
                label = category,
                selected = selectedCategory == category,
                onClick = { onCategoryClick(category) },
            )
        }
    }
}

@Composable
private fun CompactCategoryItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val visual = visualForCategory(label)
    val containerColor = if (selected) Color(0xFFFFF1BF) else SheetCard
    val borderColor = if (selected) HighlightYellowDark.copy(alpha = 0.55f) else Color.Transparent

    Surface(
        onClick = onClick,
        color = containerColor,
        shape = MaterialTheme.shapes.extraLarge,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.width(CompactCategoryStripItemWidth),
    ) {
        Column(
            modifier = Modifier
                .height(72.dp)
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(visual.iconBackground, MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = visual.icon,
                    contentDescription = label,
                    tint = visual.iconTint,
                    modifier = Modifier.size(18.dp),
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CategoryPager(
    categories: List<List<String>>,
    selectedCategory: String,
    onCategoryClick: (String) -> Unit,
    cardModifier: Modifier = Modifier,
    showContentPadding: Boolean = true,
    compact: Boolean = false,
) {
    val initialPage = remember(selectedCategory, categories) {
        categories.indexOfFirst { page -> selectedCategory.isNotBlank() && selectedCategory in page }
            .takeIf { it >= 0 } ?: 0
    }
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { categories.size.coerceAtLeast(1) },
    )

    LaunchedEffect(selectedCategory) {
        val targetPage = categories.indexOfFirst { page -> selectedCategory.isNotBlank() && selectedCategory in page }
        if (targetPage >= 0 && targetPage != pagerState.currentPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    val rowSpacing = if (compact) 8.dp else 12.dp
    val pagerSpacing = if (compact) 10.dp else 14.dp
    val dotSpacing = if (compact) 3.dp else 4.dp
    val activeDotSize = if (compact) 7.dp else 9.dp
    val inactiveDotSize = if (compact) 5.dp else 7.dp
    val emptySlotHeight = if (compact) CompactCategoryCardHeight else CategoryCardHeight

    Column(verticalArrangement = Arrangement.spacedBy(pagerSpacing)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
        ) { page ->
            val pageItems = categories
                .getOrElse(page) { emptyList() }
                .let { currentPage ->
                    if (currentPage.size >= CATEGORY_PAGE_SIZE) {
                        currentPage
                    } else {
                        currentPage + List(CATEGORY_PAGE_SIZE - currentPage.size) { "" }
                    }
                }
            Column(
                verticalArrangement = Arrangement.spacedBy(rowSpacing),
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (showContentPadding) Modifier else Modifier),
            ) {
                pageItems.chunked(CATEGORY_ROW_SIZE).forEach { rowCategories ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        rowCategories.forEach { category ->
                            if (category.isBlank()) {
                                Box(
                                    modifier = cardModifier
                                        .weight(1f)
                                        .height(emptySlotHeight),
                                )
                            } else {
                                CategoryOptionCard(
                                    label = category,
                                    selected = selectedCategory == category,
                                    modifier = cardModifier.weight(1f),
                                    compact = compact,
                                    onClick = { onCategoryClick(category) },
                                )
                            }
                        }
                    }
                }
            }
        }

        if (categories.size > 1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(categories.size) { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = dotSpacing)
                            .size(if (index == pagerState.currentPage) activeDotSize else inactiveDotSize)
                            .background(
                                color = if (index == pagerState.currentPage) {
                                    HighlightYellow
                                } else {
                                    Color(0xFFE1DFEA)
                                },
                                shape = MaterialTheme.shapes.extraLarge,
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
            content()
        }
    }
}

@Composable
private fun CategoryList(
    categories: List<CategoryAmount>,
    total: Long,
) {
    val denominator = total.coerceAtLeast(1L)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        categories.forEach { category ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(category.category)
                    Text(formatMoney(category.amountCents))
                }
                LinearProgressIndicator(
                    progress = { (category.amountCents.toFloat() / denominator.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                )
            }
        }
    }
}

private fun trendSectionTitle(range: StatRange): String = when (range) {
    StatRange.Week -> "每周趋势"
    StatRange.Month -> "月度小结"
    StatRange.Year -> "年度趋势"
}

private fun visibleStatPoints(
    stats: BillStats,
    range: StatRange,
): List<StatPoint> = when (range) {
    StatRange.Week -> stats.points
    StatRange.Month -> stats.points.take(LocalDate.now().dayOfMonth.coerceAtMost(stats.points.size))
    StatRange.Year -> stats.points.take(LocalDate.now().monthValue.coerceAtMost(stats.points.size))
}

private fun defaultStatSelectionIndex(
    range: StatRange,
    pointCount: Int,
): Int {
    if (pointCount <= 0) return 0
    val current = when (range) {
        StatRange.Week -> LocalDate.now().dayOfWeek.value - 1
        StatRange.Month -> LocalDate.now().dayOfMonth - 1
        StatRange.Year -> LocalDate.now().monthValue - 1
    }
    return current.coerceIn(0, pointCount - 1)
}

private fun trendSelectionText(
    stats: BillStats,
    range: StatRange,
    selectedIndex: Int,
    selectedPoint: StatPoint,
): String {
    val date = when (range) {
        StatRange.Week, StatRange.Month -> stats.startDate.plusDays(selectedIndex.toLong())
        StatRange.Year -> stats.startDate.plusMonths(selectedIndex.toLong())
    }
    val periodLabel = when (range) {
        StatRange.Week -> "${statWeekdayLabel(date)}（${date.format(DateTimeFormatter.ofPattern("MM月dd日"))}）"
        StatRange.Month -> stats.title
        StatRange.Year -> stats.title
    }
    return "$periodLabel 支出：${formatMoney(selectedPoint.amountCents)}"
}

private fun statWeekdayLabel(date: LocalDate): String = when (date.dayOfWeek) {
    DayOfWeek.MONDAY -> "周一"
    DayOfWeek.TUESDAY -> "周二"
    DayOfWeek.WEDNESDAY -> "周三"
    DayOfWeek.THURSDAY -> "周四"
    DayOfWeek.FRIDAY -> "周五"
    DayOfWeek.SATURDAY -> "周六"
    DayOfWeek.SUNDAY -> "周日"
}
