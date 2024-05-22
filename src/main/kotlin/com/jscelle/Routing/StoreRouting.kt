package com.jscelle.Routing

import Store
import com.jscelle.Services.StoreService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.Connection
import java.util.*

fun Application.storeRouting(connection: Connection) {
    val storeService = StoreService(connection)

    routing {

            get ("/stores") {
                call.respond(storeService.getStores())
            }

            post("/stores") {
                val store = call.receive<Store>()
                storeService.addStore(store)
                call.respond(HttpStatusCode.Created, store)
            }

            get("/stores/{storeName}") {
                val storeName = call.parameters["storeName"] ?: return@get call.respondText("Missing store name", status = HttpStatusCode.BadRequest)
                val store = storeService.getStoreByName(storeName) ?: return@get call.respondText("Store not found", status = HttpStatusCode.NotFound)
                call.respond(store)
            }

            post("stores/{storeName}/addSeller") {
                val storeName = call.parameters["storeName"] ?: return@post call.respondText("Missing store name", status = HttpStatusCode.BadRequest)
                val request = call.receive<Map<String, String>>()
                val sellerName = request["sellerName"] ?: return@post call.respondText("Missing seller name", status = HttpStatusCode.BadRequest)
                storeService.addSellerToStore(storeName, sellerName)
                call.respondText("Seller added to store successfully")
            }

        get("/stores/{storeName}/collections") {
            val storeName = call.parameters["storeName"] ?: return@get call.respondText("Missing store name", status = HttpStatusCode.BadRequest)
            val store = storeService.getCollections(storeName)
            call.respond(store)
        }
    }
}