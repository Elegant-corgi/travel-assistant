package com.billapp.data

import android.content.Context
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class FileTravelRepository(context: Context) : TravelRepository {
    private val file = File(context.filesDir, "travel.json")
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val _workspace = MutableStateFlow(load())
    override val workspace: StateFlow<TravelWorkspace> = _workspace.asStateFlow()

    override suspend fun update(workspace: TravelWorkspace) {
        withContext(Dispatchers.IO) {
            _workspace.value = workspace
            save(workspace)
        }
    }

    private fun load(): TravelWorkspace {
        if (!file.exists()) return sampleTravelWorkspace()
        val raw = runCatching { file.readText() }.getOrNull() ?: return sampleTravelWorkspace()
        return runCatching {
            json.decodeFromString(TravelWorkspace.serializer(), raw)
        }.recoverCatching {
            val legacyTrip = json.decodeFromString(TravelTrip.serializer(), raw)
            TravelWorkspace(
                trips = listOf(legacyTrip),
                selectedTripId = legacyTrip.id,
            )
        }.getOrDefault(sampleTravelWorkspace())
    }

    private fun save(workspace: TravelWorkspace) {
        file.writeText(json.encodeToString(TravelWorkspace.serializer(), workspace))
    }
}
