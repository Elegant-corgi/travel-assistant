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
)

data class AaUiState(
    val open: Boolean = false,
    val draft: AaDraft = defaultAaDraft(),
    val error: String? = null,
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
)

fun defaultAaDraft(): AaDraft = AaDraft()

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
            buildAaSettlementLines(activity).sumOf { line -> line.amountCents }
        },
        memberCount = activities.flatMap { it.members }.distinct().size,
    )
}

fun buildAaSettlementLines(activity: AaActivity): List<AaSettlementLine> {
    val participants = activity.members.distinct()
    if (participants.size < 2 || activity.payer !in participants) return emptyList()

    val baseShare = activity.amountCents / participants.size
    val remainder = (activity.amountCents % participants.size).toInt()

    return participants.mapIndexedNotNull { index, member ->
        val share = baseShare + if (index < remainder) 1 else 0
        if (member == activity.payer || share <= 0L) {
            null
        } else {
            AaSettlementLine(
                debtor = member,
                creditor = activity.payer,
                amountCents = share,
            )
        }
    }
}

fun buildAaCopyText(activity: AaActivity): String {
    val lines = buildAaSettlementLines(activity)
    return buildString {
        appendLine("${activity.title} · ${activity.scene.label}")
        appendLine("总额 ${formatMoney(activity.amountCents)}")
        appendLine("付款人 ${activity.payer}")
        if (activity.note.isNotBlank()) {
            appendLine("备注 ${activity.note}")
        }
        if (lines.isNotEmpty()) {
            appendLine("结算结果")
            lines.forEach { line ->
                appendLine("${line.debtor} → ${line.creditor} ${formatMoney(line.amountCents)}")
            }
        }
    }.trimEnd()
}
