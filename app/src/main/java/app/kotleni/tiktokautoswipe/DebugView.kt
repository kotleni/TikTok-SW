package app.kotleni.tiktokautoswipe

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View

class DebugView(context: Context, attrs: AttributeSet): View(context, attrs) {
    private var actualContentValues: MainActivity.ContentValues? = null
    private val paint = Paint().apply {
        this.color = Color.RED
    }
    private val textPaint = TextPaint().apply {
        this.color = Color.GREEN
        this.textSize = 16f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val values = actualContentValues ?: return

        canvas.drawLine(values.swipeX, 0f, values.swipeX, values.height, paint)

        val textPadding = 4f

        canvas.drawLine(0f, values.swipeStartY, values.width, values.swipeStartY, paint)
        canvas.drawText("START", 40f, values.swipeStartY - textPadding, textPaint)

        canvas.drawLine(0f, values.swipeHalfY, values.width, values.swipeHalfY, paint)
        canvas.drawText("HALF", 40f, values.swipeHalfY - textPadding, textPaint)

        canvas.drawLine(0f, values.swipeEndY, values.width, values.swipeEndY, paint)
        canvas.drawText("END", 40f, values.swipeEndY - textPadding, textPaint)
    }

    fun updateValues(contentValues: MainActivity.ContentValues) {
        this.actualContentValues = contentValues
        invalidate()
    }
}