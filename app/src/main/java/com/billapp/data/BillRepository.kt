package com.billapp.data

import kotlinx.coroutines.flow.StateFlow

interface BillRepository {
    val bills: StateFlow<List<BillEntry>>
    val monthlyBudgetText: StateFlow<String>

    suspend fun upsert(entry: BillEntry)

    suspend fun upsertAll(entries: List<BillEntry>)

    suspend fun delete(id: String)

    suspend fun updateMonthlyBudget(budgetText: String)
}
