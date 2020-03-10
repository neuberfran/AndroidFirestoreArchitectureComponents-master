package br.com.siecola.firestorearchcomp.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

import java.io.Serializable

@IgnoreExtraProperties
class Product : Serializable {

    @get:Exclude
    var id: String? = null
    var userId: String? = null
    var name: String? = null
    var description: String? = null
    var code: String? = null
    var price: Double = 0.toDouble()

    companion object {
        val COLLECTION = "products"
        val FIELD_userId = "userId"
        val FIELD_name = "name"
        val FIELD_description = "description"
        val FIELD_code = "code"
        val FIELD_price = "price"
    }
}