package com.ownwn.datastore

import com.ownwn.datastore.Env2.loadEnv
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.nio.file.Files
import java.nio.file.Path

@SpringBootApplication
class DataStoreApplication {
    companion object {
        fun getEnv(key: String): String? {
            return Env2.envs[key]
        }
    }
}

fun main(args: Array<String>) {
    loadEnv()
    runApplication<DataStoreApplication>(*args)
}

private object Env2 {
    private val envPath = Path.of(".env")
    val envs = mutableMapOf<String, String>()

    fun loadEnv() {
        if (!Files.exists(envPath)) {
            throw RuntimeException("Missing .env at ${envPath.toAbsolutePath()}")
        }

        val lines = Files.readAllLines(envPath)
        for (line in lines) {
            if (!line.matches(Regex("^[^=]+=[^=]+$"))) { // foo=bar
                throw RuntimeException("Bad env line $line")
            }

            val pair = line.split("=")
            if (envs.contains(pair[0])) {
                throw RuntimeException("Duplicate key ${pair[0]}")
            }
            envs[pair[0]] = pair[1]
        }
    }
}
