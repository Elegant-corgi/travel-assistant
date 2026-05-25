package com.billapp.data

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import kotlinx.serialization.Serializable

@Serializable
data class BillEntry(
    val id: String,
    val amountCents: Long,
    val type: BillType = BillType.Expense,
    val category: String,
    val note: String = "",
    val dateIso: String,
    val createdAt: Long,
    val updatedAt: Long,
)

data class BillDraft(
    val id: String? = null,
    val amountText: String = "",
    val type: BillType = BillType.Expense,
    val category: String = "",
    val note: String = "",
    val date: LocalDate = LocalDate.now(),
)

@Serializable
enum class BillType(val label: String) {
    Expense("支出"),
    Income("收入"),
}

enum class StatRange(val label: String) {
    Week("周"),
    Month("月"),
    Year("年"),
}

data class StatPoint(
    val label: String,
    val amountCents: Long,
)

data class CategoryAmount(
    val category: String,
    val amountCents: Long,
)

data class BillStats(
    val range: StatRange,
    val title: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val totalCents: Long,
    val incomeCents: Long,
    val netBalanceCents: Long,
    val averagePerDayCents: Long,
    val totalCount: Int,
    val points: List<StatPoint>,
    val categories: List<CategoryAmount>,
)

val defaultCategories = listOf(
    "\u9910\u996e",
    "\u852c\u83dc",
    "\u6c34\u679c",
    "\u4ea4\u901a",
    "\u8d2d\u7269",
    "\u96f6\u98df",
    "\u751f\u6d3b\u7f34\u8d39",
    "\u670d\u88c5",
    "\u7f8e\u5bb9",
    "\u8fd0\u52a8",
    "\u4f4f\u623f",
    "\u5a31\u4e50",
    "\u901a\u8baf",
    "\u8f6c\u8d26",
    "\u533b\u7597",
    "\u5b66\u4e60",
    "\u65e5\u7528",
    "\u65c5\u884c",
    "\u996e\u54c1",
    "\u6570\u7801",
    "\u7f51\u7edc\u865a\u62df",
    "\u529e\u516c",
    "\u5b69\u5b50",
    "\u957f\u8f88",
    "\u5ba0\u7269",
    "\u9c9c\u82b1",
    "\u793e\u4ea4",
    "\u8ffd\u661f",
    "\u4eb2\u53cb",
    "\u9996\u9970",
    "\u5176\u4ed6",
)

private const val RECENT_CATEGORY_WINDOW = 60
private val FULL_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月dd日")

fun BillDraft.toEntry(previousCreatedAt: Long = System.currentTimeMillis()): BillEntry {
    val now = System.currentTimeMillis()
    return BillEntry(
        id = id ?: java.util.UUID.randomUUID().toString(),
        amountCents = parseMoneyToCents(amountText) ?: 0L,
        type = type,
        category = category.ifBlank { defaultCategories.first() },
        note = note.trim(),
        dateIso = date.toString(),
        createdAt = previousCreatedAt,
        updatedAt = now,
    )
}

fun BillEntry.toDraft(): BillDraft = BillDraft(
    id = id,
    amountText = BigDecimal(amountCents)
        .movePointLeft(2)
        .setScale(2, RoundingMode.UNNECESSARY)
        .toPlainString(),
    type = type,
    category = category,
    note = note,
    date = LocalDate.parse(dateIso),
)

fun formatMoney(cents: Long): String {
    val amount = BigDecimal(cents).movePointLeft(2).setScale(2, RoundingMode.HALF_UP)
    return "\u00a5${amount.toPlainString()}"
}

fun sortCategoriesByUsage(
    entries: List<BillEntry>,
    baseCategories: List<String> = defaultCategories,
): List<String> {
    if (entries.isEmpty()) return baseCategories

    val defaultOrder = baseCategories.withIndex().associate { it.value to it.index }
    val scores = mutableMapOf<String, Int>()

    entries
        .sortedByDescending { it.createdAt }
        .take(RECENT_CATEGORY_WINDOW)
        .forEachIndexed { index, entry ->
            val category = entry.category.trim()
            if (category !in defaultOrder) return@forEachIndexed

            val recencyWeight = RECENT_CATEGORY_WINDOW - index
            scores[category] = scores.getOrDefault(category, 0) + recencyWeight
        }

    return baseCategories.sortedWith(
        compareByDescending<String> { scores[it] ?: 0 }
            .thenBy { defaultOrder[it] ?: Int.MAX_VALUE },
    )
}

fun formatDraftAmount(amountText: String): String = amountText.ifBlank { "0.00" }

fun appendAmountInput(current: String, token: String): String {
    var result = current
    token.forEach { char ->
        result = appendAmountChar(result, char)
    }
    return result
}

fun removeLastAmountInput(current: String): String = current.dropLast(1)

private fun appendAmountChar(current: String, char: Char): String {
    val normalized = current.trim()
    return when {
        char == '.' -> when {
            normalized.isBlank() -> "0."
            normalized.contains('.') -> normalized
            else -> "$normalized."
        }

        !char.isDigit() -> normalized
        normalized == "0" -> char.toString()
        else -> {
            val candidate = normalized + char
            val decimalPart = candidate.substringAfter('.', "")
            if (candidate.contains('.') && decimalPart.length > 2) {
                normalized
            } else {
                candidate
            }
        }
    }
}

fun parseMoneyToCents(input: String): Long? {
    val normalized = input.trim().replace(",", ".")
    if (normalized.isBlank()) return null
    val amount = normalized.toBigDecimalOrNull() ?: return null
    if (amount < BigDecimal.ZERO) return null
    return try {
        amount.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact()
    } catch (_: ArithmeticException) {
        null
    }
}

fun buildBillStats(
    entries: List<BillEntry>,
    range: StatRange,
    today: LocalDate = LocalDate.now(),
): BillStats {
    val window = when (range) {
        StatRange.Week -> {
            val start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val end = start.plusDays(6)
            StatWindow(
                startDate = start,
                endDate = end,
                title = "${start.format(FULL_DATE_FORMATTER)}-${end.format(FULL_DATE_FORMATTER)}",
            )
        }

        StatRange.Month -> {
            val month = YearMonth.from(today)
            val start = month.atDay(1)
            val end = today
            StatWindow(
                startDate = start,
                endDate = end,
                title = "${month.year}年${month.monthValue}月",
            )
        }

        StatRange.Year -> {
            val start = LocalDate.of(today.year, 1, 1)
            val end = today
            StatWindow(
                startDate = start,
                endDate = end,
                title = "${today.year}年",
            )
        }
    }

    val parsedEntries = entries.map { entry -> entry to LocalDate.parse(entry.dateIso) }
    val inRange = parsedEntries.filter { (_, date) ->
        !date.isBefore(window.startDate) && !date.isAfter(window.endDate)
    }

    val dailyDates = generateSequence(window.startDate) { current ->
        current.plusDays(1)
    }.takeWhile { !it.isAfter(window.endDate) }
        .toList()

    val expenseEntries = inRange.filter { it.first.type == BillType.Expense }
    val incomeEntries = inRange.filter { it.first.type == BillType.Income }

    val points = when (range) {
        StatRange.Year -> buildMonthlyPoints(expenseEntries, window.startDate, window.endDate)
        StatRange.Week, StatRange.Month -> buildDailyPoints(expenseEntries, dailyDates, range)
    }

    val totalCents = expenseEntries.sumOf { it.first.amountCents }
    val incomeCents = incomeEntries.sumOf { it.first.amountCents }
    val dayCount = dailyDates.size.coerceAtLeast(1).toLong()

    val categoryAmounts = expenseEntries
        .groupBy { it.first.category.ifBlank { "其他" } }
        .map { (category, items) ->
            CategoryAmount(
                category = category,
                amountCents = items.sumOf { item -> item.first.amountCents },
            )
        }
        .sortedByDescending { it.amountCents }

    return BillStats(
        range = range,
        title = window.title,
        startDate = window.startDate,
        endDate = window.endDate,
        totalCents = totalCents,
        incomeCents = incomeCents,
        netBalanceCents = incomeCents - totalCents,
        averagePerDayCents = totalCents / dayCount,
        totalCount = inRange.size,
        points = points,
        categories = categoryAmounts,
    )
}

private data class StatWindow(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val title: String,
)

private fun buildDailyPoints(
    entries: List<Pair<BillEntry, LocalDate>>,
    dates: List<LocalDate>,
    range: StatRange,
) : List<StatPoint> {
    val totalsByDate = dates.associateWith { 0L }.toMutableMap()

    entries.forEach { (entry, date) ->
        totalsByDate[date] = (totalsByDate[date] ?: 0L) + entry.amountCents
    }

    return dates.map { date ->
        StatPoint(
            label = when (range) {
                StatRange.Week -> formatWeekday(date)
                StatRange.Month -> date.dayOfMonth.toString().padStart(2, '0')
                StatRange.Year -> date.dayOfMonth.toString().padStart(2, '0')
            },
            amountCents = totalsByDate[date] ?: 0L,
        )
    }
}

private fun buildMonthlyPoints(
    entries: List<Pair<BillEntry, LocalDate>>,
    startDate: LocalDate,
    endDate: LocalDate,
) : List<StatPoint> {
    val startMonth = YearMonth.from(startDate)
    val endMonth = YearMonth.from(endDate)
    val months = generateSequence(startMonth) { current ->
        current.plusMonths(1)
    }.takeWhile { !it.isAfter(endMonth) }
        .toList()
    val totalsByMonth = months.associateWith { 0L }.toMutableMap()

    entries.forEach { (entry, date) ->
        val month = YearMonth.from(date)
        totalsByMonth[month] = (totalsByMonth[month] ?: 0L) + entry.amountCents
    }

    return months.map { month ->
        StatPoint(
            label = month.monthValue.toString().padStart(2, '0'),
            amountCents = totalsByMonth[month] ?: 0L,
        )
    }
}

private fun formatWeekday(date: LocalDate): String = when (date.dayOfWeek.value) {
    1 -> "周一"
    2 -> "周二"
    3 -> "周三"
    4 -> "周四"
    5 -> "周五"
    6 -> "周六"
    else -> "周日"
}
