package com.billapp.data

import kotlinx.coroutines.flow.StateFlow

interface BillRepository {
    val bills: StateFlow<List<BillEntry>>

    suspend fun upsert(entry: BillEntry)

    suspend fun delete(id: String)
}
