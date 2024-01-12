package com.jscelle.Routing

import com.jscelle.Services.Purchases.PurchaseRequest
import com.jscelle.Services.Purchases.PurchaseService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.Connection
import java.util.*

fun Application.purchaseRouting(connection: Connection) {

    val purchaseService = PurchaseService(connection)

    routing {
        // Make a Purchase
        post("/purchase") {
            try {
                val callContent = call.receive<PurchaseRequest>()
                val username = callContent.name
                val itemId = UUID.fromString(callContent.itemId)
                val count = callContent.count

                purchaseService.makePurchase(username, itemId, count)
                call.respond(HttpStatusCode.Created, "Purchase successful")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid purchase request")
            }
        }

        // Get User Purchases
        get("/purchases/{username}") {
            try {
                val username = call.parameters["username"]
                    ?: throw IllegalArgumentException("Invalid username parameter")

                val purchases = purchaseService.getUserPurchases(username)
                call.respond(HttpStatusCode.OK, purchases)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request")
            }
        }
    }
}
