package com.example.myapplication

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class OverlayView (context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private var boxes: List<RectF> = emptyList()

    //==================CONFIGURACION ESTILO CAJAS=========================================
    private val boxPaint = Paint().apply {
        color = Color.RED
        strokeWidth = 6f
        style = Paint.Style.STROKE
    }
    //================== FIN CONFIGURACION ESTILO CAJAS====================================

    fun setResults (newBoxes: List<RectF>) {
        boxes = newBoxes
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (box in boxes){
            canvas.drawRect(box, boxPaint)
        }
    }

}