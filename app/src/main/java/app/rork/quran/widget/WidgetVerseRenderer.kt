package app.rork.quran.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import android.text.TextUtils
import androidx.core.content.res.ResourcesCompat
import app.rork.quran.R
import kotlin.math.sqrt

/** Launcher RemoteViews ignore custom font files; render with the app's exact Quran typeface. */
internal object WidgetVerseRenderer {
    fun render(context: Context, text: String, options: Bundle): Bitmap {
        val resources = context.resources
        val metrics = resources.displayMetrics
        val landscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val widthKey = if (landscape) AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH else AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH
        val heightKey = if (landscape) AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT else AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT
        val widthDp = (options.getInt(widthKey, 250) - 28).coerceIn(80, 1000)
        val heightDp = (options.getInt(heightKey, options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 180)) - 116).coerceIn(24, 1000)
        val physicalWidth = widthDp * metrics.density
        val physicalHeight = heightDp * metrics.density
        // Bound each RemoteViews image to 2 MiB, even on large, high-density launchers.
        val scale = minOf(1f, sqrt(524_288f / (physicalWidth * physicalHeight)))
        val width = (physicalWidth * scale).toInt().coerceAtLeast(1)
        val height = (physicalHeight * scale).toInt().coerceAtLeast(1)
        val paint = TextPaint(android.graphics.Paint.ANTI_ALIAS_FLAG or android.graphics.Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = requireNotNull(ResourcesCompat.getFont(context, R.font.kfgqpc))
            color = Color.WHITE // ImageView applies the current light/dark text tint.
        }
        fun layout(maxLines: Int = Int.MAX_VALUE): StaticLayout {
            val fontHeight = paint.fontMetricsInt.run { descent - ascent }
            return StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setTextDirection(TextDirectionHeuristics.RTL)
                .setIncludePad(true)
                .setLineSpacing((paint.textSize * 1.9f - fontHeight).coerceAtLeast(0f), 1f)
                .setMaxLines(maxLines)
                .setEllipsize(TextUtils.TruncateAt.END)
                .setEllipsizedWidth(width)
                .build()
        }
        var chosen: StaticLayout? = null
        for (size in 28 downTo 18 step 2) {
            paint.textSize = android.util.TypedValue.applyDimension(
                android.util.TypedValue.COMPLEX_UNIT_SP, size.toFloat(), metrics) * scale
            val candidate = layout()
            if (candidate.height <= height) {
                chosen = candidate
                break
            }
        }
        if (chosen == null) {
            val glyphHeight = paint.fontMetricsInt.run { bottom - top }
            if (glyphHeight > height) paint.textSize *= height.toFloat() / glyphHeight
            var lines = (height / (paint.textSize * 1.9f)).toInt().coerceAtLeast(1)
            var preview = layout(lines)
            while (preview.height > height && lines > 1) preview = layout(--lines)
            chosen = preview
        }
        val finalLayout = requireNotNull(chosen)
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { bitmap ->
            val canvas = Canvas(bitmap)
            canvas.translate(0f, ((height - finalLayout.height) / 2f).coerceAtLeast(0f))
            finalLayout.draw(canvas)
        }
    }
}
