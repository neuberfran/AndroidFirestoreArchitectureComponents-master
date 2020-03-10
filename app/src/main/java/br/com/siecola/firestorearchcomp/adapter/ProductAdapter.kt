package br.com.siecola.firestorearchcomp.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import br.com.siecola.firestorearchcomp.R
import br.com.siecola.firestorearchcomp.model.Product

class ProductAdapter(private val listener: ProductAdapter.OnProductSelectedListener?) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    private var products: List<Product>? = null

    interface OnProductSelectedListener {
        fun onProductSelected(productId: String)
        fun onProductLongSelected(product: Product): Boolean
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtProductListItemName: TextView
        var txtProductListItemCode: TextView
        var txtProductListItemPrice: TextView

        init {
            txtProductListItemName = itemView.findViewById(R.id.txtProductListItemName)
            txtProductListItemCode = itemView.findViewById(R.id.txtProductListItemCode)
            txtProductListItemPrice = itemView.findViewById(R.id.txtProductListItemPrice)
        }
    }

    fun setProducts(products: List<Product>) {
        this.products = products
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup ,
                                    viewType: Int): ProductAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val inflate = inflater.inflate(R.layout.item_product , parent , false)

        return ProductAdapter.ViewHolder(inflate)
    }

    override fun onBindViewHolder(holder: ProductAdapter.ViewHolder , position: Int) {
        if (this.products != null) {
            val product = this.products!![position]

            holder.txtProductListItemName.text = product.name
            holder.txtProductListItemCode.text = product.code
            holder.txtProductListItemPrice.text = String.format("$ %s" , product.price)

            holder.itemView.setOnClickListener { view ->
                listener?.onProductSelected(product.id)
            }

            holder.itemView.setOnLongClickListener { view -> listener == null || listener.onProductLongSelected(product) }
        }
    }

    override fun getItemCount(): Int {
        return if (this.products != null) {
            this.products!!.size
        } else {
            0
        }
    }

}
