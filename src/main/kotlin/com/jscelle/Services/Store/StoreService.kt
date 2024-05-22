package com.jscelle.Services

import Store
import UserService
import com.jscelle.Services.Collections.Collection
import com.jscelle.Services.Collections.CollectionService
import com.jscelle.Services.User.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection

class StoreService(private val connection: Connection) {
    companion object {
        private const val CREATE_TABLE_STORES =
            "CREATE TABLE IF NOT EXISTS Stores (name VARCHAR(255) PRIMARY KEY, latitude DOUBLE PRECISION, longitude DOUBLE PRECISION);"
        private const val INSERT_STORE =
            "INSERT INTO Stores (name, latitude, longitude) VALUES (?, ?, ?);"
        private const val SELECT_ALL_STORES =
            "SELECT * FROM Stores;"
        private const val SELECT_STORE_BY_NAME =
            "SELECT * FROM Stores WHERE name = ?;"
        private const val CREATE_TABLE_STORE_SELLERS =
            "CREATE TABLE IF NOT EXISTS StoreSellers (store_name VARCHAR(255), seller_name VARCHAR(255), PRIMARY KEY (store_name, seller_name), FOREIGN KEY (store_name) REFERENCES Stores(name) ON DELETE CASCADE ON UPDATE CASCADE, FOREIGN KEY (seller_name) REFERENCES application.Users(name) ON DELETE CASCADE ON UPDATE CASCADE);"
        private const val INSERT_STORE_SELLER =
            "INSERT INTO StoreSellers (store_name, seller_name) VALUES (?, ?);"
        private const val SELECT_STORE_SELLERS =
            "SELECT seller_name FROM StoreSellers WHERE store_name = ?;"
    }

    init {
        val statement = connection.createStatement()
        statement.executeUpdate(CREATE_TABLE_STORES)
        statement.executeUpdate(CREATE_TABLE_STORE_SELLERS)
    }

    // Function to add a store
    suspend fun addStore(store: Store) = withContext(Dispatchers.IO) {
        connection.autoCommit = false
        try {
            val insertStatement = connection.prepareStatement(INSERT_STORE)
            insertStatement.setString(1, store.name)
            insertStatement.setDouble(2, store.latitude)
            insertStatement.setDouble(3, store.longitude)
            insertStatement.executeUpdate()
            connection.commit()
        } catch (e: Exception) {
            connection.rollback()
            throw e
        } finally {
            connection.autoCommit = true
        }

        println("Добавлен магазин ${store.name}")
    }

    // Function to get all stores
    suspend fun getStores(): List<Store> = withContext(Dispatchers.IO) {
        val selectStatement = connection.prepareStatement(SELECT_ALL_STORES)
        val resultSet = selectStatement.executeQuery()

        val stores = mutableListOf<Store>()
        while (resultSet.next()) {
            val name = resultSet.getString("name")
            val latitude = resultSet.getDouble("latitude")
            val longitude = resultSet.getDouble("longitude")

            stores.add(Store(name, latitude, longitude))
        }

        return@withContext stores
    }

    // Function to get a store by name
    suspend fun getStoreByName(name: String): Store? = withContext(Dispatchers.IO) {
        val selectStatement = connection.prepareStatement(SELECT_STORE_BY_NAME)
        selectStatement.setString(1, name)
        val resultSet = selectStatement.executeQuery()

        if (resultSet.next()) {
            val latitude = resultSet.getDouble("latitude")
            val longitude = resultSet.getDouble("longitude")
            return@withContext Store(name, latitude, longitude)
        }

        return@withContext null
    }

    suspend fun addSellerToStore(storeName: String, sellerName: String) = withContext(Dispatchers.IO) {
        connection.autoCommit = false
        try {
            val insertStatement = connection.prepareStatement(INSERT_STORE_SELLER)
            insertStatement.setString(1, storeName)
            insertStatement.setString(2, sellerName)
            insertStatement.executeUpdate()
            connection.commit()
        } catch (e: Exception) {
            connection.rollback()
            throw e
        } finally {
            connection.autoCommit = true
        }
    }

    suspend fun getSellers(storeName: String): List<User> = withContext(Dispatchers.IO) {
        val selectStatement = connection.prepareStatement(SELECT_STORE_SELLERS)
        selectStatement.setString(1, storeName)
        val resultSet = selectStatement.executeQuery()

        val sellers = mutableListOf<User>()
        while (resultSet.next()) {
            val sellerName = resultSet.getString("seller_name")

            // Assuming User has a constructor that takes a name as a parameter
            sellers.add(
                UserService(connection).getUserByName(sellerName)
            )
        }

        return@withContext sellers
    }

    suspend fun getCollections(storeName: String): List<Collection> = withContext(Dispatchers.IO) {
        val sellers = getSellers(storeName)

        val collectionService = CollectionService(connection)

        val collections = sellers.map {
            collectionService.getCollectionByName(it.name)
        }.flatMap { it }

        return@withContext collections
    }
}