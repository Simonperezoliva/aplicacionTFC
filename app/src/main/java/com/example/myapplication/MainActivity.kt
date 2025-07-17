package com.example.myapplication

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.graphics.RectF
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
    private lateinit var detector: DetectorDeLibros
    private lateinit var previewView: PreviewView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        detector = DetectorDeLibros(this)
        previewView = findViewById(R.id.camera_preview)
        if (permisosOtorgados()) {
            iniciarCamara()
            findViewById<Button>(R.id.btnDetectar).setOnClickListener {
                Toast.makeText(this, "Escaneando...", Toast.LENGTH_SHORT).show()
                escanearLibros()
            }
        } else {
            requestPermissions.launch(REQUIRED_PERMISSIONS)
        }
    }

    private fun iniciarCamara() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val selectorDeCamara = CameraSelector.DEFAULT_BACK_CAMERA //camara de atrás por default

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, selectorDeCamara, preview
                )
            } catch (exc: Exception) {
                Log.e("CameraX", "Error al iniciar cámara", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    //============================CONFIGURACION DE PERMISOS DE CAMARA==============================================
    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        if (permisosOtorgados()) {
            iniciarCamara()
        } else {
            Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun permisosOtorgados(): Boolean {
        for (permiso in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permiso) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    companion object { //listado de permisos en la app
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    //============================FIN PERMISOS DE CAMARA==============================================

    //============================DETECCION DE LIBROS=================================================
    private fun escanearLibros() {
        val dibujarCajas = findViewById<OverlayView>(R.id.dibujarCajas)
        val bitmap = previewView.bitmap
        if (bitmap != null) {
            Log.d("YOLO", "Bitmap capturado: ${bitmap.width}x${bitmap.height}") //code para chequear que onda el modelo
            //Bitmap capturado: 1080x1775 ---> ESO SALIÓ EN EL LOGCAT ---->VER DE AJUSTAR EL PREPROCESAMIENTO DE INPUT
            val boxes = detector.detectarLibros(bitmap)
            val reescaladoX = previewView.width / 640f //conversion de 640 a tamanio del previewview
            val reescaladoY = previewView.height / 640f
            val cajasReescaladas = boxes.map { rect ->
                RectF(
                    rect.left * reescaladoX,
                    rect.top * reescaladoY,
                    rect.right * reescaladoX,
                    rect.bottom * reescaladoY
                )
            }
            dibujarCajas.setResults(cajasReescaladas)
            Toast.makeText(this, "Libros detectados: ${cajasReescaladas.size}", Toast.LENGTH_LONG).show()
        }else {
            Toast.makeText(this, "No se pudo capturar el frame ", Toast.LENGTH_SHORT).show()
        }
    }
    //============================FIN DETECCION DE LIBROS==============================================

}