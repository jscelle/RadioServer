package com.jscelle.Services.Purchases

import com.jscelle.Services.Collections.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.util.UUID

class PurchaseService(private val connection: Connection) {
    companion object {
        private const val CREATE_TABLE_PURCHASES =
            "CREATE TABLE IF NOT EXISTS application.Purchases (user_name VARCHAR(255), item_id UUID, count INT, PRIMARY KEY (user_name, item_id), FOREIGN KEY (user_name) REFERENCES application.Users(name) ON DELETE CASCADE ON UPDATE CASCADE, FOREIGN KEY (item_id) REFERENCES application.Items(item_id) ON DELETE CASCADE ON UPDATE CASCADE);"
        private const val INSERT_PURCHASE =
            "INSERT INTO application.Purchases (user_name, item_id, count) VALUES (?, ?, ?);"
        private const val CHECK_PURCHASE =
            "SELECT count FROM application.Purchases WHERE user_name = ? AND item_id = ?;"
        private const val UPDATE_PURCHASE =
            "UPDATE application.Purchases SET count = count + ? WHERE user_name = ? AND item_id = ?;"
        private const val SELECT_USER_PURCHASES =
            "SELECT p.user_name, p.item_id, p.count, i.title, i.description, i.price, i.currency FROM application.Purchases p JOIN application.Items i ON p.item_id = i.item_id WHERE p.user_name = ?;"
    }

    init {
        val statement = connection.createStatement()
        statement.executeUpdate(CREATE_TABLE_PURCHASES)
    }

    // Function to make a purchase
    suspend fun makePurchase(username: String, itemId: UUID, count: Int) = withContext(Dispatchers.IO) {
        connection.autoCommit = false
        try {
            // Check if the purchase already exists
            val checkStatement = connection.prepareStatement(CHECK_PURCHASE)
            checkStatement.setString(1, username)
            checkStatement.setObject(2, itemId)
            val resultSet = checkStatement.executeQuery()

            if (resultSet.next()) {
                // If exists, update the count
                val updateStatement = connection.prepareStatement(UPDATE_PURCHASE)
                updateStatement.setInt(1, count)
                updateStatement.setString(2, username)
                updateStatement.setObject(3, itemId)
                updateStatement.executeUpdate()
            } else {
                // If not, insert a new record
                val insertStatement = connection.prepareStatement(INSERT_PURCHASE)
                insertStatement.setString(1, username)
                insertStatement.setObject(2, itemId)
                insertStatement.setInt(3, count)
                insertStatement.executeUpdate()
            }
            connection.commit()
        } catch (e: Exception) {
            connection.rollback()
            throw e
        } finally {
            connection.autoCommit = true
        }
    }

    // Function to get user purchases
    suspend fun getUserPurchases(username: String): List<Purchase> = withContext(Dispatchers.IO) {
        // Assuming you have a SELECT query to retrieve user purchases from the Purchases and Items tables
        val selectStatement = connection.prepareStatement(SELECT_USER_PURCHASES)
        selectStatement.setString(1, username)
        val resultSet = selectStatement.executeQuery()

        val purchases = mutableListOf<Purchase>()
        while (resultSet.next()) {
            val id = resultSet.getString("item_id").let { UUID.fromString(it) }
            val itemTitle = resultSet.getString("title")
            val itemDescription = resultSet.getString("description")
            val itemPrice = resultSet.getDouble("price")
            val itemCurrency = resultSet.getString("currency")
            val itemCount = resultSet.getInt("count")

            purchases.add(
                Purchase(
                    Item(id, itemTitle, itemDescription, itemPrice, itemCurrency),
                    itemCount
                )
            )
        }

        return@withContext purchases
    }
}
