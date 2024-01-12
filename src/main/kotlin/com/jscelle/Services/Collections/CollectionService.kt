package com.jscelle.Services.Collections

import com.jscelle.Utilities.UUIDSerializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.util.UUID

class CollectionService(private val connection: Connection) {
    companion object {
        private const val CREATE_TABLE_COLLECTIONS =
            "CREATE TABLE IF NOT EXISTS application.Collections (name VARCHAR(255) PRIMARY KEY, title VARCHAR(255) NOT NULL);"
        private const val CREATE_TABLE_COLLECTION_ITEMS =
            "CREATE TABLE IF NOT EXISTS application.CollectionItems (collection_name VARCHAR(255), item_id UUID, PRIMARY KEY (collection_name, item_id), FOREIGN KEY (collection_name) REFERENCES application.Collections(name) ON DELETE CASCADE ON UPDATE CASCADE, FOREIGN KEY (item_id) REFERENCES application.Items(item_id) ON DELETE CASCADE ON UPDATE CASCADE);"
        private const val SELECT_COLLECTIONS =
            "SELECT name, title FROM application.Collections;"
        private const val SELECT_ITEMS_BY_COLLECTION =
            "SELECT i.item_id, i.title, i.description, i.price, i.currency FROM application.CollectionItems ci JOIN application.Items i ON ci.item_id = i.item_id WHERE ci.collection_name = ?;"
        private const val INSERT_COLLECTION =
            "INSERT INTO application.Collections (name, title) VALUES (?, ?);"
        private const val INSERT_ITEM =
            "INSERT INTO application.Items (item_id, title, description, price, currency) VALUES (?, ?, ?, ?, ?);"
        private const val ADD_ITEM_TO_COLLECTION =
            "INSERT INTO application.CollectionItems (collection_name, item_id) VALUES (?, ?);"
        private const val UPDATE_COLLECTION_TITLE =
            "UPDATE application.Collections SET title = ? WHERE name = ?;"
    }

    init {
        val statement = connection.createStatement()
        statement.executeUpdate(CREATE_TABLE_COLLECTIONS)
        statement.executeUpdate(CREATE_TABLE_COLLECTION_ITEMS)
    }

    // Function to get all collections
    suspend fun getCollections(): List<Collection> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_COLLECTIONS)
        val resultSet = statement.executeQuery()

        val collections = mutableListOf<Collection>()
        while (resultSet.next()) {
            val name = resultSet.getString("name")
            val title = resultSet.getString("title")
            collections.add(Collection(name, title))
        }

        return@withContext collections
    }

    // Function to get items by collection ID
    suspend fun getItemsByCollectionId(collectionId: String): List<Item> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_ITEMS_BY_COLLECTION)
        statement.setString(1, collectionId)
        val resultSet = statement.executeQuery()

        val items = mutableListOf<Item>()
        while (resultSet.next()) {
            val itemId = UUID.fromString(resultSet.getString("item_id"))
            val title = resultSet.getString("title")
            val description = resultSet.getString("description")
            val price = resultSet.getDouble("price")
            val currency = resultSet.getString("currency")
            items.add(Item(itemId, title, description, price, currency))
        }

        return@withContext items
    }

    suspend fun createCollection(name: String, title: String): Collection = withContext(Dispatchers.IO) {

        val statement = connection.prepareStatement(INSERT_COLLECTION)
        statement.setString(1, name)
        statement.setString(2, title)
        statement.executeUpdate()

        return@withContext Collection(name, title)
    }

    // Function to create a new item
    suspend fun createItem(title: String, description: String, price: Double, currency: String): Item = withContext(Dispatchers.IO) {
        val itemId = UUID.randomUUID()

        val statement = connection.prepareStatement(INSERT_ITEM)
        statement.setObject(1, itemId)
        statement.setString(2, title)
        statement.setString(3, description)
        statement.setDouble(4, price)
        statement.setString(5, currency)
        statement.executeUpdate()

        return@withContext Item(itemId, title, description, price, currency)
    }

    // Function to add an item to a collection
    suspend fun addItemToCollection(collectionName: String, itemId: UUID) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(ADD_ITEM_TO_COLLECTION)
        statement.setString(1, collectionName)
        statement.setObject(2, itemId)
        statement.executeUpdate()
    }

    // Function to update collection title
    suspend fun updateCollectionTitle(name: String, newTitle: String) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_COLLECTION_TITLE)
        statement.setString(1, newTitle)
        statement.setString(2, name)
        statement.executeUpdate()
    }
}
