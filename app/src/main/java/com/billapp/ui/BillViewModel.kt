package com.billapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.billapp.data.AaRepository
import com.billapp.data.BillDraft
import com.billapp.data.BillEntry
import com.billapp.data.BillRepository
import com.billapp.data.BillStats
import com.billapp.data.BillType
import com.billapp.data.StatRange
import com.billapp.data.TravelExpense
import com.billapp.data.TravelExpenseCategory
import com.billapp.data.TravelExpenseDraft
import com.billapp.data.TravelTripCreatorState
import com.billapp.data.TravelTripDraft
import com.billapp.data.TravelRepository
import com.billapp.data.TravelTrip
import com.billapp.data.TravelWorkspace
import com.billapp.data.TravelUiState
import com.billapp.data.WechatBillImporter
import com.billapp.data.addTravelChecklistItem as addTravelChecklistItemToTrip
import com.billapp.data.addTravelReminder as addTravelReminderToTrip
import com.billapp.data.currentTravelTrip as currentTravelTripFromWorkspace
import com.billapp.data.defaultTravelExpenseDraft
import com.billapp.data.defaultTravelTripDraft
import com.billapp.data.buildBillStats
import com.billapp.data.deleteTravelChecklistItem as deleteTravelChecklistItemFromTrip
import com.billapp.data.deleteTravelExpense as deleteTravelExpenseFromTrip
import com.billapp.data.deleteTravelReminder as deleteTravelReminderFromTrip
import com.billapp.data.deleteTravelTrip
import com.billapp.data.parseMoneyToCents
import com.billapp.data.parseTravelMembers
import com.billapp.data.sortCategoriesByUsage
import com.billapp.data.newTravelTripFromDraft
import com.billapp.data.toDraft
import com.billapp.data.toEntry
import com.billapp.data.replaceCurrentTravelTrip
import com.billapp.data.selectTravelTrip as selectTravelTripInWorkspace
import com.billapp.data.toggleTravelChecklist as toggleTravelChecklistTrip
import com.billapp.data.toggleTravelExpenseSettled as toggleTravelExpenseSettledTrip
import com.billapp.data.upsertTravelExpense
import com.billapp.data.upsertTravelTrip
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppTab(val label: String) {
    Ledger("\u8d26\u5355"),
    Stats("\u7edf\u8ba1"),
}

enum class AppSection(val label: String) {
    Billing("\u8ba1\u8d39"),
    Travel("旅行助手"),
    AaSplit("AA\u5206\u8d26"),
}

data class EditorState(
    val open: Boolean = false,
    val draft: BillDraft = BillDraft(),
    val error: String? = null,
)

data class LedgerFilterState(
    val query: String = "",
    val category: String? = null,
)

data class WechatImportUiState(
    val importing: Boolean = false,
    val message: String? = null,
    val error: String? = null,
)

class BillViewModel(
    private val billRepository: BillRepository,
    private val aaRepository: AaRepository,
    private val travelRepository: TravelRepository,
) : ViewModel() {
    val bills: StateFlow<List<BillEntry>> = billRepository.bills

    private val _selectedSection = MutableStateFlow(AppSection.Billing)
    val selectedSection: StateFlow<AppSection> = _selectedSection

    private val _selectedTab = MutableStateFlow(AppTab.Ledger)
    val selectedTab: StateFlow<AppTab> = _selectedTab

    val aaActivities: StateFlow<List<AaActivity>> = aaRepository.activities

    private val _aaUiState = MutableStateFlow(AaUiState())
    val aaUiState: StateFlow<AaUiState> = _aaUiState

    val travelWorkspace: StateFlow<TravelWorkspace> = travelRepository.workspace

    val selectedTravelTrip: StateFlow<TravelTrip?> = travelWorkspace
        .map { workspace -> currentTravelTripFromWorkspace(workspace) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            currentTravelTripFromWorkspace(travelWorkspace.value),
        )

    private val _travelUiState = MutableStateFlow(TravelUiState())
    val travelUiState: StateFlow<TravelUiState> = _travelUiState

    private val _travelTripCreatorState = MutableStateFlow(TravelTripCreatorState())
    val travelTripCreatorState: StateFlow<TravelTripCreatorState> = _travelTripCreatorState

    val aaCommonMembers: StateFlow<List<String>> = aaActivities
        .map { activities -> commonAaMembers(activities) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            commonAaMembers(aaActivities.value),
        )

    private val _statRange = MutableStateFlow(StatRange.Week)
    val statRange: StateFlow<StatRange> = _statRange

    private val _editorState = MutableStateFlow(EditorState())
    val editorState: StateFlow<EditorState> = _editorState

    private val _ledgerFilter = MutableStateFlow(LedgerFilterState())
    val ledgerFilter: StateFlow<LedgerFilterState> = _ledgerFilter

    private val _wechatImportState = MutableStateFlow(WechatImportUiState())
    val wechatImportState: StateFlow<WechatImportUiState> = _wechatImportState

    val monthlyBudgetText: StateFlow<String> = billRepository.monthlyBudgetText

    val filteredBills: StateFlow<List<BillEntry>> = combine(bills, _ledgerFilter) { entries, filter ->
        val keyword = filter.query.trim()
        entries.filter { entry ->
            val amountText = formatCentsForInput(entry.amountCents)
            val matchesQuery = keyword.isBlank() ||
                entry.category.contains(keyword, ignoreCase = true) ||
                entry.note.contains(keyword, ignoreCase = true) ||
                entry.type.label.contains(keyword, ignoreCase = true) ||
                amountText.contains(keyword) ||
                entry.amountCents.toString().contains(keyword)
            val matchesCategory = filter.category == null || entry.category == filter.category
            matchesQuery && matchesCategory
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        bills.value,
    )

    val ledgerCategories: StateFlow<List<String>> = bills
        .map { entries ->
            entries
                .map { it.category.trim() }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList(),
        )

    val prioritizedCategories: StateFlow<List<String>> = bills
        .combine(_editorState) { entries, editor ->
            val sorted = sortCategoriesByUsage(entries)
            val selectedCategory = editor.draft.category.takeIf { it.isNotBlank() }
            if (selectedCategory != null && selectedCategory !in sorted) {
                listOf(selectedCategory) + sorted
            } else {
                sorted
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            sortCategoriesByUsage(emptyList()),
        )

    val stats: StateFlow<BillStats> = combine(bills, statRange) { entries, range ->
        buildBillStats(entries, range)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        buildBillStats(emptyList(), StatRange.Week),
    )

    val aaSummary: StateFlow<AaSummary> = aaActivities
        .map { activities -> buildAaSummary(activities) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            buildAaSummary(aaActivities.value),
        )

    fun selectTab(tab: AppTab) {
        _selectedTab.value = tab
    }

    fun selectSection(section: AppSection) {
        if (_selectedSection.value == section) return
        _selectedSection.value = section
        if (section != AppSection.Billing) {
            _editorState.update { it.copy(open = false, error = null) }
        }
        if (section != AppSection.AaSplit) {
            _aaUiState.update { it.copy(open = false, error = null) }
        }
        if (section != AppSection.Travel) {
            _travelUiState.update { it.copy(open = false, error = null) }
            _travelTripCreatorState.update { it.copy(open = false, error = null) }
        }
    }

    fun selectRange(range: StatRange) {
        _statRange.value = range
    }

    fun openNewBill() {
        _editorState.update { current ->
            current.copy(
                open = true,
                draft = current.draft.takeIf { it.id == null } ?: BillDraft(),
                error = null,
            )
        }
    }

    fun selectBillType(type: BillType) {
        _editorState.update { current ->
            current.copy(draft = current.draft.copy(type = type), error = null)
        }
    }

    fun openAaCreator() {
        _aaUiState.update { current ->
            current.copy(
                open = true,
                draft = current.draft.takeIf { current.editingActivityId == null } ?: defaultAaDraft(),
                editingActivityId = null,
                error = null,
            )
        }
    }

    fun openAaEditor(activityId: String) {
        val activity = aaActivities.value.firstOrNull { it.id == activityId } ?: return
        _aaUiState.update { current ->
            current.copy(
                open = true,
                draft = AaDraft(
                    title = activity.title,
                    scene = activity.scene,
                    amountText = formatCentsForInput(aaTotalAmountCents(activity)),
                    members = activity.members.distinct(),
                    payer = activity.payer,
                    note = activity.note,
                ),
                editingActivityId = activity.id,
                error = null,
            )
        }
    }

    fun openAaDetail(activityId: String) {
        if (aaActivities.value.none { it.id == activityId }) return
        _aaUiState.update { current ->
            current.copy(
                detailActivityId = activityId,
                expenseOpen = false,
                error = null,
                expenseError = null,
            )
        }
    }

    fun editBill(entry: BillEntry) {
        _editorState.value = EditorState(
            open = true,
            draft = entry.toDraft(),
            error = null,
        )
    }

    fun closeEditor() {
        _editorState.update { it.copy(open = false, error = null) }
    }

    fun closeAaCreator() {
        _aaUiState.update { it.copy(open = false, error = null) }
    }

    fun closeAaDetail() {
        _aaUiState.update {
            it.copy(
                detailActivityId = null,
                expenseOpen = false,
                editingExpenseId = null,
                expenseError = null,
            )
        }
    }

    fun openAaExpenseCreator(activityId: String) {
        val activity = aaActivities.value.firstOrNull { it.id == activityId } ?: return
        _aaUiState.update { current ->
            val shouldRestoreDraft = current.detailActivityId == activityId && current.editingExpenseId == null
            current.copy(
                detailActivityId = activityId,
                expenseOpen = true,
                expenseDraft = if (shouldRestoreDraft) {
                    current.expenseDraft
                } else {
                    defaultAaExpenseDraft(
                        payer = activity.payer.takeIf { it in activity.members } ?: activity.members.firstOrNull().orEmpty(),
                        members = activity.members.distinct(),
                    )
                },
                editingExpenseId = null,
                expenseError = null,
            )
        }
    }

    fun openAaExpenseEditor(activityId: String, expenseId: String) {
        val activity = aaActivities.value.firstOrNull { it.id == activityId } ?: return
        val expense = aaExpensesForSettlement(activity).firstOrNull { it.id == expenseId } ?: return
        _aaUiState.update { current ->
            current.copy(
                detailActivityId = activityId,
                expenseOpen = true,
                expenseDraft = AaExpenseDraft(
                    title = expense.title,
                    amountText = formatCentsForInput(expense.amountCents),
                    payer = expense.payer,
                    members = (expense.members.takeIf { it.isNotEmpty() } ?: activity.members).distinct(),
                    note = expense.note,
                ),
                editingExpenseId = expense.id,
                expenseError = null,
            )
        }
    }

    fun closeAaExpenseEditor() {
        _aaUiState.update {
            it.copy(
                expenseOpen = false,
                expenseError = null,
            )
        }
    }

    fun openTravelExpenseCreator() {
        val trip = selectedTravelTrip.value
        _travelUiState.update { current ->
            current.copy(
                open = true,
                draft = if (current.editingExpenseId == null && current.draft.hasInput()) {
                    current.draft
                } else {
                    defaultTravelExpenseDraft(
                        defaultPayer = trip?.members?.firstOrNull().orEmpty(),
                        participantMembers = trip?.members.orEmpty(),
                    )
                },
                editingExpenseId = null,
                error = null,
            )
        }
    }

    fun openTravelExpenseEditor(expenseId: String) {
        val trip = selectedTravelTrip.value ?: return
        val expense = trip.expenses.firstOrNull { it.id == expenseId } ?: return
        _travelUiState.value = TravelUiState(
            open = true,
            draft = TravelExpenseDraft(
                title = expense.title,
                category = expense.category,
                amountText = formatCentsForInput(expense.amountCents),
                payer = expense.payer,
                participantMembers = (expense.members.takeIf { it.isNotEmpty() } ?: trip.members).distinct(),
                note = expense.note,
            ),
            editingExpenseId = expense.id,
            error = null,
        )
    }

    fun closeTravelExpenseCreator() {
        _travelUiState.update { it.copy(open = false, error = null) }
    }

    fun openTravelTripCreator() {
        _travelTripCreatorState.update { current ->
            current.copy(
                open = true,
                draft = current.draft.takeIf { it.hasInput() } ?: defaultTravelTripDraft(),
                error = null,
            )
        }
    }

    fun closeTravelTripCreator() {
        _travelTripCreatorState.update { it.copy(open = false, error = null) }
    }

    fun updateDraft(transform: (BillDraft) -> BillDraft) {
        _editorState.update { current ->
            current.copy(draft = transform(current.draft), error = null)
        }
    }

    fun updateLedgerQuery(query: String) {
        _ledgerFilter.update { it.copy(query = query) }
    }

    fun selectLedgerCategory(category: String?) {
        _ledgerFilter.update { it.copy(category = category) }
    }

    fun updateMonthlyBudget(budgetText: String) {
        viewModelScope.launch {
            billRepository.updateMonthlyBudget(budgetText)
        }
    }

    fun updateAaDraft(transform: (AaDraft) -> AaDraft) {
        _aaUiState.update { current ->
            current.copy(draft = transform(current.draft), error = null)
        }
    }

    fun updateAaExpenseDraft(transform: (AaExpenseDraft) -> AaExpenseDraft) {
        _aaUiState.update { current ->
            current.copy(expenseDraft = transform(current.expenseDraft), expenseError = null)
        }
    }

    fun updateTravelDraft(transform: (TravelExpenseDraft) -> TravelExpenseDraft) {
        _travelUiState.update { current ->
            current.copy(draft = transform(current.draft), error = null)
        }
    }

    fun updateTravelTripDraft(transform: (TravelTripDraft) -> TravelTripDraft) {
        _travelTripCreatorState.update { current ->
            current.copy(draft = transform(current.draft), error = null)
        }
    }

    fun selectTravelTripOverseas(isOverseas: Boolean) {
        _travelTripCreatorState.update { current ->
            current.copy(
                draft = current.draft.copy(isOverseas = isOverseas),
                error = null,
            )
        }
    }

    fun selectTravelCategory(category: TravelExpenseCategory) {
        _travelUiState.update { current ->
            current.copy(
                draft = current.draft.copy(category = category),
                error = null,
            )
        }
    }

    fun selectTravelPayer(member: String) {
        val candidate = member.trim()
        if (candidate.isBlank()) return

        _travelUiState.update { current ->
            val trip = selectedTravelTrip.value
            if (trip == null || candidate !in trip.members) {
                current
            } else {
                current.copy(
                    draft = current.draft.copy(
                        payer = candidate,
                        participantMembers = (current.draft.participantMembers + candidate).distinct(),
                    ),
                    error = null,
                )
            }
        }
    }

    fun toggleTravelParticipant(member: String) {
        val candidate = member.trim()
        if (candidate.isBlank()) return

        _travelUiState.update { current ->
            val trip = selectedTravelTrip.value
            if (trip == null || candidate !in trip.members) {
                current
            } else {
                val nextMembers = if (candidate in current.draft.participantMembers) {
                    current.draft.participantMembers.filterNot { it == candidate }
                } else {
                    current.draft.participantMembers + candidate
                }
                current.copy(
                    draft = current.draft.copy(participantMembers = nextMembers.distinct()),
                    error = null,
                )
            }
        }
    }

    fun selectTravelTrip(tripId: String) {
        val workspace = travelWorkspace.value
        if (workspace.trips.none { it.id == tripId }) return
        viewModelScope.launch {
            travelRepository.update(selectTravelTripInWorkspace(workspace, tripId))
        }
    }

    fun deleteTravelTrip(tripId: String) {
        val workspace = travelWorkspace.value
        if (workspace.trips.none { it.id == tripId }) return
        viewModelScope.launch {
            travelRepository.update(deleteTravelTrip(workspace, tripId))
        }
    }

    fun addTravelMember(member: String) {
        val trip = selectedTravelTrip.value ?: return
        val candidate = member.trim()
        if (candidate.isBlank() || candidate in trip.members) return

        viewModelScope.launch {
            travelRepository.update(
                replaceCurrentTravelTrip(
                    travelWorkspace.value,
                    trip.copy(members = trip.members + candidate),
                ),
            )
        }
    }

    fun removeTravelMember(member: String) {
        val trip = selectedTravelTrip.value ?: return
        if (trip.members.size <= 1 || member !in trip.members) return

        viewModelScope.launch {
            travelRepository.update(
                replaceCurrentTravelTrip(
                    travelWorkspace.value,
                    trip.copy(members = trip.members.filterNot { it == member }),
                ),
            )
        }
    }

    fun toggleAaMember(member: String) {
        val candidate = member.trim()
        if (candidate.isBlank()) return

        _aaUiState.update { current ->
            val draft = current.draft
            val updatedMembers = if (candidate in draft.members) {
                draft.members.filterNot { it == candidate }
            } else {
                draft.members + candidate
            }.distinct()
            val updatedPayer = draft.payer.takeIf { it in updatedMembers } ?: updatedMembers.firstOrNull().orEmpty()

            current.copy(
                draft = draft.copy(
                    members = updatedMembers,
                    payer = updatedPayer,
                ),
                error = null,
            )
        }
    }

    fun addAaMember(member: String) {
        val candidate = member.trim()
        if (candidate.isBlank()) return

        _aaUiState.update { current ->
            val draft = current.draft
            val updatedMembers = (draft.members + candidate).distinct()
            val updatedPayer = draft.payer.takeIf { it in updatedMembers } ?: candidate

            current.copy(
                draft = draft.copy(
                    members = updatedMembers,
                    payer = updatedPayer,
                ),
                error = null,
            )
        }
    }

    fun selectAaPayer(member: String) {
        val candidate = member.trim()
        if (candidate.isBlank()) return

        _aaUiState.update { current ->
            if (candidate !in current.draft.members) {
                current
            } else {
                current.copy(
                    draft = current.draft.copy(payer = candidate),
                    error = null,
                )
            }
        }
    }

    fun selectAaExpensePayer(member: String) {
        val candidate = member.trim()
        if (candidate.isBlank()) return

        _aaUiState.update { current ->
            val activity = current.detailActivityId?.let { id ->
                aaActivities.value.firstOrNull { it.id == id }
            }
            if (activity == null || candidate !in activity.members) {
                current
            } else {
                current.copy(
                    expenseDraft = current.expenseDraft.copy(
                        payer = candidate,
                        members = (current.expenseDraft.members + candidate).distinct(),
                    ),
                    expenseError = null,
                )
            }
        }
    }

    fun toggleAaExpenseMember(member: String) {
        val candidate = member.trim()
        if (candidate.isBlank()) return

        _aaUiState.update { current ->
            val activity = current.detailActivityId?.let { id ->
                aaActivities.value.firstOrNull { it.id == id }
            }
            if (activity == null || candidate !in activity.members) {
                current
            } else {
                val nextMembers = if (candidate in current.expenseDraft.members) {
                    current.expenseDraft.members.filterNot { it == candidate }
                } else {
                    current.expenseDraft.members + candidate
                }.distinct()
                val nextPayer = current.expenseDraft.payer.takeIf { it in nextMembers }
                    ?: nextMembers.firstOrNull().orEmpty()
                current.copy(
                    expenseDraft = current.expenseDraft.copy(
                        members = nextMembers,
                        payer = nextPayer,
                    ),
                    expenseError = null,
                )
            }
        }
    }

    fun selectAaScene(scene: AaScene) {
        _aaUiState.update { current ->
            current.copy(
                draft = current.draft.copy(scene = scene),
                error = null,
            )
        }
    }

    fun saveDraft() {
        val current = _editorState.value.draft
        if (current.category.isBlank()) {
            _editorState.update { it.copy(error = "\u8bf7\u5148\u9009\u62e9\u8d26\u5355\u5206\u7c7b") }
            return
        }

        val amountCents = parseMoneyToCents(current.amountText)
        if (amountCents == null || amountCents <= 0L) {
            _editorState.update { it.copy(error = "\u8bf7\u8f93\u5165\u6b63\u786e\u91d1\u989d") }
            return
        }

        val entry = current.toEntry(
            previousCreatedAt = current.id?.let { existingId ->
                bills.value.firstOrNull { it.id == existingId }?.createdAt ?: System.currentTimeMillis()
            } ?: System.currentTimeMillis(),
        )

        viewModelScope.launch {
            billRepository.upsert(entry)
            _editorState.value = EditorState()
        }
    }

    fun importWechatBill(content: ByteArray) {
        if (_wechatImportState.value.importing) return
        _wechatImportState.value = WechatImportUiState(importing = true)
        viewModelScope.launch {
            val result = runCatching {
                WechatBillImporter.parse(content)
            }.getOrElse { error ->
                _wechatImportState.value = WechatImportUiState(error = "导入失败：${error.message ?: "文件无法解析"}")
                return@launch
            }

            if (result.entries.isEmpty()) {
                val detail = result.errorMessages.firstOrNull()?.let { "，$it" }.orEmpty()
                _wechatImportState.value = WechatImportUiState(error = "没有可导入的微信账单$detail")
                return@launch
            }

            runCatching {
                billRepository.upsertAll(result.entries)
            }.onSuccess {
                val skippedText = if (result.skippedCount > 0) "，跳过 ${result.skippedCount} 笔" else ""
                val errorText = result.errorMessages.takeIf { it.isNotEmpty() }
                    ?.joinToString(separator = "\n", prefix = "\n") { it }
                    .orEmpty()
                _wechatImportState.value = WechatImportUiState(
                    message = "已导入/更新 ${result.entries.size} 笔微信账单$skippedText$errorText",
                )
            }.onFailure { error ->
                _wechatImportState.value = WechatImportUiState(error = "保存失败：${error.message ?: "未知错误"}")
            }
        }
    }

    fun clearWechatImportMessage() {
        _wechatImportState.value = WechatImportUiState()
    }

    fun saveAaDraft() {
        val current = _aaUiState.value.draft
        val title = current.title.trim()
        if (title.isBlank()) {
            _aaUiState.update { it.copy(error = "请先输入活动名称") }
            return
        }
        if (current.members.size < 2) {
            _aaUiState.update { it.copy(error = "至少选择 2 位成员") }
            return
        }
        if (current.payer !in current.members) {
            _aaUiState.update { it.copy(error = "请选择正确的垫付人") }
            return
        }

        val amount = parseMoneyToCents(current.amountText)
        if (amount == null || amount <= 0L) {
            _aaUiState.update { it.copy(error = "请输入正确的总金额") }
            return
        }

        val editingActivityId = _aaUiState.value.editingActivityId
        val previousActivity = editingActivityId?.let { id ->
            aaActivities.value.firstOrNull { it.id == id }
        }
        val members = current.members.distinct()
        val normalizedExpenses = previousActivity?.expenses
            ?.map { expense ->
                val expenseMembers = expense.members.filter { it in members }
                val nextMembers = expenseMembers.ifEmpty { members }
                expense.copy(
                    payer = expense.payer.takeIf { it in members } ?: current.payer,
                    members = nextMembers,
                )
            }
            .orEmpty()
        val initialExpense = AaExpense(
            id = previousActivity?.expenses?.singleOrNull()?.id ?: UUID.randomUUID().toString(),
            title = title,
            amountCents = amount,
            payer = current.payer,
            members = members,
            note = current.note.trim(),
            createdAt = previousActivity?.createdAt ?: System.currentTimeMillis(),
        )
        val expenses = normalizedExpenses.ifEmpty { listOf(initialExpense) }

        val activity = AaActivity(
            id = previousActivity?.id ?: UUID.randomUUID().toString(),
            title = title,
            scene = current.scene,
            amountCents = amount,
            payer = current.payer,
            members = members,
            note = current.note.trim(),
            settled = previousActivity?.settled ?: false,
            isSample = false,
            createdAt = previousActivity?.createdAt ?: System.currentTimeMillis(),
            expenses = expenses,
            settledTransferKeys = previousActivity?.settledTransferKeys.orEmpty(),
        )

        viewModelScope.launch {
            aaRepository.upsert(activity)
            _aaUiState.value = AaUiState()
        }
    }

    fun saveAaExpenseDraft() {
        val ui = _aaUiState.value
        val activity = ui.detailActivityId?.let { id ->
            aaActivities.value.firstOrNull { it.id == id }
        } ?: return
        val draft = ui.expenseDraft
        val title = draft.title.trim()
        if (title.isBlank()) {
            _aaUiState.update { it.copy(expenseError = "请先输入支出名称") }
            return
        }
        val amount = parseMoneyToCents(draft.amountText)
        if (amount == null || amount <= 0L) {
            _aaUiState.update { it.copy(expenseError = "请输入正确的金额") }
            return
        }
        val members = draft.members
            .map { it.trim() }
            .filter { it in activity.members }
            .distinct()
        if (members.size < 2) {
            _aaUiState.update { it.copy(expenseError = "至少选择 2 位参与人") }
            return
        }
        val payer = draft.payer.trim()
        if (payer !in members) {
            _aaUiState.update { it.copy(expenseError = "垫付人需要在参与人中") }
            return
        }

        val existingExpenses = activity.expenses.takeIf { it.isNotEmpty() } ?: aaExpensesForSettlement(activity)
        val previousExpense = ui.editingExpenseId?.let { expenseId ->
            existingExpenses.firstOrNull { it.id == expenseId }
        }
        val expense = AaExpense(
            id = previousExpense?.id ?: UUID.randomUUID().toString(),
            title = title,
            amountCents = amount,
            payer = payer,
            members = members,
            note = draft.note.trim(),
            createdAt = previousExpense?.createdAt ?: System.currentTimeMillis(),
        )
        val nextExpenses = if (previousExpense == null) {
            listOf(expense) + existingExpenses
        } else {
            existingExpenses.map { current -> if (current.id == previousExpense.id) expense else current }
        }
        val updated = activity.copy(
            amountCents = nextExpenses.sumOf { it.amountCents },
            payer = nextExpenses.firstOrNull()?.payer ?: activity.payer,
            expenses = nextExpenses,
            settled = false,
            settledTransferKeys = emptyList(),
        )

        viewModelScope.launch {
            aaRepository.upsert(updated)
            _aaUiState.update {
                it.copy(
                    expenseOpen = false,
                    editingExpenseId = null,
                    expenseDraft = defaultAaExpenseDraft(),
                    expenseError = null,
                )
            }
        }
    }

    fun saveTravelExpense() {
        val currentDraft = _travelUiState.value.draft
        val editingExpenseId = _travelUiState.value.editingExpenseId
        val trip = selectedTravelTrip.value
        if (trip == null) {
            _travelUiState.update { it.copy(error = "请先创建一个出行计划") }
            return
        }
        val title = currentDraft.title.trim()
        if (title.isBlank()) {
            _travelUiState.update { it.copy(error = "请先输入支出名称") }
            return
        }

        val amount = parseMoneyToCents(currentDraft.amountText)
        if (amount == null || amount <= 0L) {
            _travelUiState.update { it.copy(error = "请输入正确的金额") }
            return
        }

        val payer = currentDraft.payer.trim()
        if (trip.members.isEmpty()) {
            _travelUiState.update { it.copy(error = "请先添加同行成员") }
            return
        }
        if (payer.isBlank() || payer !in trip.members) {
            _travelUiState.update { it.copy(error = "请选择正确的付款人") }
            return
        }
        val participants = currentDraft.participantMembers
            .map { it.trim() }
            .filter { it in trip.members }
            .distinct()
        if (participants.isEmpty()) {
            _travelUiState.update { it.copy(error = "请至少选择 1 位参与人") }
            return
        }
        if (payer !in participants) {
            _travelUiState.update { it.copy(error = "付款人也需要在参与人中") }
            return
        }

        val previousExpense = editingExpenseId?.let { id ->
            trip.expenses.firstOrNull { it.id == id }
        }
        val expense = TravelExpense(
            id = editingExpenseId ?: UUID.randomUUID().toString(),
            title = title,
            category = currentDraft.category,
            amountCents = amount,
            payer = payer,
            members = participants,
            note = currentDraft.note.trim(),
            settled = previousExpense?.settled ?: false,
            createdAt = previousExpense?.createdAt ?: System.currentTimeMillis(),
        )

        viewModelScope.launch {
            travelRepository.update(
                replaceCurrentTravelTrip(
                    travelWorkspace.value,
                    upsertTravelExpense(trip, expense),
                ),
            )
            _travelUiState.value = TravelUiState()
        }
    }

    fun saveTravelTrip() {
        val currentDraft = _travelTripCreatorState.value.draft
        val name = currentDraft.name.trim()
        if (name.isBlank()) {
            _travelTripCreatorState.update { it.copy(error = "请先输入行程名称") }
            return
        }
        val destination = currentDraft.destination.trim()
        if (destination.isBlank()) {
            _travelTripCreatorState.update { it.copy(error = "请先输入目的地") }
            return
        }
        val members = com.billapp.data.parseTravelMembers(currentDraft.membersText)
        if (members.isEmpty()) {
            _travelTripCreatorState.update { it.copy(error = "请至少填写 1 位同行成员") }
            return
        }

        val draft = currentDraft.copy(
            name = name,
            destination = destination,
            membersText = members.joinToString("、"),
        )
        val trip = newTravelTripFromDraft(draft)

        viewModelScope.launch {
            travelRepository.update(upsertTravelTrip(travelWorkspace.value, trip))
            _travelTripCreatorState.value = TravelTripCreatorState()
        }
    }

    fun deleteBill(entry: BillEntry) {
        viewModelScope.launch {
            billRepository.delete(entry.id)
        }
    }

    fun toggleAaSettled(activityId: String) {
        val activity = aaActivities.value.firstOrNull { it.id == activityId } ?: return
        viewModelScope.launch {
            val nextSettled = !activity.settled
            aaRepository.upsert(
                activity.copy(
                    settled = nextSettled,
                    settledTransferKeys = if (nextSettled) {
                        buildAaSettlementLines(activity).map { it.key }
                    } else {
                        emptyList()
                    },
                ),
            )
        }
    }

    fun toggleAaTransferSettled(activityId: String, transferKey: String) {
        val activity = aaActivities.value.firstOrNull { it.id == activityId } ?: return
        val nextKeys = if (transferKey in activity.settledTransferKeys) {
            activity.settledTransferKeys.filterNot { it == transferKey }
        } else {
            activity.settledTransferKeys + transferKey
        }
        val nextActivity = activity.copy(
            settled = false,
            settledTransferKeys = nextKeys.distinct(),
        )
        viewModelScope.launch {
            aaRepository.upsert(nextActivity)
        }
    }

    fun toggleTravelExpenseSettled(expenseId: String) {
        val trip = selectedTravelTrip.value ?: return
        viewModelScope.launch {
            travelRepository.update(
                replaceCurrentTravelTrip(
                    travelWorkspace.value,
                    toggleTravelExpenseSettledTrip(trip, expenseId),
                ),
            )
        }
    }

    fun deleteTravelExpense(expenseId: String) {
        val trip = selectedTravelTrip.value ?: return
        if (trip.expenses.none { it.id == expenseId }) return
        viewModelScope.launch {
            travelRepository.update(
                replaceCurrentTravelTrip(
                    travelWorkspace.value,
                    deleteTravelExpenseFromTrip(trip, expenseId),
                ),
            )
            if (_travelUiState.value.editingExpenseId == expenseId) {
                _travelUiState.value = TravelUiState()
            }
        }
    }

    fun toggleTravelChecklist(itemId: String) {
        val trip = selectedTravelTrip.value ?: return
        viewModelScope.launch {
            travelRepository.update(
                replaceCurrentTravelTrip(
                    travelWorkspace.value,
                    toggleTravelChecklistTrip(trip, itemId),
                ),
            )
        }
    }

    fun addTravelChecklistItem(label: String) {
        val trip = selectedTravelTrip.value ?: return
        viewModelScope.launch {
            travelRepository.update(
                replaceCurrentTravelTrip(
                    travelWorkspace.value,
                    addTravelChecklistItemToTrip(trip, label),
                ),
            )
        }
    }

    fun deleteTravelChecklistItem(itemId: String) {
        val trip = selectedTravelTrip.value ?: return
        if (trip.checklist.none { it.id == itemId }) return
        viewModelScope.launch {
            travelRepository.update(
                replaceCurrentTravelTrip(
                    travelWorkspace.value,
                    deleteTravelChecklistItemFromTrip(trip, itemId),
                ),
            )
        }
    }

    fun addTravelReminder(reminder: String) {
        val trip = selectedTravelTrip.value ?: return
        viewModelScope.launch {
            travelRepository.update(
                replaceCurrentTravelTrip(
                    travelWorkspace.value,
                    addTravelReminderToTrip(trip, reminder),
                ),
            )
        }
    }

    fun deleteTravelReminder(reminder: String) {
        val trip = selectedTravelTrip.value ?: return
        if (reminder !in trip.reminders) return
        viewModelScope.launch {
            travelRepository.update(
                replaceCurrentTravelTrip(
                    travelWorkspace.value,
                    deleteTravelReminderFromTrip(trip, reminder),
                ),
            )
        }
    }

    fun deleteAaActivity(activityId: String) {
        viewModelScope.launch {
            aaRepository.delete(activityId)
        }
    }

    fun deleteAaExpense(activityId: String, expenseId: String) {
        val activity = aaActivities.value.firstOrNull { it.id == activityId } ?: return
        val currentExpenses = activity.expenses.takeIf { it.isNotEmpty() } ?: aaExpensesForSettlement(activity)
        if (currentExpenses.size <= 1 || currentExpenses.none { it.id == expenseId }) return
        val nextExpenses = currentExpenses.filterNot { it.id == expenseId }
        viewModelScope.launch {
            aaRepository.upsert(
                activity.copy(
                    amountCents = nextExpenses.sumOf { it.amountCents },
                    payer = nextExpenses.firstOrNull()?.payer ?: activity.payer,
                    expenses = nextExpenses,
                    settled = false,
                    settledTransferKeys = emptyList(),
                ),
            )
        }
    }

    fun createAaFromBill(entry: BillEntry) {
        if (entry.type != BillType.Expense) return
        val payer = "我"
        _selectedSection.value = AppSection.AaSplit
        _aaUiState.value = AaUiState(
            open = true,
            draft = AaDraft(
                title = entry.note.ifBlank { entry.category },
                scene = AaScene.Daily,
                amountText = formatCentsForInput(entry.amountCents),
                members = listOf(payer),
                payer = payer,
                note = "来自账单：${entry.category}",
            ),
            error = null,
        )
    }

}

private fun formatCentsForInput(cents: Long): String {
    val yuan = cents / 100
    val centsPart = cents % 100
    return if (centsPart == 0L) {
        yuan.toString()
    } else {
        "$yuan.${centsPart.toString().padStart(2, '0')}"
    }
}

private fun TravelExpenseDraft.hasInput(): Boolean {
    return title.isNotBlank() ||
        amountText.isNotBlank() ||
        payer.isNotBlank() ||
        participantMembers.isNotEmpty() ||
        note.isNotBlank() ||
        category != TravelExpenseCategory.Food
}

private fun TravelTripDraft.hasInput(): Boolean {
    val emptyDraft = defaultTravelTripDraft()
    return name.isNotBlank() ||
        destination.isNotBlank() ||
        budgetText.isNotBlank() ||
        membersText != emptyDraft.membersText ||
        startDate != emptyDraft.startDate ||
        endDate != emptyDraft.endDate ||
        isOverseas != emptyDraft.isOverseas
}

class BillViewModelFactory(
    private val billRepository: BillRepository,
    private val aaRepository: AaRepository,
    private val travelRepository: TravelRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BillViewModel::class.java)) {
            return BillViewModel(billRepository, aaRepository, travelRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
