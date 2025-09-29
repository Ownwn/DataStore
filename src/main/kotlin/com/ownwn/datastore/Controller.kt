package com.ownwn.datastore

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin
class Controller {
    private val entries: MutableList<Entry> = mutableListOf(Entry("first entry", System.currentTimeMillis()), Entry("second one here", System.currentTimeMillis()))

    @GetMapping("/test")
    fun ok(): String {
        return "asdf ok"
    }

    @GetMapping("/entries")
    fun entries(): ResponseEntity<List<Entry>> {
        return ResponseEntity.ok(entries)
    }

    @PostMapping("/submit")
    fun submit(@RequestBody content: String?): ResponseEntity<String> {
        return content
            ?.takeIf { entries.add(Entry(it, System.currentTimeMillis())) }
            ?.let {ResponseEntity.ok("ok!")}
            ?: ResponseEntity.badRequest().body("Bad content!")
    }
}

data class Entry(val content: String, val created_at: Long)