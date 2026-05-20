package com.billapp.data

import android.content.Context
import com.billapp.ui.AaActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File

class FileAaRepository(context: Context) : AaRepository {
    private val file = File(context.filesDir, "aa_activities.json")
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private val _activities = MutableStateFlow(load())
    override val activities: StateFlow<List<AaActivity>> = _activities.asStateFlow()

    override suspend fun upsert(activity: AaActivity) {
        withContext(Dispatchers.IO) {
            val updated = _activities.value.toMutableList()
            val index = updated.indexOfFirst { it.id == activity.id }
            if (index >= 0) {
                updated[index] = activity
            } else {
                updated.add(0, activity)
            }
            _activities.value = updated.sortedWith(activityComparator())
            save(_activities.value)
        }
    }

    override suspend fun delete(id: String) {
        withContext(Dispatchers.IO) {
            val updated = _activities.value.filterNot { it.id == id }.sortedWith(activityComparator())
            _activities.value = updated
            save(updated)
        }
    }

    private fun load(): List<AaActivity> {
        if (!file.exists()) return emptyList()
        return runCatching {
            json.decodeFromString(ListSerializer(AaActivity.serializer()), file.readText())
        }.getOrDefault(emptyList()).sortedWith(activityComparator())
    }

    private fun save(entries: List<AaActivity>) {
        file.writeText(json.encodeToString(ListSerializer(AaActivity.serializer()), entries))
    }

    private fun activityComparator(): Comparator<AaActivity> = compareByDescending<AaActivity> {
        it.createdAt
    }
}
