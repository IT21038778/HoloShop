package com.example.holoshop.model

data class Product(
    val name: String,
    val price: String,
    val imageUrl: String,
    val modelUrl: String
){
    constructor() : this(null.toString(), null.toString(), null.toString(), null.toString())
}
