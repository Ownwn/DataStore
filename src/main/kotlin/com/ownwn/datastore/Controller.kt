package com.ownwn.datastore

import com.ownwn.server.Handle
import com.ownwn.server.Request
import com.ownwn.server.Response
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*


@RestController
@CrossOrigin
class Controller {

    @Handle("entries")
    fun entries(request: Request): Response {
        return Response.ok(Database.getEntries().toString())
    }

    @PostMapping("/submit", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun submit(@RequestPart("file", required = false) files: List<MultipartFile?>?, @RequestPart("text", required = false) text: String?): ResponseEntity<String> {
        if (files == null && text.isNullOrBlank()) return ResponseEntity.badRequest().body("missing content")
        if (text != null && text.isNotBlank()) {
            Database.addEntry(text)
        }

        if (files.isNullOrEmpty()) {
            return ResponseEntity.ok().build()
        }

        for (file in files) {
            if (file == null || file.isEmpty) continue

            try {
                Database.addEntry(file.bytes, file.originalFilename ?: "unknown name")
            } catch (e: Exception) {
                return ResponseEntity.badRequest().body(e.message)
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
    fun downloadFile(@RequestParam("created") created: String?, @RequestParam("filename") fileNameBase64: String?): ResponseEntity<ByteArray> {
        if (created?.toLongOrNull() == null || fileNameBase64 == null) {
            return ResponseEntity.badRequest().build()
        }

        val fileBytes = Database.getFileBytes(created.toLong(), Base64.getDecoder().decode(fileNameBase64).decodeToString())
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(fileBytes)
    }

    @Handle("clearcookie")
    fun clearCookies(request: Request): Response {
        val html = """
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

        return Response.ok(html)
    }
}
