package com.ownwn.datastore

import org.springframework.web.multipart.MultipartFile
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
        val file = dataRoot.resolve("TEXT${System.currentTimeMillis()}")
        if (file.exists()) throw RuntimeException("File $file already exists!")
        file.writeText(content)

    }

    fun addEntry(file: MultipartFile) {
        val fileName = "FILE${System.currentTimeMillis()}--BORDER--${file.originalFilename}"
        val filePath = dataRoot.resolve(fileName)
        if (filePath.exists()) throw RuntimeException("File $filePath already exists!")
        file.inputStream.use {
            filePath.outputStream().use { outputStream -> it.copyTo(outputStream) }
        }
    }

    fun getEntries(): List<Entry> {
       return getFiles().map {
           Entry.createEntry(it) ?: Entry("Error", true, "", 999)
       }
    }

    fun getFileAndName(time: Long): Pair<File, String>? {
        return dataRoot.listFiles()?.firstOrNull {
            Entry.createEntry(it)?.createdAt == time
        }?.let { Pair(it, it.name.split("--BORDER--")[1]) }
    }

    private fun getFiles(): List<File> {
        return dataRoot.listFiles()?.toList() ?: listOf()
    }
}

data class Entry(val name: String, val plainText: Boolean, val content: String, val createdAt: Long) {
    companion object {
        fun createEntry(f: File): Entry? {
            if (!f.name.startsWith("TEXT") && !f.name.startsWith("FILE")) {
                return null
            }
            val plainText = f.name.startsWith("TEXT")
            if (plainText) {
                return Entry("Plaintext", true, f.readText(), f.name.substring(4).toLong())
            }

            val parts = f.name.split("--BORDER--")
            if (parts.size != 2) {
                return null
            }

            val time = parts[0].substring(4).toLong()
            val fileName = parts[1]

            return Entry(fileName, false, "FILE", time)
        }
    }
}