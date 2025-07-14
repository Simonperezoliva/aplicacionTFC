package com.example.myapplication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.RectF
import org.tensorflow.lite.Interpreter
import android.util.Size
import java.io.FileInputStream
import java.nio.channels.FileChannel

class detectorDeLibros(context: Context) {

    //===========================================CONFIGURACION DE DETECCION=================================================
    private val interpreter: Interpreter
    private val inputImageSize = Size(640, 640)
    private val threshold = 0.4f
    private val labels = listOf("book") // Clase 84
    //===========================================FIN CONFIGURACION DE DETECCION=============================================

    init {
        val assetManager = context.assets
        val assetFileDescriptor = assetManager.openFd("yolov8n_float32.tflite")
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        val modelBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        val options = Interpreter.Options()
        interpreter = Interpreter(modelBuffer, options)
    }

    fun detectarLibros(bitmap: Bitmap): List<RectF> {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 640, 640, false)
        val input = preprocess(resizedBitmap)
        val output = Array(1) { Array(84) { FloatArray(8400) } }
        interpreter.run(input, output)
        return postprocesar(output[0])
    }

    private fun preprocess(bitmap: Bitmap): Array<Array<Array<FloatArray>>> {
        val input = Array(1) { Array(inputImageSize.width) { Array(inputImageSize.height) { FloatArray(3) } } } //4dim
        for (y in 0 until inputImageSize.width) {
            for (x in 0 until inputImageSize.height) {
                val px = bitmap.getPixel(x, y)
                input[0][y][x][0] = Color.red(px) / 255f
                input[0][y][x][1] = Color.green(px) / 255f
                input[0][y][x][2] = Color.blue(px) / 255f
            }
        }
        return input
    }

    private fun postprocesar(output: Array<FloatArray>): List<RectF> {
        val boxes = mutableListOf<RectF>()
        for (i in output.indices) {
            val confidence = output[i][4]
            val classScore = output[i][5]
            //val classId = classScore.indexOf(maxClassScore)
            if (confidence > threshold && classScore > 0.5f) {
                val x = output[i][0]
                val y = output[i][1]
                val w = output[i][2]
                val h = output[i][3]
                boxes.add(RectF(x - w/2, y - h/2, x + w/2, y + h/2))
            }
        }
        return boxes
    }
}

/*
El modelo devuelto por Ultralytics tiene forma [1, 84, 8400], por lo tanto:

output[0] → array con 84 elementos
output[0][4] → array con 8400 scores de confianza
output[0][4][i] → confianza de la detección i-ésima
*/
