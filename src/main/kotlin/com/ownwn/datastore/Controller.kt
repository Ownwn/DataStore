package com.ownwn.datastore

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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
}
