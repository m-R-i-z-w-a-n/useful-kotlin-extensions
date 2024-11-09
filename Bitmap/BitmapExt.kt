fun Bitmap.toRoundedBitmap(radius: Float): Bitmap {
    val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val rect = Rect(0, 0, width, height)
    val roundRect = RectF(rect)
    canvas.drawRoundRect(roundRect, radius, radius, paint)
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(this, rect, rect, paint)
    return output
}

fun Bitmap.drawTextOnBitmap(text: String, x: Float, y: Float, textSize: Float = 40f, textColor: Int = Color.BLACK): Bitmap {
    val paint = Paint().apply {
        this.textSize = textSize
        this.color = textColor
        this.isAntiAlias = true
    }
    val mutableBitmap = this.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(mutableBitmap)
    canvas.drawText(text, x, y, paint)
    return mutableBitmap
}

fun Bitmap.toGrayscale(): Bitmap {
    val cm = ColorMatrix()
    cm.setSaturation(0f)
    val paint = Paint()
    paint.colorFilter = ColorMatrixColorFilter(cm)
    
    val grayscaleBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(grayscaleBitmap)
    canvas.drawBitmap(this, 0f, 0f, paint)
    return grayscaleBitmap
}

  fun Bitmap.toCircularBitmap(): Bitmap {
    val size = Math.min(width, height)
    val radius = size / 2f
    val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val paint = Paint()
    paint.isAntiAlias = true
    paint.shader = BitmapShader(this, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    canvas.drawCircle(radius, radius, radius, paint)
    return output
}

  fun Bitmap.drawCircleOnBitmap(radius: Float, centerX: Float, centerY: Float, circleColor: Int): Bitmap {
    val mutableBitmap = this.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(mutableBitmap)
    val paint = Paint().apply {
        this.color = circleColor
        this.isAntiAlias = true
    }
    canvas.drawCircle(centerX, centerY, radius, paint)
    return mutableBitmap
}

  fun Bitmap.flipHorizontally(): Bitmap {
    val matrix = Matrix().apply { preScale(-1f, 1f) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, false)
}

  fun Bitmap.toSepia(): Bitmap {
    val colorMatrix = ColorMatrix()
    colorMatrix.setScale(1f, 0.9f, 0.6f, 1f) // Sepia tone effect
    val paint = Paint()
    paint.colorFilter = ColorMatrixColorFilter(colorMatrix)

    val sepiaBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(sepiaBitmap)
    canvas.drawBitmap(this, 0f, 0f, paint)
    return sepiaBitmap
}
  
