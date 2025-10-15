package com.ownwn.datastore

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile


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
}
