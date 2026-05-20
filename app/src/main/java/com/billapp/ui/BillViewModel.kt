package com.billapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.billapp.data.AaRepository
import com.billapp.data.BillDraft
import com.billapp.data.BillEntry
import com.billapp.data.BillRepository
import com.billapp.data.BillStats
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
import com.billapp.data.addTravelExpense
import com.billapp.data.currentTravelTrip as currentTravelTripFromWorkspace
import com.billapp.data.defaultTravelExpenseDraft
import com.billapp.data.defaultTravelTripDraft
import com.billapp.data.buildBillStats
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
import com.billapp.data.upsertTravelTrip
import java.math.BigDecimal
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
        _editorState.value = EditorState(
            open = true,
            draft = BillDraft(),
            error = null,
        )
    }

    fun openAaCreator() {
        _aaUiState.value = AaUiState(
            open = true,
            draft = defaultAaDraft(),
            error = null,
        )
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

    fun openTravelExpenseCreator() {
        val trip = selectedTravelTrip.value
        _travelUiState.value = TravelUiState(
            open = true,
            draft = defaultTravelExpenseDraft(
                defaultPayer = trip?.members?.firstOrNull().orEmpty(),
            ),
            error = null,
        )
    }

    fun closeTravelExpenseCreator() {
        _travelUiState.update { it.copy(open = false, error = null) }
    }

    fun openTravelTripCreator() {
        _travelTripCreatorState.value = TravelTripCreatorState(
            open = true,
            draft = defaultTravelTripDraft(),
            error = null,
        )
    }

    fun closeTravelTripCreator() {
        _travelTripCreatorState.update { it.copy(open = false, error = null) }
    }

    fun updateDraft(transform: (BillDraft) -> BillDraft) {
        _editorState.update { current ->
            current.copy(draft = transform(current.draft), error = null)
        }
    }

    fun updateAaDraft(transform: (AaDraft) -> AaDraft) {
        _aaUiState.update { current ->
            current.copy(draft = transform(current.draft), error = null)
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
                    draft = current.draft.copy(payer = candidate),
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

        val amount = current.amountText.trim().replace(",", ".").toBigDecimalOrNull()
        if (amount == null || amount <= BigDecimal.ZERO) {
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

        val activity = AaActivity(
            id = UUID.randomUUID().toString(),
            title = title,
            scene = current.scene,
            amountCents = amount,
            payer = current.payer,
            members = current.members.distinct(),
            note = current.note.trim(),
            settled = false,
            createdAt = System.currentTimeMillis(),
        )

        viewModelScope.launch {
            aaRepository.upsert(activity)
            _aaUiState.value = AaUiState()
        }
    }

    fun saveTravelExpense() {
        val currentDraft = _travelUiState.value.draft
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

        val expense = TravelExpense(
            id = UUID.randomUUID().toString(),
            title = title,
            category = currentDraft.category,
            amountCents = amount,
            payer = payer,
            members = trip.members.distinct(),
            note = currentDraft.note.trim(),
            settled = false,
            createdAt = System.currentTimeMillis(),
        )

        viewModelScope.launch {
            travelRepository.update(
                replaceCurrentTravelTrip(
                    travelWorkspace.value,
                    addTravelExpense(trip, expense),
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
            aaRepository.upsert(activity.copy(settled = !activity.settled))
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

    fun deleteAaActivity(activityId: String) {
        viewModelScope.launch {
            aaRepository.delete(activityId)
        }
    }

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
