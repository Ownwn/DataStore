package com.ownwn.datastore

import com.ownwn.server.Handle
import com.ownwn.server.HttpMethod
import com.ownwn.server.Request
import com.ownwn.server.response.Response
import com.ownwn.server.response.TemplateResponse
import com.ownwn.server.response.WholeBodyResponse
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile
import java.util.*


class Controller {

    @Handle("api/entries")
    fun entries(request: Request): Response {
        return WholeBodyResponse.json(Database.getEntries())
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

    @Handle("/delete", method = HttpMethod.DELETE)
    fun deleteEntry(request: Request): Response {
        val created = request.queryParameters()!!["created"]?.toLongOrNull()
        val id = request.queryParameters()!!["id"]?.toIntOrNull()

        if (created == null || id == null) {
            return WholeBodyResponse.badRequest()
        }
        if (Database.deleteEntry(created, id)) {
            return WholeBodyResponse.ok()
        }
        return WholeBodyResponse.notFound
    }

    @Handle("/downloadfile")
    fun downloadFile(request: Request): Response {
        val created = request.queryParameters()["created"]?.toLongOrNull()
        val fileNameBase64 = request.queryParameters()["filename"]
        if (created == null || fileNameBase64 == null) {
            return WholeBodyResponse.badRequest()
        }

        val fileBytes = Database.getFileBytes(created, Base64.getDecoder().decode(fileNameBase64).decodeToString())
            ?: return WholeBodyResponse.notFound

        return WholeBodyResponse.ok(fileBytes)
    }

    @Handle("clearcookie")
    fun clearCookies(request: Request): Response {
        return TemplateResponse.of("clearcookie")
    }
}
