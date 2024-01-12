package com.jscelle.Services.Collections

import com.jscelle.Utilities.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Item(
    @Serializable(with = UUIDSerializer::class) val itemId: UUID,
    val title: String,
    val description: String,
    val price: Double,
    val currency: String
)


@Serializable
data class Collection(
    val name: String,
    val title: String
)

@Serializable
data class CreateCollectionRequest(val name: String, val title: String)

@Serializable
data class CreateItemRequest(val title: String, val description: String, val price: Double, val currency: String)

@Serializable
data class UpdateCollectionTitleRequest(val newTitle: String)
