package com.ownwn.datastore

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import java.nio.file.Files

object Database {
    val mapper = jacksonObjectMapper()
    val dataRoot = File("data/")
    val dbLocation = dataRoot.resolve("data.json")

    init {
        if (!dataRoot.exists()) dataRoot.mkdir()
        dbLocation.createNewFile()

        val entry = Entry("first entry", System.currentTimeMillis()-1L)
        saveEntry(entry)

        val data = Data(mutableListOf(entry))
        saveData(data)

    }

    private fun getLogFile(): File {
        val file = dataRoot.resolve("logs/").resolve(System.currentTimeMillis().toString() + ".json")
        file.parentFile.mkdirs()
        file.createNewFile()
        return file
    }

    private fun saveData(data: Data) {
        mapper.writeValue(dbLocation, data)
    }

    private fun saveEntry(entry: Entry) {
        mapper.writeValue(getLogFile(), entry)
    }

    fun addEntry(content: String) {
        val entry = Entry(content, System.currentTimeMillis())
        val data = getData()
        data.list.add(entry);
        saveEntry(entry)
        saveData(data)
    }

    fun getEntries(): List<Entry> {
        return getData().list

    }

    private fun getData(): Data {
        return mapper.readValue<Data>(Files.readString(dbLocation.toPath()))

    }
}

data class Data(val list: MutableList<Entry>)
data class Entry(val content: String, val created_at: Long)