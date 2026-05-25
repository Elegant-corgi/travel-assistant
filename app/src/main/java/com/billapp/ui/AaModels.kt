package com.billapp.ui

import com.billapp.data.formatMoney
import kotlinx.serialization.Serializable

@Serializable
enum class AaScene(val label: String) {
    Dining("聚餐"),
    Travel("旅行"),
    Housing("房租"),
    Shopping("购物"),
    Daily("日常"),
}

@Serializable
data class AaDraft(
    val title: String = "",
    val scene: AaScene = AaScene.Dining,
    val amountText: String = "",
    val members: List<String> = emptyList(),
    val payer: String = "",
    val note: String = "",
)

@Serializable
data class AaExpense(
    val id: String,
    val title: String,
    val amountCents: Long,
    val payer: String,
    val members: List<String>,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)

@Serializable
data class AaActivity(
    val id: String,
    val title: String,
    val scene: AaScene,
    val amountCents: Long,
    val payer: String,
    val members: List<String>,
    val note: String = "",
    val settled: Boolean = false,
    val isSample: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val expenses: List<AaExpense> = emptyList(),
    val settledTransferKeys: List<String> = emptyList(),
)

data class AaExpenseDraft(
    val title: String = "",
    val amountText: String = "",
    val payer: String = "",
    val members: List<String> = emptyList(),
    val note: String = "",
)

data class AaUiState(
    val open: Boolean = false,
    val draft: AaDraft = defaultAaDraft(),
    val editingActivityId: String? = null,
    val detailActivityId: String? = null,
    val expenseOpen: Boolean = false,
    val expenseDraft: AaExpenseDraft = defaultAaExpenseDraft(),
    val editingExpenseId: String? = null,
    val error: String? = null,
    val expenseError: String? = null,
)

data class AaSummary(
    val activityCount: Int,
    val unsettledCount: Int,
    val settledCount: Int,
    val receivableCents: Long,
    val memberCount: Int,
)

data class AaSettlementLine(
    val debtor: String,
    val creditor: String,
    val amountCents: Long,
    val key: String,
    val settled: Boolean = false,
)

fun defaultAaDraft(): AaDraft = AaDraft()

fun defaultAaExpenseDraft(
    payer: String = "",
    members: List<String> = emptyList(),
): AaExpenseDraft = AaExpenseDraft(
    payer = payer,
    members = members,
)

fun sampleAaActivities(now: Long = System.currentTimeMillis()): List<AaActivity> {
    return listOf(
        AaActivity(
            id = "sample-dining",
            title = "周末聚餐",
            scene = AaScene.Dining,
            amountCents = 26_000,
            payer = "张三",
            members = listOf("张三", "李四", "王五", "赵敏", "陈杰"),
            note = "火锅和饮料",
            settled = false,
            isSample = true,
            createdAt = now - 2_400_000,
        ),
        AaActivity(
            id = "sample-travel",
            title = "旅行住宿",
            scene = AaScene.Travel,
            amountCents = 98_000,
            payer = "王五",
            members = listOf("张三", "李四", "王五", "林娜"),
            note = "三晚民宿",
            settled = true,
            isSample = true,
            createdAt = now - 86_400_000,
        ),
        AaActivity(
            id = "sample-house",
            title = "合租水电",
            scene = AaScene.Housing,
            amountCents = 18_600,
            payer = "李四",
            members = listOf("张三", "李四", "王五"),
            note = "4月账单",
            settled = false,
            isSample = true,
            createdAt = now - 172_800_000,
        ),
    )
}

fun commonAaMembers(activities: List<AaActivity>): List<String> {
    return activities
        .filterNot { it.isSample }
        .flatMap { it.members }
        .groupingBy { it }
        .eachCount()
        .entries
        .sortedWith(
            compareByDescending<Map.Entry<String, Int>> { it.value }
                .thenBy { it.key },
        )
        .map { it.key }
}

fun buildAaSummary(activities: List<AaActivity>): AaSummary {
    val unsettledActivities = activities.filterNot { it.settled }
    return AaSummary(
        activityCount = activities.size,
        unsettledCount = unsettledActivities.size,
        settledCount = activities.count { it.settled },
        receivableCents = unsettledActivities.sumOf { activity ->
            buildAaSettlementLines(activity)
                .filterNot { line -> line.settled }
                .sumOf { line -> line.amountCents }
        },
        memberCount = activities.flatMap { it.members }.distinct().size,
    )
}

fun aaExpensesForSettlement(activity: AaActivity): List<AaExpense> {
    if (activity.expenses.isNotEmpty()) return activity.expenses
    return listOf(
        AaExpense(
            id = "${activity.id}-legacy",
            title = activity.title,
            amountCents = activity.amountCents,
            payer = activity.payer,
            members = activity.members,
            note = activity.note,
            createdAt = activity.createdAt,
        ),
    )
}

fun aaTotalAmountCents(activity: AaActivity): Long {
    val expenses = aaExpensesForSettlement(activity)
    return expenses.sumOf { it.amountCents }
}

fun buildAaSettlementLines(activity: AaActivity): List<AaSettlementLine> {
    val balances = activity.members.distinct().associateWith { 0L }.toMutableMap()

    aaExpensesForSettlement(activity).forEach { expense ->
        val participants = (expense.members.takeIf { it.isNotEmpty() } ?: activity.members).distinct()
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

    val lines = mutableListOf<AaSettlementLine>()
    var debtorIndex = 0
    var creditorIndex = 0

    while (debtorIndex < debtors.size && creditorIndex < creditors.size) {
        val (debtorName, debtAmount) = debtors[debtorIndex]
        val (creditorName, creditAmount) = creditors[creditorIndex]
        val transfer = minOf(debtAmount, creditAmount)
        if (transfer > 0L) {
            val key = aaTransferKey(debtorName, creditorName, transfer)
            lines += AaSettlementLine(
                debtor = debtorName,
                creditor = creditorName,
                amountCents = transfer,
                key = key,
                settled = activity.settled || key in activity.settledTransferKeys,
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

fun buildAaCopyText(activity: AaActivity): String {
    val lines = buildAaSettlementLines(activity)
    val expenses = aaExpensesForSettlement(activity)
    return buildString {
        appendLine("${activity.title} · ${activity.scene.label}")
        appendLine("总额 ${formatMoney(aaTotalAmountCents(activity))}")
        appendLine("成员 ${activity.members.joinToString("、")}")
        if (activity.note.isNotBlank()) {
            appendLine("备注 ${activity.note}")
        }
        if (expenses.size > 1) {
            appendLine("支出明细")
            expenses.forEach { expense ->
                appendLine("${expense.title} ${formatMoney(expense.amountCents)} · ${expense.payer}垫付 · ${expense.members.size}人参与")
            }
        } else {
            appendLine("付款人 ${expenses.firstOrNull()?.payer.orEmpty()}")
        }
        if (lines.isNotEmpty()) {
            appendLine("结算结果")
            lines.forEach { line ->
                val status = if (line.settled) "（已收）" else ""
                appendLine("${line.debtor} → ${line.creditor} ${formatMoney(line.amountCents)}$status")
            }
        }
    }.trimEnd()
}

fun aaTransferKey(
    debtor: String,
    creditor: String,
    amountCents: Long,
): String = "$debtor->$creditor:$amountCents"
