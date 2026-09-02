package com.billapp.data

import kotlin.test.Test
import kotlin.test.assertEquals

class TravelExpenseTitleTest {
    @Test
    fun ticketsTitleAppendsTicketSuffix() {
        val title = normalizeTravelExpenseTitle("故宫", TravelExpenseCategory.Tickets)

        assertEquals("故宫门票", title)
    }

    @Test
    fun ticketsTitleDoesNotAppendDuplicateTicketSuffix() {
        val title = normalizeTravelExpenseTitle("故宫门票", TravelExpenseCategory.Tickets)

        assertEquals("故宫门票", title)
    }

    @Test
    fun transportTitlesKeepUserInput() {
        listOf("上海 → 杭州", "加油费", "机场打车").forEach { input ->
            val title = normalizeTravelExpenseTitle(input, TravelExpenseCategory.Transport)

            assertEquals(input, title)
        }
    }

    @Test
    fun blankTitleRemainsBlankAfterNormalization() {
        val title = normalizeTravelExpenseTitle("  ", TravelExpenseCategory.Tickets)

        assertEquals("", title)
    }
}
