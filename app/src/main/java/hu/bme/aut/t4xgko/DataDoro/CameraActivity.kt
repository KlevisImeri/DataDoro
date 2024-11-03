package hu.bme.aut.t4xgko.DataDoro

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import hu.bme.aut.t4xgko.DataDoro.databinding.ActivityCameraBinding
import hu.bme.aut.t4xgko.DataDoro.permissionHandlers.CameraPermissionHandler
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {
    companion object {
        private const val DATE_FORMAT = "yyyyMMdd_HHmmss"
        private const val PHOTO_PREFIX = "JPEG_"
        private const val PHOTO_EXTENSION = ".jpg"
        private const val IMAGES_DIRECTORY = "images"
        const val IMAGE_PATH = "IMAGE_PATH"
    }

    private val binding by lazy { ActivityCameraBinding.inflate(layoutInflater) }
    private val cameraExecutor: ExecutorService by lazy { Executors.newSingleThreadExecutor() }
    private lateinit var imageCapture: ImageCapture
    private var photoFile: File? = null
    private lateinit var cameraPermissionHandler: CameraPermissionHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupPermissions()
        setupButtons()
    }

    private fun setupPermissions() {
        cameraPermissionHandler = CameraPermissionHandler(this)
        cameraPermissionHandler.setupPermissions { startCamera() }
        cameraPermissionHandler.checkAndRequestPermissions()
    }

    private fun setupButtons() {
        binding.apply {
            btnCapture.setOnClickListener { takePhoto() }
            btnConfirm.setOnClickListener { confirmPhoto() }
            btnRetake.setOnClickListener { retakePhoto() }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                Toast.makeText(this, "Failed to start camera", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val bitmap = imageProxyToBitmap(image)
                    val rotatedBitmap = rotateBitmap(bitmap, 90f)
                    
                    photoFile = createImageFile()
                    saveBitmapToFile(rotatedBitmap, photoFile!!)
                    
                    binding.imageView.setImageBitmap(rotatedBitmap)
                    binding.viewFinder.visibility = View.GONE
                    binding.imageView.visibility = View.VISIBLE
                    binding.btnCapture.visibility = View.GONE
                    binding.btnConfirm.visibility = View.VISIBLE
                    binding.btnRetake.visibility = View.VISIBLE
                    
                    image.close()
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(
                        this@CameraActivity,
                        "Failed to take photo: ${exception.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val buffer: ByteBuffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining()).apply { 
            buffer.get(this)
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun saveBitmapToFile(bitmap: Bitmap, file: File) {
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat(DATE_FORMAT, Locale.US).format(Date())
        val storageDir = File(applicationContext.filesDir, IMAGES_DIRECTORY).apply {
            if (!exists()) mkdir()
        }
        return File(storageDir, "${PHOTO_PREFIX}${timeStamp}${PHOTO_EXTENSION}")
    }

    private fun confirmPhoto() {
        val resultIntent = Intent().apply {
            putExtra(IMAGE_PATH, photoFile!!.absolutePath)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun retakePhoto() {
        binding.apply {
            viewFinder.visibility = View.VISIBLE
            imageView.visibility = View.GONE
            btnCapture.visibility = View.VISIBLE
            btnConfirm.visibility = View.GONE
            btnRetake.visibility = View.GONE
        }
        startCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}