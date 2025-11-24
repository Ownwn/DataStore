package com.ownwn.datastore

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*


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

    @DeleteMapping("/delete")
    fun deleteEntry(@RequestParam("created") created: String?, @RequestParam("id") id: Int?): ResponseEntity<String> {
        if (created?.toLongOrNull() == null || id == null) {
            return ResponseEntity.badRequest().build()
        }
        if (Database.deleteEntry(created.toLong(), id)) {
            return ResponseEntity.ok().build()
        }
        return ResponseEntity.notFound().build()
    }

    @GetMapping("/downloadfile")
    fun downloadFile(@RequestParam("created") created: String?, @RequestParam("filename") fileNameBase64: String?): ResponseEntity<String> {
        if (created?.toLongOrNull() == null || fileNameBase64 == null) {
            return ResponseEntity.badRequest().build()
        }

        val fileBytes = Database.getFileBytes(created.toLong(), Base64.getDecoder().decode(fileNameBase64).decodeToString())
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(fileBytes.trim())
    }
    @GetMapping("/clearcookie")
    fun clearCookies(): String {
        return """
            <!DOCTYPE html>
            <html>
            <body>
            <button style="width: 300px; height: 100px;" type="button" onclick="clearCookie()">clear cookies</button>
            </body>
            
            <script>
            function clearCookie() {
                document.cookie = "encodedEncryption=;"
            }
            
            
            </script>
            
            </html>
            
            
            
        """.trimIndent()
    }
}
