package com.billapp.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.billapp.data.formatMoney
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val AA_PAGE_TITLE = "AA分账"
private const val AA_SUBTITLE = "支持多笔支出、多垫付人、按参与人归并结算"
private const val AA_SUMMARY_RECEIVABLE = "待收总额"
private const val AA_SUMMARY_PENDING = "未结清"
private const val AA_SUMMARY_MEMBERS = "常用成员"
private const val AA_RECENT_TITLE = "活动列表"
private const val AA_MEMBERS_TITLE = "常用成员"
private const val AA_COMMON_MEMBERS_TITLE = "常用成员"
private const val AA_COMMON_MEMBERS_EMPTY = "还没有常用成员，先手动添加几位"
private const val AA_SELECTED_MEMBERS_TITLE = "已选成员"
private const val AA_EMPTY_TITLE = "还没有AA活动"
private const val AA_EMPTY_HINT = "先建一笔聚餐、旅行或合租分账"
private const val AA_QUICK_NEW = "新建活动"
private const val AA_CREATE_TITLE = "新建AA活动"
private const val AA_EDIT_TITLE = "编辑AA活动"
private const val AA_CANCEL = "取消"
private const val AA_SAVE = "保存"
private const val AA_TITLE_LABEL = "活动名称"
private const val AA_TITLE_HINT = "例如：周末聚餐"
private const val AA_SCENE_LABEL = "场景"
private const val AA_MEMBERS_LABEL = "成员"
private const val AA_PAYER_LABEL = "默认垫付人"
private const val AA_AMOUNT_LABEL = "初始金额"
private const val AA_AMOUNT_HINT = "输入第一笔金额"
private const val AA_NOTE_LABEL = "备注"
private const val AA_NOTE_HINT = "可选备注，例如“火锅和饮料”"
private const val AA_MEMBER_ADD_HINT = "输入成员姓名"
private const val AA_MEMBER_ADD = "添加"
private const val AA_SETTLEMENT_TITLE = "结算结果"
private const val AA_COPY_RESULT = "复制结果"
private const val AA_MARK_SETTLED = "标记全结清"
private const val AA_UNMARK_SETTLED = "撤回全结清"
private const val AA_DELETE = "删除"
private const val AA_SETTLED = "已结清"
private const val AA_UNSETTLED = "待结清"

private enum class AaActivityFilter(val label: String) {
    All("全部"),
    Pending("待结清"),
    Settled("已结清"),
}

private data class AaSceneVisual(
    val icon: ImageVector,
    val iconTint: Color,
    val iconBackground: Color,
)

private data class AaMetric(
    val label: String,
    val value: String,
)

private val AaHeroBackground = Brush.linearGradient(
    listOf(Color(0xFFFFF6CF), Color(0xFFFFE08F)),
)

private fun visualForScene(scene: AaScene): AaSceneVisual {
    return when (scene) {
        AaScene.Dining -> AaSceneVisual(Icons.Default.Fastfood, Color(0xFFFF8B2C), Color(0xFFFFE7C3))
        AaScene.Travel -> AaSceneVisual(Icons.Default.Flight, Color(0xFF4B8FF5), Color(0xFFE4F0FF))
        AaScene.Housing -> AaSceneVisual(Icons.Default.Home, Color(0xFF8759F0), Color(0xFFF0E8FF))
        AaScene.Shopping -> AaSceneVisual(Icons.Default.LocalMall, Color(0xFFFF6B6B), Color(0xFFFFE6E4))
        AaScene.Daily -> AaSceneVisual(Icons.Default.SelfImprovement, Color(0xFF2FA56E), Color(0xFFE2F4EA))
    }
}

private fun formatAaTime(timestamp: Long): String {
    val formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm")
    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
        .format(formatter)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AaSplitScreen(
    viewModel: BillViewModel,
    modifier: Modifier = Modifier,
) {
    val activities by viewModel.aaActivities.collectAsStateWithLifecycle()
    val summary by viewModel.aaSummary.collectAsStateWithLifecycle()
    val commonMembers by viewModel.aaCommonMembers.collectAsStateWithLifecycle()
    val uiState by viewModel.aaUiState.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val recentMembers = remember(commonMembers) { commonMembers.take(8) }
    var filter by rememberSaveable { mutableStateOf(AaActivityFilter.All) }
    var aaCustomMemberText by rememberSaveable { mutableStateOf("") }
    var deleteActivityId by rememberSaveable { mutableStateOf<String?>(null) }
    var deleteExpenseTarget by rememberSaveable { mutableStateOf<Pair<String, String>?>(null) }
    val selectedActivity = uiState.detailActivityId?.let { id ->
        activities.firstOrNull { it.id == id }
    }
    val visibleActivities = remember(activities, filter) {
        activities
            .filter { activity ->
                when (filter) {
                    AaActivityFilter.All -> true
                    AaActivityFilter.Pending -> !activity.settled
                    AaActivityFilter.Settled -> activity.settled
                }
            }
            .sortedWith(compareBy<AaActivity> { it.settled }.thenByDescending { it.createdAt })
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (activities.isEmpty()) {
            AaEmptyState(
                onCreate = viewModel::openAaCreator,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 112.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item { AaHeroCard(summary = summary) }
                item {
                    AaMetricStrip(
                        metrics = listOf(
                            AaMetric(AA_SUMMARY_RECEIVABLE, formatMoney(summary.receivableCents)),
                            AaMetric(AA_SUMMARY_PENDING, summary.unsettledCount.toString()),
                            AaMetric(AA_SUMMARY_MEMBERS, summary.memberCount.toString()),
                        ),
                    )
                }
                item { AaQuickActions(onCreate = viewModel::openAaCreator) }
                item {
                    AaSectionCard(title = AA_MEMBERS_TITLE) {
                        AaMemberChips(
                            members = recentMembers,
                            onMemberClick = {},
                            selectedMembers = recentMembers,
                            clickable = false,
                        )
                    }
                }
                item {
                    AaFilterRow(
                        selected = filter,
                        onSelect = { filter = it },
                    )
                }
                item {
                    Text(
                        text = AA_RECENT_TITLE,
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                }
                if (visibleActivities.isEmpty()) {
                    item {
                        Text(
                            text = "当前筛选下没有活动",
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                items(visibleActivities, key = { it.id }) { activity ->
                    AaActivityCard(
                        activity = activity,
                        onOpen = { viewModel.openAaDetail(activity.id) },
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(buildAaCopyText(activity)))
                            Toast.makeText(context, "已复制结算结果", Toast.LENGTH_SHORT).show()
                        },
                        onEdit = { viewModel.openAaEditor(activity.id) },
                        onToggleSettled = { viewModel.toggleAaSettled(activity.id) },
                        onDelete = { deleteActivityId = activity.id },
                    )
                }
            }
        }

        if (uiState.open) {
            ModalBottomSheet(
                onDismissRequest = viewModel::closeAaCreator,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            ) {
                AaCreatorSheet(
                    uiState = uiState,
                    commonMembers = commonMembers,
                    customMemberText = aaCustomMemberText,
                    onCustomMemberTextChange = { aaCustomMemberText = it },
                    onDismiss = viewModel::closeAaCreator,
                    onTitleChange = { title -> viewModel.updateAaDraft { it.copy(title = title) } },
                    onAmountChange = { amountText -> viewModel.updateAaDraft { it.copy(amountText = amountText) } },
                    onNoteChange = { note -> viewModel.updateAaDraft { it.copy(note = note) } },
                    onSceneSelected = viewModel::selectAaScene,
                    onToggleMember = viewModel::toggleAaMember,
                    onAddMember = viewModel::addAaMember,
                    onSelectPayer = viewModel::selectAaPayer,
                    onSave = viewModel::saveAaDraft,
                )
            }
        }

        if (selectedActivity != null && !uiState.open && !uiState.expenseOpen) {
            ModalBottomSheet(
                onDismissRequest = viewModel::closeAaDetail,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            ) {
                AaDetailSheet(
                    activity = selectedActivity,
                    onDismiss = viewModel::closeAaDetail,
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(buildAaCopyText(selectedActivity)))
                        Toast.makeText(context, "已复制结算结果", Toast.LENGTH_SHORT).show()
                    },
                    onEditActivity = { viewModel.openAaEditor(selectedActivity.id) },
                    onAddExpense = { viewModel.openAaExpenseCreator(selectedActivity.id) },
                    onEditExpense = { expenseId -> viewModel.openAaExpenseEditor(selectedActivity.id, expenseId) },
                    onDeleteExpense = { expenseId -> deleteExpenseTarget = selectedActivity.id to expenseId },
                    onToggleTransfer = { key -> viewModel.toggleAaTransferSettled(selectedActivity.id, key) },
                    onToggleActivitySettled = { viewModel.toggleAaSettled(selectedActivity.id) },
                )
            }
        }

        if (uiState.expenseOpen && selectedActivity != null) {
            ModalBottomSheet(
                onDismissRequest = viewModel::closeAaExpenseEditor,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            ) {
                AaExpenseSheet(
                    uiState = uiState,
                    activity = selectedActivity,
                    onDismiss = viewModel::closeAaExpenseEditor,
                    onTitleChange = { title -> viewModel.updateAaExpenseDraft { it.copy(title = title) } },
                    onAmountChange = { amountText -> viewModel.updateAaExpenseDraft { it.copy(amountText = amountText) } },
                    onNoteChange = { note -> viewModel.updateAaExpenseDraft { it.copy(note = note) } },
                    onSelectPayer = viewModel::selectAaExpensePayer,
                    onToggleMember = viewModel::toggleAaExpenseMember,
                    onSave = viewModel::saveAaExpenseDraft,
                )
            }
        }

        deleteActivityId?.let { activityId ->
            val activity = activities.firstOrNull { it.id == activityId }
            if (activity != null) {
                ConfirmDeleteDialog(
                    title = "删除AA活动",
                    text = "确认删除「${activity.title}」吗？删除后无法恢复。",
                    onDismiss = { deleteActivityId = null },
                    onConfirm = {
                        viewModel.deleteAaActivity(activity.id)
                        deleteActivityId = null
                        Toast.makeText(context, "已删除AA活动", Toast.LENGTH_SHORT).show()
                    },
                )
            }
        }

        deleteExpenseTarget?.let { target ->
            val activity = activities.firstOrNull { it.id == target.first }
            val expense = activity?.let { aaExpensesForSettlement(it).firstOrNull { item -> item.id == target.second } }
            if (activity != null && expense != null) {
                ConfirmDeleteDialog(
                    title = "删除支出",
                    text = "确认删除「${expense.title}」吗？",
                    onDismiss = { deleteExpenseTarget = null },
                    onConfirm = {
                        viewModel.deleteAaExpense(activity.id, expense.id)
                        deleteExpenseTarget = null
                        Toast.makeText(context, "已删除支出", Toast.LENGTH_SHORT).show()
                    },
                )
            }
        }
    }
}

@Composable
private fun AaHeroCard(summary: AaSummary) {
    val shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = shape,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(AaHeroBackground, shape)
                .padding(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = AA_PAGE_TITLE,
                    style = androidx.compose.material3.MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Text(
                    text = AA_SUBTITLE,
                    color = Color(0xFF5D4A00),
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AaHeroPill("活动 ${summary.activityCount}")
                    AaHeroPill("${AA_SUMMARY_RECEIVABLE} ${formatMoney(summary.receivableCents)}")
                    AaHeroPill("已结清 ${summary.settledCount}")
                }
            }
        }
    }
}

@Composable
private fun AaHeroPill(text: String) {
    Surface(
        color = Color.White.copy(alpha = 0.55f),
        shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}

@Composable
private fun AaMetricStrip(metrics: List<AaMetric>) {
    ElevatedCard(
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
                        style = androidx.compose.material3.MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                        ),
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
private fun AaQuickActions(onCreate: () -> Unit) {
    Button(
        onClick = onCreate,
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
    ) {
        Icon(Icons.Default.Add, contentDescription = null)
        Spacer(modifier = Modifier.size(4.dp))
        Text(AA_QUICK_NEW)
    }
}

@Composable
private fun AaFilterRow(
    selected: AaActivityFilter,
    onSelect: (AaActivityFilter) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(AaActivityFilter.entries, key = { it.name }) { filter ->
            AaChip(
                text = filter.label,
                selected = selected == filter,
                onClick = { onSelect(filter) },
            )
        }
    }
}

@Composable
private fun AaSectionCard(
    title: String,
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
            Text(
                text = title,
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            content()
        }
    }
}

@Composable
private fun AaMemberChips(
    members: List<String>,
    selectedMembers: List<String>,
    clickable: Boolean = true,
    onMemberClick: (String) -> Unit,
) {
    if (members.isEmpty()) {
        Text(
            text = "暂无成员",
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(members, key = { it }) { member ->
            AaChip(
                text = member,
                selected = member in selectedMembers,
                clickable = clickable,
                onClick = { onMemberClick(member) },
            )
        }
    }
}

@Composable
private fun AaActivityCard(
    activity: AaActivity,
    onOpen: () -> Unit,
    onCopy: () -> Unit,
    onEdit: () -> Unit,
    onToggleSettled: () -> Unit,
    onDelete: () -> Unit,
) {
    val visual = visualForScene(activity.scene)
    val settlementLines = remember(activity) { buildAaSettlementLines(activity) }
    val pendingReceivable = remember(settlementLines) {
        settlementLines.filterNot { it.settled }.sumOf { it.amountCents }
    }
    val expenses = remember(activity) { aaExpensesForSettlement(activity) }

    Card(
        colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
        shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
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
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(visual.iconBackground, androidx.compose.material3.MaterialTheme.shapes.extraLarge),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = visual.icon,
                            contentDescription = activity.scene.label,
                            tint = visual.iconTint,
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = activity.title,
                            style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                        )
                        Text(
                            text = "${activity.scene.label} · ${activity.members.size}人 · ${expenses.size}笔 · ${formatAaTime(activity.createdAt)}",
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = AA_DELETE)
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AaStatusChip(
                    text = if (activity.settled) AA_SETTLED else AA_UNSETTLED,
                    selected = activity.settled,
                )
                AaStatusChip(
                    text = "总额 ${formatMoney(aaTotalAmountCents(activity))}",
                    selected = false,
                )
                AaStatusChip(
                    text = "待收 ${formatMoney(pendingReceivable)}",
                    selected = false,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = AA_SETTLEMENT_TITLE,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
                if (settlementLines.isEmpty()) {
                    Text(
                        text = "当前没有可转账项",
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    settlementLines.take(3).forEach { line ->
                        Text(
                            text = "${line.debtor} → ${line.creditor} ${formatMoney(line.amountCents)}${if (line.settled) " · 已收" else ""}",
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        )
                    }
                    if (settlementLines.size > 3) {
                        Text(
                            text = "点开查看全部 ${settlementLines.size} 笔结算",
                            color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onCopy) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Text(AA_COPY_RESULT)
                }
                TextButton(onClick = onToggleSettled) {
                    Text(if (activity.settled) AA_UNMARK_SETTLED else AA_MARK_SETTLED)
                }
            }
        }
    }
}

@Composable
private fun AaDetailSheet(
    activity: AaActivity,
    onDismiss: () -> Unit,
    onCopy: () -> Unit,
    onEditActivity: () -> Unit,
    onAddExpense: () -> Unit,
    onEditExpense: (String) -> Unit,
    onDeleteExpense: (String) -> Unit,
    onToggleTransfer: (String) -> Unit,
    onToggleActivitySettled: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val expenses = remember(activity) { aaExpensesForSettlement(activity).sortedByDescending { it.createdAt } }
    val lines = remember(activity) { buildAaSettlementLines(activity) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SheetHandle()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.title,
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
                Text(
                    text = "${activity.scene.label} · ${activity.members.joinToString("、")}",
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            TextButton(onClick = onDismiss) {
                Text(AA_CANCEL)
            }
        }

        AaSectionCard(title = "支出明细") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAddExpense, shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("新增支出")
                }
                OutlinedButton(onClick = onEditActivity, shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge) {
                    Text("编辑活动")
                }
            }
            expenses.forEach { expense ->
                AaExpenseRow(
                    expense = expense,
                    canDelete = expenses.size > 1,
                    onEdit = { onEditExpense(expense.id) },
                    onDelete = { onDeleteExpense(expense.id) },
                )
            }
        }

        AaSectionCard(title = AA_SETTLEMENT_TITLE) {
            if (lines.isEmpty()) {
                Text(
                    text = "当前没有可转账项",
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                lines.forEach { line ->
                    AaSettlementLineRow(
                        line = line,
                        onToggle = { onToggleTransfer(line.key) },
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onCopy) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Text(AA_COPY_RESULT)
                }
                TextButton(onClick = onToggleActivitySettled) {
                    Text(if (activity.settled) AA_UNMARK_SETTLED else AA_MARK_SETTLED)
                }
            }
        }
    }
}

@Composable
private fun AaExpenseRow(
    expense: AaExpense,
    canDelete: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Surface(
        color = Color(0xFFF8F6FB),
        shape = androidx.compose.material3.MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = expense.title,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                    Text(
                        text = "${expense.payer}垫付 · ${expense.members.size}人参与",
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    )
                    if (expense.note.isNotBlank()) {
                        Text(
                            text = expense.note,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                Text(
                    text = formatMoney(expense.amountCents),
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onEdit) {
                    Text("编辑")
                }
                if (canDelete) {
                    TextButton(onClick = onDelete) {
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

@Composable
private fun AaSettlementLineRow(
    line: AaSettlementLine,
    onToggle: () -> Unit,
) {
    Surface(
        color = if (line.settled) Color(0xFFE7F6EC) else Color(0xFFFFF8E1),
        shape = androidx.compose.material3.MaterialTheme.shapes.large,
        onClick = onToggle,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "${line.debtor} → ${line.creditor}",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
                Text(
                    text = if (line.settled) "已确认收款" else "点按标记为已收",
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                )
            }
            Text(
                text = formatMoney(line.amountCents),
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                ),
            )
        }
    }
}

@Composable
private fun AaStatusChip(
    text: String,
    selected: Boolean,
) {
    Surface(
        color = if (selected) Color(0xFFFFE38C) else Color(0xFFF4F2F7),
        shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}

@Composable
private fun AaChip(
    text: String,
    selected: Boolean,
    clickable: Boolean = true,
    onClick: () -> Unit,
) {
    val containerColor = if (selected) Color(0xFFFFE08C) else Color(0xFFF4F2F7)
    val shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge

    if (clickable) {
        Surface(
            color = containerColor,
            shape = shape,
            border = BorderStroke(1.dp, if (selected) Color(0xFFF0B400) else Color.Transparent),
            onClick = onClick,
        ) {
            AaChipText(text = text)
        }
    } else {
        Surface(
            color = containerColor,
            shape = shape,
            border = BorderStroke(1.dp, if (selected) Color(0xFFF0B400) else Color.Transparent),
        ) {
            AaChipText(text = text)
        }
    }
}

@Composable
private fun AaChipText(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.SemiBold,
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun AaSceneChip(
    scene: AaScene,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val visual = visualForScene(scene)
    val containerColor = if (selected) Color(0xFFFFF1BF) else Color(0xFFF4F2F7)
    val borderColor = if (selected) Color(0xFFF0B400) else Color.Transparent

    Surface(
        color = containerColor,
        shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
        border = BorderStroke(1.dp, borderColor),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(visual.iconBackground, androidx.compose.material3.MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = visual.icon,
                    contentDescription = scene.label,
                    tint = visual.iconTint,
                    modifier = Modifier.size(18.dp),
                )
            }
            Text(
                text = scene.label,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun AaEmptyState(
    onCreate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(Color(0xFFFFE7A3), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Payments,
                    contentDescription = null,
                    tint = Color(0xFF8C6300),
                    modifier = Modifier.size(34.dp),
                )
            }
            Text(
                text = AA_EMPTY_TITLE,
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                ),
            )
            Text(
                text = AA_EMPTY_HINT,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Button(onClick = onCreate) {
                Text(AA_QUICK_NEW)
            }
        }
    }
}

@Composable
private fun AaCreatorSheet(
    uiState: AaUiState,
    commonMembers: List<String>,
    customMemberText: String,
    onCustomMemberTextChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onTitleChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSceneSelected: (AaScene) -> Unit,
    onToggleMember: (String) -> Unit,
    onAddMember: (String) -> Unit,
    onSelectPayer: (String) -> Unit,
    onSave: () -> Unit,
) {
    val draft = uiState.draft
    val scrollState = rememberScrollState()
    val isEditing = uiState.editingActivityId != null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SheetHandle()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (isEditing) AA_EDIT_TITLE else AA_CREATE_TITLE,
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            TextButton(onClick = onDismiss) {
                Text(AA_CANCEL)
            }
        }

        OutlinedTextField(
            value = draft.title,
            onValueChange = onTitleChange,
            label = { Text(AA_TITLE_LABEL) },
            placeholder = { Text(AA_TITLE_HINT) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        AaSectionCard(title = AA_SCENE_LABEL) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(AaScene.entries, key = { it.name }) { scene ->
                    AaSceneChip(
                        scene = scene,
                        selected = draft.scene == scene,
                        onClick = { onSceneSelected(scene) },
                    )
                }
            }
        }

        AaSectionCard(title = AA_MEMBERS_LABEL) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = AA_COMMON_MEMBERS_TITLE,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                    if (commonMembers.isEmpty()) {
                        Text(
                            text = AA_COMMON_MEMBERS_EMPTY,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        )
                    } else {
                        AaMemberChips(
                            members = commonMembers,
                            selectedMembers = draft.members,
                            onMemberClick = onToggleMember,
                            clickable = true,
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = customMemberText,
                        onValueChange = onCustomMemberTextChange,
                        label = { Text(AA_MEMBER_ADD_HINT) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    Button(
                        onClick = {
                            val candidate = customMemberText.trim()
                            if (candidate.isNotBlank()) {
                                onAddMember(candidate)
                                onCustomMemberTextChange("")
                            }
                        },
                        shape = androidx.compose.material3.MaterialTheme.shapes.large,
                    ) {
                        Text(AA_MEMBER_ADD)
                    }
                }

                if (draft.members.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = AA_SELECTED_MEMBERS_TITLE,
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                        )
                        AaMemberChips(
                            members = draft.members,
                            selectedMembers = draft.members,
                            onMemberClick = onToggleMember,
                            clickable = true,
                        )
                    }
                }
            }
        }

        AaSectionCard(title = AA_PAYER_LABEL) {
            if (draft.members.isEmpty()) {
                Text(
                    text = "先选成员，再指定默认垫付人",
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                AaMemberChips(
                    members = draft.members,
                    selectedMembers = listOf(draft.payer),
                    onMemberClick = onSelectPayer,
                    clickable = true,
                )
            }
        }

        OutlinedTextField(
            value = draft.amountText,
            onValueChange = onAmountChange,
            label = { Text(if (isEditing) "当前总额" else AA_AMOUNT_LABEL) },
            placeholder = { Text(AA_AMOUNT_HINT) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        OutlinedTextField(
            value = draft.note,
            onValueChange = onNoteChange,
            label = { Text(AA_NOTE_LABEL) },
            placeholder = { Text(AA_NOTE_HINT) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        uiState.error?.let {
            Text(
                text = it,
                color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            )
        }

        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
            colors = ButtonDefaults.buttonColors(),
        ) {
            Text(AA_SAVE)
        }
    }
}

@Composable
private fun AaExpenseSheet(
    uiState: AaUiState,
    activity: AaActivity,
    onDismiss: () -> Unit,
    onTitleChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSelectPayer: (String) -> Unit,
    onToggleMember: (String) -> Unit,
    onSave: () -> Unit,
) {
    val draft = uiState.expenseDraft
    val scrollState = rememberScrollState()
    val isEditing = uiState.editingExpenseId != null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SheetHandle()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (isEditing) "编辑支出" else "新增支出",
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
                Text(
                    text = "只给实际参与的人均摊，系统会自动归并转账",
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                )
            }
            TextButton(onClick = onDismiss) {
                Text(AA_CANCEL)
            }
        }

        OutlinedTextField(
            value = draft.title,
            onValueChange = onTitleChange,
            label = { Text("支出名称") },
            placeholder = { Text("例如：火锅、打车、门票") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        OutlinedTextField(
            value = draft.amountText,
            onValueChange = onAmountChange,
            label = { Text("金额") },
            placeholder = { Text("输入金额") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        AaSectionCard(title = "垫付人") {
            AaMemberChips(
                members = activity.members,
                selectedMembers = listOf(draft.payer),
                onMemberClick = onSelectPayer,
            )
        }

        AaSectionCard(title = "参与人") {
            AaMemberChips(
                members = activity.members,
                selectedMembers = draft.members,
                onMemberClick = onToggleMember,
            )
        }

        OutlinedTextField(
            value = draft.note,
            onValueChange = onNoteChange,
            label = { Text(AA_NOTE_LABEL) },
            placeholder = { Text("可选，例如“李四没喝酒”") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        uiState.expenseError?.let {
            Text(
                text = it,
                color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            )
        }

        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
        ) {
            Text(if (isEditing) "保存支出" else "添加支出")
        }
    }
}

@Composable
private fun SheetHandle() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = 52.dp, height = 6.dp)
                .background(Color(0xFFE6E4EB), androidx.compose.material3.MaterialTheme.shapes.extraLarge),
        )
    }
}

@Composable
private fun ConfirmDeleteDialog(
    title: String,
    text: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = androidx.compose.material3.MaterialTheme.colorScheme.error,
            )
        },
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "删除",
                    color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(AA_CANCEL)
            }
        },
    )
}
