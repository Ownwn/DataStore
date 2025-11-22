package com.ownwn.datastore

import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.FileInputStream


@RestController
@CrossOrigin
class Controller {
    @GetMapping("/entries")
    fun entries(): ResponseEntity<List<Entry>> {
        return ResponseEntity.ok(Database.getEntries())
    }

    @PostMapping("/submit")
    fun submit(@RequestBody items: List<Map<String?, String?>?>?): ResponseEntity<String> {
        if (items == null) return ResponseEntity.badRequest().body("null items")
        println(items)
        for (item in items) {
            if (item.isNullOrEmpty()) continue
            val type: String = item["type"] ?: return ResponseEntity.badRequest().body("missing type")
            val content = item[type] ?: return ResponseEntity.badRequest().body("missing content")
            when (type) {
                "text" -> Database.addEntry(content)
                "file" -> try {
                    Database.addEntry(content, item["filename"])
                } catch (e: Exception) {
                    return ResponseEntity.badRequest().body(e.message)
                }

                else -> return ResponseEntity.badRequest().body("unknown type $type")
            }
        }
        return ResponseEntity.ok("ok!")
    }

    @GetMapping("/downloadfile/{created}")
    fun downloadFile(@PathVariable created: String?): ResponseEntity<Resource> {
        val fileNamePair = created?.toLongOrNull()?.let { Database.getFileAndName(it) } ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileNamePair.second + "\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .contentLength(fileNamePair.first.length())
            .body(InputStreamResource(FileInputStream(fileNamePair.first)));
    }
}
