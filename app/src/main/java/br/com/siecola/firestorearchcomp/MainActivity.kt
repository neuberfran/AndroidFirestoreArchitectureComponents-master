package br.com.siecola.firestorearchcomp

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager

import com.google.android.gms.auth.api.Auth
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

import br.com.siecola.firestorearchcomp.fragment.ProductsListFragment

class MainActivity : AppCompatActivity() , NavigationView.OnNavigationItemSelectedListener , GoogleApiClient.OnConnectionFailedListener {

    private var navigationView: NavigationView? = null
    private var drawer: DrawerLayout? = null
    private var toggle: ActionBarDrawerToggle? = null

    private var mGoogleApiClient: GoogleApiClient? = null
    private var mFirebaseAuth: FirebaseAuth? = null
    private var mFirebaseUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawer = findViewById(R.id.drawer_layout)
        toggle = ActionBarDrawerToggle(this , drawer , toolbar ,
                R.string.navigation_drawer_open , R.string.navigation_drawer_close)
        drawer!!.addDrawerListener(toggle!!)
        toggle!!.syncState()

        navigationView = findViewById(R.id.nav_view)
        navigationView!!.setNavigationItemSelectedListener(this)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)
        }

        toggle = object : ActionBarDrawerToggle(this , drawer , toolbar ,
                R.string.navigation_drawer_open , R.string.navigation_drawer_close) {
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                invalidateOptionsMenu()
                toggle!!.isDrawerIndicatorEnabled = true

                hideKeyboard()
            }

            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
                invalidateOptionsMenu()
                setActionBarArrowDependingOnFragmentsBackStack()
            }
        }
        drawer!!.setDrawerListener(toggle)
        supportFragmentManager.addOnBackStackChangedListener { setActionBarArrowDependingOnFragmentsBackStack() }

        toggle!!.toolbarNavigationClickListener = View.OnClickListener {
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
            }
        }

        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)

        if (savedInstanceState == null) {
            displayFragment(R.id.nav_products)
        }

        setActionBarArrowDependingOnFragmentsBackStack()

        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseUser = mFirebaseAuth!!.currentUser
        if (mFirebaseUser == null) {
            startActivity(Intent(this , SignInActivity::class.java))
            finish()
            return
        }

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this , this)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle!!.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toggle!!.onConfigurationChanged(newConfig)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val drawerOpen = drawer!!.isDrawerOpen(GravityCompat.START)
        hideMenuItems(menu , !drawerOpen)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }

    private fun hideMenuItems(menu: Menu , visible: Boolean) {
        for (i in 0 until menu.size()) {
            menu.getItem(i).isVisible = visible
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle!!.isDrawerIndicatorEnabled && toggle!!.onOptionsItemSelected(item)) {
            return true
        }

        when (item.itemId) {
            android.R.id.home -> {
                supportFragmentManager.popBackStack()
                supportFragmentManager.executePendingTransactions()
                if (supportFragmentManager.backStackEntryCount < 1) {
                    toggle!!.isDrawerIndicatorEnabled = true
                }
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun displayFragment(fragmentId: Int , arg: String? = null) {
        var fragmentClass: Class<*>

        fragmentClass = ProductsListFragment::class.java

        var fragment: Fragment? = null

        val backStackEntryCount: Int
        backStackEntryCount = supportFragmentManager.backStackEntryCount
        for (j in 0 until backStackEntryCount) {
            supportFragmentManager.popBackStack()
        }

        try {
            when (fragmentId) {
                R.id.nav_products -> {
                    fragmentClass = ProductsListFragment::class.java
                    fragment = fragmentClass.newInstance() as Fragment
                }
                R.id.nav_sign_out -> {
                    mFirebaseAuth!!.signOut()
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient)
                    mFirebaseUser = null
                    startActivity(Intent(this , SignInActivity::class.java))
                    finish()
                }
                else -> {
                    fragmentClass = ProductsListFragment::class.java
                    fragment = fragmentClass.newInstance() as Fragment
                }
            }
        } catch (e: Exception) {
            Log.e(TAG , e.message)
        }

        if (fragment != null) {
            val fragmentManager = supportFragmentManager
            fragmentManager.beginTransaction().replace(R.id.container , fragment ,
                    fragmentClass.canonicalName).commit()
        }

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        navigationView!!.setCheckedItem(fragmentId)
        drawer.closeDrawer(GravityCompat.START)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        displayFragment(item.itemId)
        return true
    }

    private fun hideKeyboard() {
        val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (imm != null && this.currentFocus != null && this.currentFocus!!.windowToken != null) {
            imm.hideSoftInputFromWindow(this.currentFocus!!.windowToken , 0)
        }
    }

    private fun setActionBarArrowDependingOnFragmentsBackStack() {
        val backStackEntryCount = supportFragmentManager.backStackEntryCount
        val shouldEnableDrawerIndicator = backStackEntryCount == 0
        toggle!!.isDrawerIndicatorEnabled = shouldEnableDrawerIndicator
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.d(TAG , "onConnectionFailed:$connectionResult")
    }

    companion object {

        private val TAG = "MainActivity"
    }
}