package com.ownwn.datastore

import java.io.File

object Database {
    val dataRoot = File("data/")

    init {
        if (!dataRoot.exists()) dataRoot.mkdir()
        if (dataRoot.listFiles().orEmpty().isEmpty()) {
            addEntry("test")
        }
    }

    fun addEntry(content: String) {
        val file = dataRoot.resolve(System.currentTimeMillis().toString())
        if (file.exists()) throw RuntimeException("File $file already exists!")
        file.writeText(content)

    }

    fun getEntries(): List<Entry> {
       return getFiles().map { f -> Entry(f.readText(), f.name.toLong()) }
    }

    private fun getFiles(): List<File> {
        return dataRoot.listFiles()?.map { f -> f } ?: throw RuntimeException("Error listing files!")

    }
}

data class Entry(val content: String, val createdAt: Long)