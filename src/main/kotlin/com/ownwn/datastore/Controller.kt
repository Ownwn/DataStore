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
    fun submit(@RequestBody content: String?): ResponseEntity<String> {
        return content
            ?.let { Database.addEntry(content); ResponseEntity.ok("ok!")}
            ?: ResponseEntity.badRequest().body("Bad content!")
    }

    @PostMapping("/submitfile", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun submitFile(@RequestPart("files") files: List<MultipartFile?>?): ResponseEntity<String> {
        files?.forEach {
            try {
                Database.addEntry(it?: return ResponseEntity.badRequest().body("Bad individual file"))
            } catch (e: Exception) {
                return ResponseEntity.badRequest().body("Error submitting files! $e")
            }
        } ?: ResponseEntity.badRequest().body("Bad file list")

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
