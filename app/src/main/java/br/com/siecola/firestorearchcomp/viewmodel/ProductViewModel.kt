package br.com.siecola.firestorearchcomp.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel

import br.com.siecola.firestorearchcomp.model.Product
import br.com.siecola.firestorearchcomp.repository.ProductRepository

class ProductViewModel : ViewModel() {
    private var product: MutableLiveData<Product>? = null
    private var products: MutableLiveData<List<Product>>? = null

    val allProducts: MutableLiveData<List<Product>>
        get() {
            if (products == null) {
                products = ProductRepository.getInstance().products
            }
            return products
        }

    fun getProductById(productId: String): MutableLiveData<Product> {
        if (product == null) {
            product = ProductRepository.getInstance().getProductById(productId)
        }
        return product
    }

    fun clearProducts() {
        products = null
    }
}
