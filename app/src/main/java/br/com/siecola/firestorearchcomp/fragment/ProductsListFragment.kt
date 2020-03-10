package br.com.siecola.firestorearchcomp.fragment

import android.app.AlertDialog
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast

import br.com.siecola.firestorearchcomp.R
import br.com.siecola.firestorearchcomp.adapter.ProductAdapter
import br.com.siecola.firestorearchcomp.model.Product
import br.com.siecola.firestorearchcomp.repository.ProductRepository
import br.com.siecola.firestorearchcomp.util.GlobalArgs
import br.com.siecola.firestorearchcomp.viewmodel.ProductViewModel

import android.support.v7.widget.RecyclerView.VERTICAL

class ProductsListFragment : Fragment() , ProductAdapter.OnProductSelectedListener {

    private var productAdapter: ProductAdapter? = null
    private var productViewModel: ProductViewModel? = null
    private var actionMode: ActionMode? = null

    private var productSelected: Product? = null

    private val mActionModeCallback = object : ActionMode.Callback {

        override fun onCreateActionMode(mode: ActionMode , menu: Menu): Boolean {
            if (productSelected != null) {
                mode.title = productSelected!!.name
            }
            val inflater = mode.menuInflater
            inflater.inflate(R.menu.products_cab_menu , menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode , menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode , item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.product_edit -> {
                    if (productSelected != null) {
                        startProductDetail(productSelected!!.id)
                    }
                    productSelected = null
                    mode.finish()
                    return true
                }
                R.id.product_delete -> {
                    val builder = AlertDialog.Builder(activity)

                    builder.setMessage(R.string.str_want_delete_product)
                            .setCancelable(true)
                            .setTitle(R.string.str_delete_product)
                            .setNegativeButton(R.string.str_no , null)
                            .setPositiveButton(R.string.str_yes) { dialog , id ->
                                if (productSelected != null) {
                                    ProductRepository.getInstance().deleteProduct(productSelected!!.id)
                                }
                                productSelected = null
                            }
                    val alert = builder.create()
                    alert.show()

                    mode.finish()
                    return true
                }
                else -> return false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            actionMode = null
        }
    }

    override fun onCreateView(inflater: LayoutInflater , container: ViewGroup? ,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_products_list , container , false)
        activity!!.setTitle(R.string.str_products)

        productViewModel = ViewModelProviders.of(this).get(ProductViewModel::class.java)

        val rcvProducts = rootView.findViewById<RecyclerView>(R.id.rcvProducts)
        productAdapter = ProductAdapter(this)
        rcvProducts.layoutManager = LinearLayoutManager(activity)
        rcvProducts.adapter = productAdapter

        val itemDecor = DividerItemDecoration(context!! , VERTICAL)
        rcvProducts.addItemDecoration(itemDecor)

        val fab = rootView.findViewById<FloatingActionButton>(R.id.btnAddProduct)
        fab.setOnClickListener { view -> startProductDetail(null) }

        hideKeyboard()

        return rootView
    }

    override fun onResume() {
        super.onResume()

        productViewModel!!.allProducts.observe(this , { products -> productAdapter!!.setProducts(products) })
    }

    override fun onProductSelected(productId: String) {
        if (actionMode == null) {
            startProductDetail(productId)
        }
    }

    override fun onProductLongSelected(product: Product): Boolean {
        if (actionMode != null) {
            return false
        }

        productSelected = product
        actionMode = activity!!.startActionMode(mActionModeCallback)
        return true
    }

    private fun startProductDetail(productId: String?) {
        val fragmentClass: Class<*>
        var fragment: Fragment? = null

        fragmentClass = ProductFragment::class.java

        try {
            fragment = fragmentClass.newInstance() as Fragment

            if (productId != null && !productId.isEmpty()) {
                val args = Bundle()
                args.putString(GlobalArgs.PRODUCT_ID , productId)
                fragment.arguments = args
            }

            val fragmentManager = fragmentManager
            val transaction = fragmentManager!!.beginTransaction()

            transaction.replace(R.id.container , fragment ,
                    ProductFragment::class.java.canonicalName)
            transaction.addToBackStack(
                    ProductsListFragment::class.java.canonicalName)

            transaction.commit()
        } catch (e: Exception) {
            try {
                Toast.makeText(activity ,
                        "Error while trying to open product details" ,
                        Toast.LENGTH_SHORT).show()
            } catch (e1: Exception) {
            }

        }

    }

    private fun hideKeyboard() {
        if (activity != null) {
            val imm = activity!!
                    .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (imm != null && activity!!.currentFocus != null &&
                    activity!!.currentFocus!!.windowToken != null) {
                imm.hideSoftInputFromWindow(activity!!.currentFocus!!.windowToken , 0)
            }
        }
    }

    companion object {

        private val TAG = "ProductsListFragment"
    }
}
