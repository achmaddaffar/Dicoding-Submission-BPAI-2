package com.example.storyapp.ui.addStory

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.storyapp.R
import com.example.storyapp.databinding.ActivityAddStoryBinding
import com.example.storyapp.ui.addStory.CameraActivity.Companion.IS_BACK_CAMERA_EXTRA
import com.example.storyapp.ui.addStory.CameraActivity.Companion.PICTURE_EXTRA
import com.example.storyapp.ui.main.ListStoryActivity
import com.example.storyapp.utils.Helper.Companion.bitmapToFile
import com.example.storyapp.utils.Helper.Companion.rotateBitmap
import com.example.storyapp.utils.Helper.Companion.uriToFile
import com.example.storyapp.utils.ScreenState
import com.example.storyapp.utils.ViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.File

class AddStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStoryBinding
    private lateinit var viewModel: AddStoryViewModel
    private lateinit var dialog: Dialog
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var getFile: File? = null
    private var lat: Double? = null
    private var lon: Double? = null
    private var locationToggle: Boolean = false

    @Suppress("DEPRECATION")
    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERA_X_RESULT) {
            val file = it.data?.getSerializableExtra(PICTURE_EXTRA) as File
            val isBackCamera = it.data?.getBooleanExtra(IS_BACK_CAMERA_EXTRA, true) as Boolean
            val bmp = rotateBitmap(
                BitmapFactory.decodeFile(file.path),
                isBackCamera
            )
            val result = bitmapToFile(this, bmp)
            getFile = result
            viewModel.setFile(result)
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            val file = uriToFile(selectedImg, this)
            getFile = file
            viewModel.setFile(file)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                    getMyLastLocation()
                }
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    getMyLastLocation()
                }
                else -> {}
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!allPermissionsGranted())
            requestPermission()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupViewModel()
        setupAction()
        setButtonEnable()
    }

    override fun onResume() {
        super.onResume()
        setButtonEnable()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    this,
                    getString(R.string.unable_to_obtain_permission),
                    Toast.LENGTH_SHORT
                ).show()
                showPermissionDialog()
            }
        }
    }

    private fun getMyLastLocation() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    lat = location.latitude
                    lon = location.longitude
                    Log.e(TAG, "LOKASI CUYH $lat, $lon")
                }
            }
        } else {
            requestPermissionLauncher.launch(LOCATION_PERMISSIONS)
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun showPermissionDialog() {
        AlertDialog.Builder(this).apply {
            setTitle(StringBuilder("\"${getString(R.string.app_name)}\" ").append(getString(R.string.camera_request_title)))
            setMessage(getString(R.string.camera_request_msg))
            setCancelable(false)
            setPositiveButton(R.string.yes) { dialogInterface, _ ->
                dialogInterface.dismiss()
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            setNegativeButton(getString(R.string.no)) { dialogInterface, _ -> dialogInterface.dismiss() }
            show()
        }
    }

    private fun requestPermission(): Boolean {
        val isGranted = allPermissionsGranted()
        if (!isGranted)
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        return isGranted
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            this,
            ViewModelFactory(application)
        )[AddStoryViewModel::class.java]

        viewModel.apply {
            tempFile.observe(this@AddStoryActivity) { file ->
                val bitmap = BitmapFactory.decodeFile(file.path)
                binding.ivPreviewImage.setImageBitmap(bitmap)
            }
        }
    }

    private fun setupAction() {
        dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_loading)
        dialog.setCancelable(false)
        if (dialog.window != null)
            dialog.window?.setBackgroundDrawable(ColorDrawable(0))

        binding.apply {
            btnGallery.setOnClickListener { startGallery() }
            btnCamera.setOnClickListener {
                requestPermission()
                if (allPermissionsGranted())
                    startCameraX()
            }

            edAddDescription.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    c: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(c: CharSequence?, start: Int, before: Int, count: Int) {
                    setButtonEnable()
                }

                override fun afterTextChanged(s: Editable?) {
                    edAddDescription.error = if (edAddDescription.text.toString()
                            .isEmpty()
                    ) getString(R.string.this_field_cannot_be_blank)
                    else null
                }
            })

            buttonAdd.setOnClickListener {
                val file = getFile as File
                val desc = edAddDescription.text.toString()
                if (!locationToggle) {
                    lat = null
                    lon = null
                } else
                    getMyLastLocation()

                Log.e(TAG, "LOKASI TOGGLE: $locationToggle, $lat, $lon")
                viewModel.uploadStory(file, desc, lat = lat, lon = lon)
                    .observe(this@AddStoryActivity) { state ->
                        when (state) {
                            is ScreenState.Loading -> {
                                showLoading(true)
                            }
                            is ScreenState.Success -> {
                                showLoading(false)
                                val intent =
                                    Intent(this@AddStoryActivity, ListStoryActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            is ScreenState.Error -> {
                                showLoading(false)
                                Log.e(TAG, "onError: ${state.data?.message}")
                                showError()
                            }
                            else -> {}
                        }
                    }
            }

            switchLocation.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked)
                    getMyLastLocation()
                locationToggle = isChecked
            }
        }
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, getString(R.string.choose_a_picture))
        launcherIntentGallery.launch(chooser)
    }

    private fun startCameraX() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    private fun setButtonEnable() {
        val storyImage = binding.ivPreviewImage.drawable
        val description = binding.edAddDescription.text

        binding.buttonAdd.isEnabled =
            storyImage != null && description != null && description.toString()
                .isNotEmpty()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) dialog.show() else dialog.cancel()
    }

    private fun showError() {
        AlertDialog.Builder(this).apply {
            setTitle(getString(R.string.error))
            setCancelable(false)
            setMessage(getString(R.string.something_went_wrong_please_try_again_later))
            setPositiveButton(getString(R.string.yes)) { dialogInterface, _ ->
                dialogInterface.dismiss()
                finish()
            }
            show()
        }
    }

    companion object {
        const val CAMERA_X_RESULT = 200
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private val LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        private const val REQUEST_CODE_PERMISSIONS = 10
        private const val TAG = "AddStoryActivity"
    }
}