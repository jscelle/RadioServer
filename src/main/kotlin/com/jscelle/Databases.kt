package com.jscelle

import com.jscelle.Routing.itemsRouting
import com.jscelle.Routing.purchaseRouting
import com.jscelle.Routing.userRouting
import io.ktor.server.application.*
import java.sql.*

fun Application.configureDatabases() {
    val dbConnection: Connection = connectToPostgres()

    userRouting(dbConnection)
    itemsRouting(dbConnection)
    purchaseRouting(dbConnection)
}

fun Application.connectToPostgres(): Connection {
    Class.forName("org.postgresql.Driver")
    return DriverManager.getConnection(
        Secret.DB_URL,
        Secret.USER,
        Secret.PASS
    )
}