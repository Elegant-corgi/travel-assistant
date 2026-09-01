package com.billapp.ui

import com.billapp.data.BillEntry
import com.billapp.data.BillRepository
import com.billapp.data.TravelRepository
import com.billapp.data.TravelWorkspace
import com.billapp.data.sampleTravelWorkspace
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BillViewModelTravelShareTest {
    @Test
    fun handleTravelShareTextOpensTravelExpenseSheetWithParsedDraft() {
        val viewModel = buildViewModel(sampleTravelWorkspace())
        viewModel.selectSection(AppSection.Billing)

        val message = viewModel.handleTravelShareText("微信支付收款方：酒店 ¥388.00 支付成功")

        assertEquals(AppSection.Travel, viewModel.selectedSection.value)
        assertEquals("已从分享内容填入，请确认后保存", message)
        assertEquals(true, viewModel.travelUiState.value.open)
        assertEquals("酒店", viewModel.travelUiState.value.draft.title)
        assertEquals("388", viewModel.travelUiState.value.draft.amountText)
    }

    @Test
    fun handleTravelShareTextDoesNotOverwriteExistingDraftFields() {
        val viewModel = buildViewModel(sampleTravelWorkspace())
        viewModel.openTravelExpenseCreator()
        viewModel.updateTravelDraft { it.copy(title = "机场打车", amountText = "99") }

        viewModel.handleTravelShareText("微信支付收款方：酒店 ¥388.00 支付成功")

        assertEquals("机场打车", viewModel.travelUiState.value.draft.title)
        assertEquals("99", viewModel.travelUiState.value.draft.amountText)
    }

    @Test
    fun handleTravelShareTextWithoutTripKeepsSheetClosedAndReturnsMessage() {
        val viewModel = buildViewModel(TravelWorkspace())

        val message = viewModel.handleTravelShareText("实付 45.6")

        assertEquals(AppSection.Travel, viewModel.selectedSection.value)
        assertEquals("请先创建一个出行计划", message)
        assertFalse(viewModel.travelUiState.value.open)
        assertEquals("请先创建一个出行计划", viewModel.travelUiState.value.error)
    }

    private fun buildViewModel(workspace: TravelWorkspace): BillViewModel {
        return BillViewModel(
            billRepository = FakeBillRepository(),
            aaRepository = FakeAaRepository(),
            travelRepository = FakeTravelRepository(workspace),
        )
    }
}

private class FakeBillRepository : BillRepository {
    override val bills: StateFlow<List<BillEntry>> = MutableStateFlow(emptyList())
    override val monthlyBudgetText: StateFlow<String> = MutableStateFlow("")

    override suspend fun upsert(entry: BillEntry) = Unit

    override suspend fun upsertAll(entries: List<BillEntry>) = Unit

    override suspend fun delete(id: String) = Unit

    override suspend fun updateMonthlyBudget(budgetText: String) = Unit
}

private class FakeAaRepository : com.billapp.data.AaRepository {
    override val activities: StateFlow<List<AaActivity>> = MutableStateFlow(emptyList())

    override suspend fun upsert(activity: AaActivity) = Unit

    override suspend fun delete(id: String) = Unit
}

private class FakeTravelRepository(workspace: TravelWorkspace) : TravelRepository {
    private val state = MutableStateFlow(workspace)
    override val workspace: StateFlow<TravelWorkspace> = state

    override suspend fun update(workspace: TravelWorkspace) {
        state.value = workspace
    }
}
