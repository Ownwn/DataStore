package com.ownwn.datastore

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DataStoreApplication

fun main(args: Array<String>) {
    runApplication<DataStoreApplication>(*args)
}
