package br.com.siecola.firestorearchcomp.repository

import android.arch.lifecycle.MutableLiveData
import android.util.Log

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

import java.util.ArrayList

import br.com.siecola.firestorearchcomp.model.Product

class ProductRepository private constructor() {
    private val mFirestore: FirebaseFirestore

    val products: MutableLiveData<List<Product>>
        get() {
            val liveProducts = MutableLiveData<List<Product>>()

            mFirestore.collection(Product.COLLECTION)
                    .whereEqualTo(Product.FIELD_userId , mFirebaseAuth!!.uid)
                    .orderBy(Product.FIELD_name , Query.Direction.ASCENDING)
                    .addSnapshotListener { snapshot , e ->
                        if (e != null) {
                            Log.w(TAG , "Listen failed." , e)
                            return@mFirestore.collection(Product.Companion.getCOLLECTION())
                                    .whereEqualTo(Product.Companion.getFIELD_userId() , mFirebaseAuth.getUid())
                                    .orderBy(Product.Companion.getFIELD_name() , Query.Direction.ASCENDING)
                                    .addSnapshotListener
                        }

                        val products = ArrayList<Product>()
                        if (snapshot != null && !snapshot.isEmpty) {
                            for (documentSnapshot in snapshot.documents) {
                                val product = documentSnapshot.toObject(Product::class.java)
                                product!!.id = documentSnapshot.id
                                products.add(product)
                            }
                        }
                        liveProducts.postValue(products)
                    }

            return liveProducts
        }

    init {
        mFirestore = FirebaseFirestore.getInstance()
    }

    fun getProductById(productId: String): MutableLiveData<Product> {
        val liveProject = MutableLiveData<Product>()

        val docRef = mFirestore.collection(Product.COLLECTION).document(productId)
        docRef.addSnapshotListener { snapshot , e ->
            if (e != null) {
                Log.w(TAG , "Listen failed." , e)
                return@docRef.addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val product = snapshot.toObject(Product::class.java)
                product!!.id = snapshot.id
                liveProject.postValue(product)
            } else {
                Log.d(TAG , "Current data: null")
            }
        }

        return liveProject
    }

    fun saveProduct(product: Product): String {
        val document: DocumentReference
        if (product.id != null) {
            document = mFirestore.collection(Product.COLLECTION).document(product.id!!)
        } else {
            product.userId = mFirebaseAuth!!.uid
            document = mFirestore.collection(Product.COLLECTION).document()
        }
        document.set(product)

        return document.id
    }

    fun deleteProduct(productId: String) {
        val docRef = mFirestore.collection(Product.COLLECTION).document(productId)
        docRef.delete()
    }

    companion object {
        private val TAG = "ProductRepository"
        private var mFirebaseAuth: FirebaseAuth? = null

        private var instance: ProductRepository? = null

        @Synchronized
        fun getInstance(): ProductRepository {
            if (instance == null) {
                instance = ProductRepository()
                mFirebaseAuth = FirebaseAuth.getInstance()
            }
            return instance
        }
    }
}
