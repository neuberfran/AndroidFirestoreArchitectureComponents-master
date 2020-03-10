package br.com.siecola.firestorearchcomp.fragment

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast

import br.com.siecola.firestorearchcomp.R
import br.com.siecola.firestorearchcomp.model.Product
import br.com.siecola.firestorearchcomp.repository.ProductRepository
import br.com.siecola.firestorearchcomp.util.GlobalArgs
import br.com.siecola.firestorearchcomp.viewmodel.ProductViewModel

class ProductFragment : Fragment() {

    private var rootView: View? = null

    private var edtName: EditText? = null
    private var edtCode: EditText? = null
    private var edtDescription: EditText? = null
    private var edtPrice: EditText? = null

    private var productId: String? = null

    private var productViewModel: ProductViewModel? = null

    private var product: Product? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        edtName = rootView!!.findViewById(R.id.edtName)
        edtCode = rootView!!.findViewById(R.id.edtCode)
        edtDescription = rootView!!.findViewById(R.id.edtDescription)
        edtPrice = rootView!!.findViewById(R.id.edtPrice)

        val arguments = arguments
        if (arguments != null && arguments.containsKey(GlobalArgs.PRODUCT_ID)) {
            activity!!.title = "Edit product"
            productId = arguments.getString(GlobalArgs.PRODUCT_ID)
        } else {
            activity!!.title = "New product"
        }

        productViewModel = ViewModelProviders.of(this).get(ProductViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater , container: ViewGroup? ,
                              savedInstanceState: Bundle?): View? {

        rootView = inflater.inflate(R.layout.fragment_product , container , false)

        val layout = rootView!!.findViewById<ConstraintLayout>(R.id.layout)
        layout.setOnClickListener { v -> hideKeyboard() }

        return rootView
    }

    override fun onResume() {
        super.onResume()
        if (productId != null) {
            productViewModel!!.getProductById(productId).observe(this , { product ->
                this.product = product
                if (product != null) {
                    edtName!!.setText(product!!.getName())
                    edtCode!!.setText(product!!.getCode())
                    edtDescription!!.setText(product!!.getDescription())
                    edtPrice!!.setText(product!!.getPrice().toString())
                }
            })
        }
    }

    override fun onPause() {
        if (!edtName!!.text.toString().isEmpty()) {
            if (product == null) {
                product = Product()
                product!!.id = null
            }
            product!!.name = edtName!!.text.toString()
            product!!.code = edtCode!!.text.toString()
            product!!.description = edtDescription!!.text.toString()

            if (edtPrice!!.text.toString().isEmpty()) {
                edtPrice!!.setText("0")
            }

            product!!.price = java.lang.Double.parseDouble(edtPrice!!.text.toString())

            ProductRepository.getInstance().saveProduct(product!!)
        } else {
            Toast.makeText(activity , "This product was not saved!" , Toast.LENGTH_SHORT).show()
        }
        super.onPause()
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
}
