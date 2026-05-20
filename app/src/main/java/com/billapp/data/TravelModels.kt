package com.billapp.data

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlinx.serialization.Serializable

private val TravelDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

@Serializable
enum class TravelExpenseCategory(val label: String) {
    Transport("交通"),
    Stay("住宿"),
    Food("餐饮"),
    Tickets("门票"),
    Shopping("购物"),
    Other("其他"),
}

@Serializable
data class TravelExpense(
    val id: String = "",
    val title: String = "",
    val category: TravelExpenseCategory = TravelExpenseCategory.Food,
    val amountCents: Long = 0L,
    val payer: String = "",
    val members: List<String> = emptyList(),
    val note: String = "",
    val settled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)

@Serializable
data class TravelChecklistItem(
    val id: String = "",
    val label: String = "",
    val packed: Boolean = false,
)

@Serializable
data class TravelTrip(
    val id: String = "",
    val name: String = "",
    val destination: String = "",
    val startDateIso: String = LocalDate.now().toString(),
    val endDateIso: String = LocalDate.now().toString(),
    val isOverseas: Boolean = false,
    val budgetCents: Long = 0L,
    val members: List<String> = emptyList(),
    val expenses: List<TravelExpense> = emptyList(),
    val checklist: List<TravelChecklistItem> = emptyList(),
    val reminders: List<String> = emptyList(),
)

@Serializable
data class TravelWorkspace(
    val trips: List<TravelTrip> = emptyList(),
    val selectedTripId: String = "",
)

data class TravelExpenseDraft(
    val title: String = "",
    val category: TravelExpenseCategory = TravelExpenseCategory.Food,
    val amountText: String = "",
    val payer: String = "",
    val note: String = "",
)

data class TravelTripDraft(
    val name: String = "",
    val destination: String = "",
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate = LocalDate.now().plusDays(3),
    val isOverseas: Boolean = false,
    val budgetText: String = "",
    val membersText: String = "我",
)

data class TravelUiState(
    val open: Boolean = false,
    val draft: TravelExpenseDraft = defaultTravelExpenseDraft(),
    val error: String? = null,
)

data class TravelTripCreatorState(
    val open: Boolean = false,
    val draft: TravelTripDraft = defaultTravelTripDraft(),
    val error: String? = null,
)

data class TravelSettlementLine(
    val debtor: String,
    val creditor: String,
    val amountCents: Long,
)

data class TravelSummary(
    val tripName: String,
    val destination: String,
    val totalDays: Int,
    val dayIndex: Int,
    val remainingDays: Int,
    val spentCents: Long,
    val budgetCents: Long,
    val remainingCents: Long,
    val budgetProgress: Float,
    val dailyAverageCents: Long,
    val expenseCount: Int,
    val memberCount: Int,
    val unsettledCount: Int,
    val settlementDueCents: Long,
)

fun defaultTravelExpenseDraft(defaultPayer: String = ""): TravelExpenseDraft = TravelExpenseDraft(
    payer = defaultPayer,
)

fun defaultTravelTripDraft(now: LocalDate = LocalDate.now()): TravelTripDraft = TravelTripDraft(
    startDate = now,
    endDate = now.plusDays(3),
)

fun sampleTravelTrip(now: LocalDate = LocalDate.now(), timestamp: Long = System.currentTimeMillis()): TravelTrip {
    val start = now.minusDays(1)
    val end = now.plusDays(3)
    val members = listOf("张三", "李四", "王五", "林娜")

    return TravelTrip(
        id = "sample-tokyo",
        name = "东京自由行",
        destination = "东京",
        startDateIso = start.toString(),
        endDateIso = end.toString(),
        isOverseas = true,
        budgetCents = 800_000,
        members = members,
        expenses = listOf(
            TravelExpense(
                id = "travel-flight",
                title = "往返机票",
                category = TravelExpenseCategory.Transport,
                amountCents = 320_000,
                payer = "张三",
                members = members,
                note = "含托运",
                settled = false,
                createdAt = timestamp - 3_600_000,
            ),
            TravelExpense(
                id = "travel-hotel",
                title = "民宿 2 晚",
                category = TravelExpenseCategory.Stay,
                amountCents = 180_000,
                payer = "王五",
                members = members,
                note = "含清洁费",
                settled = false,
                createdAt = timestamp - 3_200_000,
            ),
            TravelExpense(
                id = "travel-ticket",
                title = "美术馆门票",
                category = TravelExpenseCategory.Tickets,
                amountCents = 42_000,
                payer = "林娜",
                members = members,
                note = "预约票",
                settled = true,
                createdAt = timestamp - 2_800_000,
            ),
            TravelExpense(
                id = "travel-dinner",
                title = "居酒屋晚餐",
                category = TravelExpenseCategory.Food,
                amountCents = 38_000,
                payer = "李四",
                members = members,
                note = "含饮料",
                settled = false,
                createdAt = timestamp - 2_400_000,
            ),
            TravelExpense(
                id = "travel-rail",
                title = "机场交通",
                category = TravelExpenseCategory.Transport,
                amountCents = 9_600,
                payer = "张三",
                members = members,
                note = "地铁 + 巴士",
                settled = true,
                createdAt = timestamp - 2_000_000,
            ),
        ),
        checklist = listOf(
            TravelChecklistItem("passport", "护照/身份证", packed = true),
            TravelChecklistItem("charger", "充电器/充电宝", packed = true),
            TravelChecklistItem("ticket", "机票/酒店确认单", packed = false),
            TravelChecklistItem("meds", "常用药", packed = false),
            TravelChecklistItem("adapter", "转换插头", packed = true),
        ),
        reminders = listOf(
            "酒店 12:00 前退房",
            "返程航班提前 2 小时到机场",
            "护照和签证放进随身包",
        ),
    )
}

fun sampleTravelWorkspace(now: LocalDate = LocalDate.now(), timestamp: Long = System.currentTimeMillis()): TravelWorkspace {
    val trip = sampleTravelTrip(now = now, timestamp = timestamp)
    val domestic = TravelTrip(
        id = "sample-suzhou",
        name = "苏州周末游",
        destination = "苏州",
        startDateIso = now.toString(),
        endDateIso = now.plusDays(1).toString(),
        isOverseas = false,
        budgetCents = 2_000L,
        members = listOf("张三", "李四"),
        expenses = listOf(
            TravelExpense(
                id = "sample-suzhou-train",
                title = "高铁",
                category = TravelExpenseCategory.Transport,
                amountCents = 260L,
                payer = "张三",
                members = listOf("张三", "李四"),
                note = "往返",
                settled = false,
                createdAt = timestamp - 7_200_000,
            ),
        ),
        checklist = listOf(
            TravelChecklistItem("suzhou-ticket", "身份证/车票", packed = false),
            TravelChecklistItem("suzhou-charger", "充电器", packed = true),
        ),
        reminders = listOf(
            "周六上午出发",
            "返程前确认酒店退房",
        ),
    )
    return TravelWorkspace(
        trips = listOf(trip, domestic),
        selectedTripId = trip.id,
    )
}

fun travelTrips(workspace: TravelWorkspace): List<TravelTrip> = workspace.trips

fun currentTravelTrip(workspace: TravelWorkspace): TravelTrip? {
    return workspace.trips.firstOrNull { it.id == workspace.selectedTripId }
        ?: workspace.trips.firstOrNull()
}

fun selectTravelTrip(workspace: TravelWorkspace, tripId: String): TravelWorkspace {
    if (workspace.trips.none { it.id == tripId }) return workspace
    return workspace.copy(selectedTripId = tripId)
}

fun upsertTravelTrip(
    workspace: TravelWorkspace,
    trip: TravelTrip,
): TravelWorkspace {
    val updatedTrips = workspace.trips.toMutableList()
    val index = updatedTrips.indexOfFirst { it.id == trip.id }
    if (index >= 0) {
        updatedTrips[index] = trip
    } else {
        updatedTrips.add(0, trip)
    }
    return workspace.copy(
        trips = updatedTrips,
        selectedTripId = trip.id,
    )
}

fun deleteTravelTrip(
    workspace: TravelWorkspace,
    tripId: String,
): TravelWorkspace {
    val index = workspace.trips.indexOfFirst { it.id == tripId }
    if (index < 0) return workspace

    val updatedTrips = workspace.trips.toMutableList()
    updatedTrips.removeAt(index)
    val nextSelected = when {
        updatedTrips.isEmpty() -> ""
        workspace.selectedTripId != tripId -> workspace.selectedTripId
        else -> updatedTrips.getOrNull(index)?.id
            ?: updatedTrips.getOrNull(index - 1)?.id
            ?: updatedTrips.first().id
    }

    return workspace.copy(
        trips = updatedTrips,
        selectedTripId = nextSelected,
    )
}

fun replaceCurrentTravelTrip(
    workspace: TravelWorkspace,
    updatedTrip: TravelTrip,
): TravelWorkspace {
    val currentId = currentTravelTrip(workspace)?.id ?: return workspace
    val nextTrips = workspace.trips.map { trip ->
        if (trip.id == currentId) updatedTrip else trip
    }
    return workspace.copy(trips = nextTrips)
}

fun newTravelTripFromDraft(draft: TravelTripDraft): TravelTrip {
    val members = parseTravelMembers(draft.membersText).ifEmpty { listOf("我") }
    val safeEndDate = if (draft.endDate.isBefore(draft.startDate)) draft.startDate else draft.endDate
    val budget = parseMoneyToCents(draft.budgetText) ?: 0L
    return TravelTrip(
        id = java.util.UUID.randomUUID().toString(),
        name = draft.name.trim(),
        destination = draft.destination.trim(),
        startDateIso = draft.startDate.toString(),
        endDateIso = safeEndDate.toString(),
        isOverseas = draft.isOverseas,
        budgetCents = budget,
        members = members,
        expenses = emptyList(),
        checklist = defaultTravelChecklist(),
        reminders = defaultTravelReminders(draft.destination.trim()),
    )
}

fun parseTravelMembers(input: String): List<String> {
    return input
        .split(',', '，', '、', '\n', '\t', ' ')
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()
}

fun defaultTravelChecklist(): List<TravelChecklistItem> {
    return listOf(
        TravelChecklistItem("passport", "护照/身份证", packed = false),
        TravelChecklistItem("charger", "充电器/充电宝", packed = false),
        TravelChecklistItem("ticket", "机票/酒店确认单", packed = false),
        TravelChecklistItem("meds", "常用药", packed = false),
        TravelChecklistItem("adapter", "转换插头", packed = false),
    )
}

fun defaultTravelReminders(destination: String = ""): List<String> {
    val target = destination.ifBlank { "目的地" }
    return listOf(
        "出发前确认 $target 的机票和酒店",
        "把证件放进随身包",
        "返程前检查退房时间和航班时间",
    )
}

fun travelStartDate(trip: TravelTrip): LocalDate = parseTravelDate(trip.startDateIso)

fun travelEndDate(trip: TravelTrip): LocalDate = parseTravelDate(trip.endDateIso)

fun travelDateRangeLabel(trip: TravelTrip): String {
    return "${travelStartDate(trip).format(TravelDateFormatter)} - ${travelEndDate(trip).format(TravelDateFormatter)}"
}

fun travelTotalDays(trip: TravelTrip): Int {
    return (ChronoUnit.DAYS.between(travelStartDate(trip), travelEndDate(trip)).toInt() + 1).coerceAtLeast(1)
}

fun buildTravelSummary(
    trip: TravelTrip,
    today: LocalDate = LocalDate.now(),
): TravelSummary {
    val totalDays = travelTotalDays(trip)
    val spentCents = trip.expenses.sumOf { it.amountCents }
    val budgetCents = trip.budgetCents
    val remainingCents = budgetCents - spentCents
    val startDate = travelStartDate(trip)
    val endDate = travelEndDate(trip)
    val dayIndex = (ChronoUnit.DAYS.between(startDate, today).toInt() + 1).coerceIn(1, totalDays)
    val remainingDays = ChronoUnit.DAYS.between(today, endDate).toInt().coerceAtLeast(0)
    val budgetProgress = if (budgetCents <= 0L) 0f else (spentCents.toFloat() / budgetCents.toFloat()).coerceIn(0f, 1f)
    val settlementDueCents = buildTravelSettlementLines(trip).sumOf { it.amountCents }

    return TravelSummary(
        tripName = trip.name,
        destination = trip.destination,
        totalDays = totalDays,
        dayIndex = dayIndex,
        remainingDays = remainingDays,
        spentCents = spentCents,
        budgetCents = budgetCents,
        remainingCents = remainingCents,
        budgetProgress = budgetProgress,
        dailyAverageCents = spentCents / totalDays.coerceAtLeast(1).toLong(),
        expenseCount = trip.expenses.size,
        memberCount = trip.members.distinct().size,
        unsettledCount = trip.expenses.count { !it.settled },
        settlementDueCents = settlementDueCents,
    )
}

fun addTravelExpense(
    trip: TravelTrip,
    expense: TravelExpense,
): TravelTrip {
    return trip.copy(expenses = listOf(expense) + trip.expenses)
}

fun toggleTravelExpenseSettled(
    trip: TravelTrip,
    expenseId: String,
): TravelTrip {
    return trip.copy(
        expenses = trip.expenses.map { expense ->
            if (expense.id == expenseId) {
                expense.copy(settled = !expense.settled)
            } else {
                expense
            }
        },
    )
}

fun toggleTravelChecklist(
    trip: TravelTrip,
    itemId: String,
): TravelTrip {
    return trip.copy(
        checklist = trip.checklist.map { item ->
            if (item.id == itemId) {
                item.copy(packed = !item.packed)
            } else {
                item
            }
        },
    )
}

fun buildTravelSettlementLines(trip: TravelTrip): List<TravelSettlementLine> {
    val balances = trip.members.distinct().associateWith { 0L }.toMutableMap()

    trip.expenses
        .filterNot { it.settled }
        .forEach { expense ->
            val participants = (expense.members.takeIf { it.isNotEmpty() } ?: trip.members).distinct()
            if (participants.size < 2 || expense.payer !in participants) {
                return@forEach
            }

            val baseShare = expense.amountCents / participants.size.toLong()
            val remainder = (expense.amountCents % participants.size.toLong()).toInt()

            participants.forEachIndexed { index, member ->
                val share = baseShare + if (index < remainder) 1L else 0L
                balances[member] = (balances[member] ?: 0L) - share
            }
            balances[expense.payer] = (balances[expense.payer] ?: 0L) + expense.amountCents
        }

    val debtors = balances
        .filter { it.value < 0 }
        .map { it.key to -it.value }
        .sortedByDescending { it.second }
        .toMutableList()
    val creditors = balances
        .filter { it.value > 0 }
        .map { it.key to it.value }
        .sortedByDescending { it.second }
        .toMutableList()

    val lines = mutableListOf<TravelSettlementLine>()
    var debtorIndex = 0
    var creditorIndex = 0

    while (debtorIndex < debtors.size && creditorIndex < creditors.size) {
        val (debtorName, debtAmount) = debtors[debtorIndex]
        val (creditorName, creditAmount) = creditors[creditorIndex]
        val transfer = minOf(debtAmount, creditAmount)
        if (transfer > 0L) {
            lines += TravelSettlementLine(
                debtor = debtorName,
                creditor = creditorName,
                amountCents = transfer,
            )
        }

        debtors[debtorIndex] = debtorName to (debtAmount - transfer)
        creditors[creditorIndex] = creditorName to (creditAmount - transfer)

        if (debtors[debtorIndex].second <= 0L) {
            debtorIndex += 1
        }
        if (creditors[creditorIndex].second <= 0L) {
            creditorIndex += 1
        }
    }

    return lines
}

fun buildTravelCopyText(trip: TravelTrip): String {
    val summary = buildTravelSummary(trip)
    val settlementLines = buildTravelSettlementLines(trip)
    val checklistDone = trip.checklist.count { it.packed }

    return buildString {
        appendLine("${trip.name} · ${trip.destination}")
        appendLine("日期 ${travelDateRangeLabel(trip)}")
        appendLine("预算 ${formatMoney(summary.budgetCents)}")
        appendLine("已花 ${formatMoney(summary.spentCents)} / 剩余 ${formatMoney(summary.remainingCents)}")
        if (trip.members.isNotEmpty()) {
            appendLine("同行 ${trip.members.joinToString("、")}")
        }
        if (trip.checklist.isNotEmpty()) {
            appendLine("打包 ${checklistDone}/${trip.checklist.size}")
        }
        if (settlementLines.isNotEmpty()) {
            appendLine("待结算")
            settlementLines.forEach { line ->
                appendLine("${line.debtor} → ${line.creditor} ${formatMoney(line.amountCents)}")
            }
        }
    }.trimEnd()
}

private fun parseTravelDate(iso: String): LocalDate {
    return runCatching { LocalDate.parse(iso) }.getOrElse { LocalDate.now() }
}
