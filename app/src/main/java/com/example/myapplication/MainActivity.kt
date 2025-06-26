package com.example.myapplication

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.Toast
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var detector: detectorDeLibros

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        detector = detectorDeLibros(this)

        startCamera()
        findViewById<Button>(R.id.btnDetectar).setOnClickListener {
            escanearLibros()
        }
        // Verificamos permisos
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions.launch(REQUIRED_PERMISSIONS)
        }

        findViewById<Button>(R.id.btnDetectar).setOnClickListener {
            Toast.makeText(this, "Botón presionado", Toast.LENGTH_SHORT).show()
            // Aquí luego llamaremos a la clase detectorDeLibros
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview
                )
            } catch (exc: Exception) {
                Log.e("CameraX", "Error al iniciar cámara", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    // Permisos de cámara
    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    private fun escanearLibros() {
        // Capturá un frame de PreviewView como Bitmap
        val previewView = findViewById<PreviewView>(R.id.camera_preview)
        previewView.bitmap?.let { bitmap ->
            val boxes = detector.detectarLibros(bitmap)
            Toast.makeText(this, "Libros detectados: ${boxes.size}", Toast.LENGTH_LONG).show()
            // Aquí podrías dibujar las cajas si querés
        }
    }
}