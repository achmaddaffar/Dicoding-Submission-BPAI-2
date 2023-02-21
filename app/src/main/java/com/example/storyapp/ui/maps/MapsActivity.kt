package com.example.storyapp.ui.maps

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.storyapp.R
import com.example.storyapp.data.remote.response.ListStoryItem
import com.example.storyapp.databinding.ActivityMapsBinding
import com.example.storyapp.utils.ScreenState
import com.example.storyapp.utils.ViewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var viewModel: MapsViewModel
    private lateinit var dialog: Dialog

    private var boundsBuilder = LatLngBounds.Builder()
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted)
            getMyLocation()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupViewModel()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    this,
                    getString(R.string.unable_to_obtain_permission),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        getMyLocation()
        setMapStyle()
    }

    private fun setupView() {
        supportActionBar?.title = getString(R.string.app_name)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_loading)
        dialog.setCancelable(false)
        if (dialog.window != null)
            dialog.window?.setBackgroundDrawable(ColorDrawable(0))
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            this,
            ViewModelFactory(application)
        )[MapsViewModel::class.java]

        viewModel.getStory().observe(this) {
            determineState(it)
        }
    }

    private fun setMapStyle() {
        try {
            val success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
            if (!success)
                Log.e(TAG, "Style parsing failed")
        } catch (exception: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", exception)
        }
    }

    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                applicationContext,z
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun determineState(state: ScreenState<List<ListStoryItem>>?) {
        when (state) {
            is ScreenState.Loading -> {
                showErrorLayout(false)
                dialog.show()
            }
            is ScreenState.Success -> {
                showErrorLayout(false)
                dialog.hide()
                if (state.data != null) {
                    val storyList = state.data
                    storyList.forEach {
                        val latitude = it.lat as Double
                        val longitude = it.lon as Double
                        val latLng = LatLng(latitude, longitude)
                        val title = it.name as String
                        val desc = it.description as String
                        mMap.addMarker(
                            MarkerOptions()
                                .position(latLng)
                                .title(title)
                                .snippet(desc)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        )
                        boundsBuilder.include(latLng)
                    }

                    val bounds: LatLngBounds = boundsBuilder.build()
                    mMap.animateCamera(
                        CameraUpdateFactory.newLatLngBounds(
                            bounds,
                            resources.displayMetrics.widthPixels,
                            resources.displayMetrics.heightPixels,
                            200
                        )
                    )
                }
            }
            is ScreenState.Error -> {
                dialog.hide()
                Log.e(TAG, "onError: ${state.message}")
                showErrorLayout(true)
            }
            else -> {}
        }
    }

    private fun showErrorLayout(isError: Boolean) {
        binding.errorLayout.visibility = if (isError) View.VISIBLE else View.GONE
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val TAG = "MapsActivity"
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        private const val REQUEST_CODE_PERMISSION = 20
    }
}