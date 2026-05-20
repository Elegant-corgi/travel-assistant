package com.billapp.data

import com.billapp.ui.AaActivity
import kotlinx.coroutines.flow.StateFlow

interface AaRepository {
    val activities: StateFlow<List<AaActivity>>

    suspend fun upsert(activity: AaActivity)

    suspend fun delete(id: String)
}
