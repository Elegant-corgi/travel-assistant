package com.billapp.data

import kotlinx.coroutines.flow.StateFlow

interface TravelRepository {
    val workspace: StateFlow<TravelWorkspace>

    suspend fun update(workspace: TravelWorkspace)
}
