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

        val content = file.inputStream.reader().use { it.readText() }
        filePath.writeText(content)

    }

    fun getEntries(): List<Entry> {
       return getFiles().map { f ->
           if (!f.name.startsWith("TEXT") && !f.name.startsWith("FILE")) {
               return@map Entry("Unknown", true, "", 999)
           }
           val plainText = f.name.startsWith("TEXT")
           if (plainText) {
               return@map Entry("Plaintext", true, f.readText(), f.name.substring(4).toLong())
           }

           val parts = f.name.split("--BORDER--")
           if (parts.size != 2) {
               System.err.println("Error parsing file name")
               return@map Entry("Error", true, "", 999)
           }

           val time = parts[0].substring(4).toLong()
           val fileName = parts[1]

           Entry(fileName, false, f.readText(), time)
       }
    }

    private fun getFiles(): List<File> {
        return dataRoot.listFiles()?.toList() ?: throw RuntimeException("Error listing files!")

    }
}

data class Entry(val name: String, val plainText: Boolean, val content: String, val createdAt: Long)