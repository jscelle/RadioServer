package com.jscelle.Services.Purchases

import com.jscelle.Services.Collections.Item
import kotlinx.serialization.Serializable

@Serializable
data class Purchase(
    val item: Item,
    val count: Int
)

@Serializable
data class PurchaseRequest(val name: String, val itemId: String, val count: Int)