package com.github.zinc.mongodb

import com.github.zinc.plugin
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.TransactionBody
import org.bson.Document
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

object MongoDB {
    private const val MONGODB_FILE = "mongodb.yml"

    lateinit var client: MongoClient
        private set
    lateinit var database: MongoDatabase
        private set

    fun register() {
        val yml = File(plugin.dataFolder, MONGODB_FILE)

        if(!yml.exists()) {
            plugin.saveResource(MONGODB_FILE, false)
        }

        YamlConfiguration.loadConfiguration(yml).get("url").let { url ->
            val settings = MongoClientSettings.builder()
                .applyConnectionString(ConnectionString(url as String))
                .build()
            client = MongoClients.create(settings)
        }
        val databaseName = YamlConfiguration.loadConfiguration(yml).get("name") as String
        database = client.getDatabase(databaseName)
    }

    fun transaction(transactionBody: () -> Unit) {
        client.startSession().withTransaction(transactionBody)
    }

    operator fun get(collectionName: String): MongoCollection<Document> = database.getCollection(collectionName)
}