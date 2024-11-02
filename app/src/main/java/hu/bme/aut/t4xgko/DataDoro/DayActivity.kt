package hu.bme.aut.t4xgko.DataDoro

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import hu.bme.aut.t4xgko.DataDoro.data.AppDatabase
import hu.bme.aut.t4xgko.DataDoro.data.Day
import hu.bme.aut.t4xgko.DataDoro.databinding.ActivityDayBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class DayActivity : AppCompatActivity() {

    companion object {
        private const val DATE_FORMAT = "yyyyMMdd_HHmmss"
        private const val PHOTO_PREFIX = "JPEG_"
        private const val PHOTO_EXTENSION = ".jpg"
        private const val IMAGES_DIRECTORY = "images"
    }

    private val binding by lazy { ActivityDayBinding.inflate(layoutInflater) }
    private val cameraExecutor: ExecutorService by lazy { Executors.newSingleThreadExecutor() }
    
    private var day: Day = Day.nullDay()
    private lateinit var imageCapture: ImageCapture
    private var isCameraMode = false

    private val requiredPermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            startCamera()
        } else {
            Toast.makeText(
                this,
                "Permissions not granted by the user.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        
        setupDay()
        setupClickListeners()
    }

    private fun setupDay() {
        val yearMonthDay = intent.getStringExtra("DAY_ID") ?: run {
            finish()
            return
        }

        loadDayData(yearMonthDay)
    }

    private fun setupClickListeners() {
        with(binding) {
            btnSave.setOnClickListener { saveDay() }
            btnCamera.setOnClickListener { handleCameraButton() }
        }
    }

    private fun loadDayData(yearMonthDay: String) {
        Thread {
            val loadedDay = AppDatabase.getInstance(this).dayDao().getDay(yearMonthDay)
            day = loadedDay ?: Day.nullDay()
            runOnUiThread { updateUI() }
        }.start()
    }

    private fun updateUI() {
        with(binding) {
            tvDate.text = day.YearMonthDay
            editTextDate.setText(day.dayText)
            progressBar.apply {
                max = day.GoalTimeSec
                progress = day.TimeStudiedSec
            }
            day.image?.let { imagePath ->
                val bitmap = BitmapFactory.decodeFile(imagePath)
                imageView.setImageBitmap(bitmap)
            }
        }
    }

    private fun handleCameraButton() {
        when {
            !isCameraMode && hasRequiredPermissions() -> startCamera()
            !isCameraMode -> requestRequiredPermissions()
            else -> takePhoto()
        }
    }

    private fun hasRequiredPermissions(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.P) {
            requiredPermissions.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestRequiredPermissions() {
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.P) {
            permissionLauncher.launch(requiredPermissions)
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
        }
    }

    private fun startCamera() {
        isCameraMode = true
        setupCameraPreviewVisibility(true)

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            setupCameraProvider(cameraProviderFuture.get())
        }, ContextCompat.getMainExecutor(this))
    }

    private fun setupCameraProvider(cameraProvider: ProcessCameraProvider) {
        try {
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageCapture
            )
        } catch (exc: Exception) {
            Toast.makeText(this, "Failed to start camera", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupCameraPreviewVisibility(showCamera: Boolean) {
        binding.viewFinder.visibility = if (showCamera) View.VISIBLE else View.GONE
        binding.imageView.visibility = if (showCamera) View.GONE else View.VISIBLE
    }

    private fun takePhoto() {
        val photoFile = createImageFile()
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    handlePhotoSuccess(photoFile)
                }

                override fun onError(exception: ImageCaptureException) {
                    handlePhotoError(exception)
                }
            }
        )
    }

    private fun handlePhotoSuccess(photoFile: File) {
        try {
            val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
            binding.imageView.setImageBitmap(bitmap)
            day.image = photoFile.absolutePath
            updateDayInDatabase()
            
            isCameraMode = false
            setupCameraPreviewVisibility(false)
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to save photo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handlePhotoError(exception: ImageCaptureException) {
        Toast.makeText(
            this,
            "Failed to take photo: ${exception.message}",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat(DATE_FORMAT, Locale.US).format(Date())
        // Create a directory in your app's private storage
        val storageDir = File(applicationContext.filesDir, IMAGES_DIRECTORY).apply {
            if (!exists()) mkdir()
        }
        
        return File(storageDir, "${PHOTO_PREFIX}${timeStamp}${PHOTO_EXTENSION}")
    }

    private fun saveDay() {
        Thread {
            day.dayText = binding.editTextDate.text.toString()
            AppDatabase.getInstance(this).dayDao().updateDay(day)
            runOnUiThread {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }.start()
    }

    private fun updateDayInDatabase() {
        Thread {
            AppDatabase.getInstance(this).dayDao().updateDay(day)
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}