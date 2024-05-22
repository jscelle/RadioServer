package com.jscelle.Routing

import com.jscelle.Services.Collections.CollectionService
import com.jscelle.Services.Collections.CreateCollectionRequest
import com.jscelle.Services.Collections.CreateItemRequest
import com.jscelle.Services.Collections.UpdateCollectionTitleRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.Connection
import java.util.*

fun Application.itemsRouting(connection: Connection) {

    val collectionService = CollectionService(connection)

    routing {
        // Get all collections
        get("/collections") {
            val collections = collectionService.getCollections()
            call.respond(HttpStatusCode.OK, collections)
        }

        get("/collections/{collectionId}") {
            val collectionId = call.parameters["collectionId"]
                ?: throw IllegalArgumentException("Invalid Collection ID")
            val collections = collectionService.getCollectionByName(collectionId)
            call.respond(HttpStatusCode.OK, collections)
        }

        // Get items by collection ID
        get("/items/{collectionId}") {
            val collectionId = call.parameters["collectionId"]
                ?: throw IllegalArgumentException("Invalid Collection ID")
            val items = collectionService.getItemsByCollectionId(collectionId)
            call.respond(HttpStatusCode.OK, items)
        }

        // Create a new collection
        post("/collections") {
            try {
                val request = call.receive<CreateCollectionRequest>()
                val collection = collectionService.createCollection(request.name, request.title)
                call.respond(HttpStatusCode.Created, collection)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.localizedMessage)
            }
        }

        // Create a new item
        post("/items") {
            try {
                val request = call.receive<CreateItemRequest>()
                val itemId = collectionService.createItem(request.title, request.description, request.price, request.currency)
                call.respond(HttpStatusCode.Created, itemId)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.localizedMessage)
            }
        }

        // Add item to a collection
        post("/collections/{collectionName}/items/{itemId}") {
            try {
                val collectionName = call.parameters["collectionName"]
                    ?: throw IllegalArgumentException("Invalid collection name")
                val itemId = call.parameters["itemId"]?.let { UUID.fromString(it) }
                    ?: throw IllegalArgumentException("Invalid Item ID")
                collectionService.addItemToCollection(collectionName, itemId)
                call.respond(HttpStatusCode.Created, "Item added to collection")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.localizedMessage)
            }
        }

        // Update collection title
        put("/collections/{collectionName}") {
            try {
                val collectionName = call.parameters["collectionName"]
                    ?: throw IllegalArgumentException("Invalid collection name")
                val request = call.receive<UpdateCollectionTitleRequest>()
                collectionService.updateCollectionTitle(collectionName, request.newTitle)
                call.respond(HttpStatusCode.OK, "Collection title updated")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.localizedMessage)
            }
        }
    }
}