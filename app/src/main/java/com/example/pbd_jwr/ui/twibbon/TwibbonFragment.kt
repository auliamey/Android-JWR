package com.example.pbd_jwr.ui.twibbon

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.YuvImage
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.pbd_jwr.R
import com.example.pbd_jwr.databinding.FragmentTwibbonBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class TwibbonFragment : Fragment() {

    private var _binding: FragmentTwibbonBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraExecutor: ExecutorService
    private var lensFacing = CameraSelector.LENS_FACING_FRONT
    private var imageCapture: ImageCapture? = null

    companion object {
        private val TAG = TwibbonFragment::class.java.simpleName
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTwibbonBinding.inflate(inflater, container, false)
        cameraExecutor = Executors.newSingleThreadExecutor()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        binding.retakeBtn.isEnabled = false

        binding.twibbonChangeCameraBtn.setOnClickListener {
            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                CameraSelector.LENS_FACING_FRONT
            } else {
                CameraSelector.LENS_FACING_BACK
            }
            startCamera()
        }

        binding.captureBtn.setOnClickListener {
            takePicture()
        }

        binding.retakeBtn.setOnClickListener {
            resetLayover()
        }
    }


    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            bindCameraUseCases(cameraProvider)

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.cameraView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)
                Log.d(TAG, "Camera binding successful")
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun stopCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()
        }, ContextCompat.getMainExecutor(requireContext()))
    }


    private fun bindCameraUseCases(cameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(binding.cameraView.surfaceProvider)
            }

        imageCapture = ImageCapture.Builder().build()

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        try {
            cameraProvider.unbindAll()

            cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageCapture)
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun takePicture() {
        val imageCapture = imageCapture ?: return

        binding.captureBtn.isEnabled = false
        binding.twibbonChangeCameraBtn.isEnabled = false
        binding.retakeBtn.isEnabled = true

        // Menggunakan executor untuk menjalankan tugas di background thread
        val executor = ContextCompat.getMainExecutor(requireContext())
        imageCapture.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                super.onCaptureSuccess(image)
                val backgroundBitmap = imageProxyToBitmap(image)
                val overlayBitmap = loadOverlayBitmap()

                // Flip gambar jika menggunakan kamera depan
                val flippedBitmap = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                    flipBitmap(backgroundBitmap, horizontal = true)
                } else {
                    backgroundBitmap
                }

                val combinedBitmap = combineImages(flippedBitmap, overlayBitmap)
                image.close() // Sangat penting untuk menutup ImageProxy ketika selesai digunakan

                // Update UI di thread UI
                activity?.runOnUiThread {
                    binding.twibbonLayover.setImageBitmap(combinedBitmap)
                    stopCamera()
                }
            }


            override fun onError(exception: ImageCaptureException) {
                Log.e(TAG, "Image capture failed: ${exception.message}", exception)
                activity?.runOnUiThread {
                    Toast.makeText(context, "Failed to capture image, please try again.", Toast.LENGTH_SHORT).show()

                    binding.captureBtn.isEnabled = true
                    binding.twibbonChangeCameraBtn.isEnabled = true
                    binding.retakeBtn.isEnabled = false
                }
            }
        })
    }

    // Fungsi untuk mengkonversi ImageProxy ke Bitmap
    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val planeProxy = image.planes[0]
        val buffer = planeProxy.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    fun flipBitmap(bitmap: Bitmap, horizontal: Boolean = true, vertical: Boolean = false): Bitmap {
        val matrix = Matrix()
        matrix.preScale(if (horizontal) -1f else 1f, if (vertical) -1f else 1f)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun loadOverlayBitmap(): Bitmap {
        return vectorDrawableToBitmap(R.drawable.twibbon_layover)
    }

    private fun resetLayover() {
        binding.twibbonLayover.setImageResource(R.drawable.twibbon_layover)
        startCamera()
        binding.captureBtn.isEnabled = true
        binding.twibbonChangeCameraBtn.isEnabled = true
        binding.retakeBtn.isEnabled = false
    }


    private fun vectorDrawableToBitmap(drawableId: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(requireContext(), drawableId) as VectorDrawable
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun combineImages(background: Bitmap, overlay: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(overlay.width, overlay.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        // Skala untuk centerCrop
        val backgroundRatio = background.width.toFloat() / background.height
        val resultRatio = result.width.toFloat() / result.height
        val scale = if (backgroundRatio > resultRatio) {
            result.height.toFloat() / background.height
        } else {
            result.width.toFloat() / background.width
        }

        val scaledWidth = scale * background.width
        val scaledHeight = scale * background.height
        val left = (result.width - scaledWidth) / 2
        val top = (result.height - scaledHeight) / 2

        // Menggambar background dengan skala centerCrop
        canvas.drawBitmap(background, Rect(0, 0, background.width, background.height), RectF(left, top, left + scaledWidth, top + scaledHeight), null)

        // Menggambar overlay di atasnya
        canvas.drawBitmap(overlay, 0f, 0f, null)

        return result
    }




    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                // Handle permission not granted
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraExecutor.shutdown()
    }

}