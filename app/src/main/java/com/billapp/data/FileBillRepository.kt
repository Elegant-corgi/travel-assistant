package com.billapp.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File

class FileBillRepository(context: Context) : BillRepository {
    private val file = File(context.filesDir, "bills.json")
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private val _bills = MutableStateFlow(load())
    override val bills: StateFlow<List<BillEntry>> = _bills.asStateFlow()

    override suspend fun upsert(entry: BillEntry) {
        withContext(Dispatchers.IO) {
            val updated = _bills.value.toMutableList()
            val index = updated.indexOfFirst { it.id == entry.id }
            if (index >= 0) {
                updated[index] = entry
            } else {
                updated.add(entry)
            }
            _bills.value = updated.sortedWith(entryComparator())
            save(_bills.value)
        }
    }

    override suspend fun delete(id: String) {
        withContext(Dispatchers.IO) {
            val updated = _bills.value.filterNot { it.id == id }.sortedWith(entryComparator())
            _bills.value = updated
            save(updated)
        }
    }

    private fun load(): List<BillEntry> {
        if (!file.exists()) return emptyList()
        return runCatching {
            json.decodeFromString(ListSerializer(BillEntry.serializer()), file.readText())
        }.getOrDefault(emptyList()).sortedWith(entryComparator())
    }

    private fun save(entries: List<BillEntry>) {
        file.writeText(json.encodeToString(ListSerializer(BillEntry.serializer()), entries))
    }

    private fun entryComparator(): Comparator<BillEntry> = compareByDescending<BillEntry> {
        it.dateIso
    }.thenByDescending {
        it.updatedAt
    }
}
