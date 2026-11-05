package app.rork.quran.widget

import android.appwidget.AppWidgetManager
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.rork.quran.R
import app.rork.quran.domain.model.Ayah
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AyahWidgetTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun widgetsKeepIndependentVersesAcrossStoreRecreation() {
        val first = -9001
        val second = -9002
        val store = AyahWidgetStore(context)
        val catalog = app.rork.quran.data.repository.PassageCatalog(context)
        val verse = Ayah("26-32", 26, "الشعراء", 32, "فَأَلْقَىٰ عَصَاهُ")
        try {
            store.save(first, AyahWidgetStore.fallback)
            store.save(second, verse)
            assertEquals(catalog.resolve(AyahWidgetStore.fallback.id), AyahWidgetStore(context).load(first))
            assertEquals(catalog.resolve(verse.id), AyahWidgetStore(context).load(second))
            assertTrue(store.isCurrent(second))
            store.remove(first)
            assertNull(store.load(first))
            assertEquals(catalog.resolve(verse.id), store.load(second))
        } finally {
            store.remove(first)
            store.remove(second)
        }
    }

    @Test fun remoteViewsInflateWithTheVerseAndAdaptToWidgetHeight() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val ayah = app.rork.quran.data.repository.PassageCatalog(context).resolve(AyahWidgetStore.fallback.id)
            fun inflate(height: Int) = AyahWidgetProvider.views(context, -9003, ayah, Bundle().apply {
                putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, height)
            }).apply(context, FrameLayout(context))
            val small = inflate(150)
            val large = inflate(350)
            val smallVerse = small.findViewById<android.widget.ImageView>(R.id.widget_verse)
            val largeVerse = large.findViewById<android.widget.ImageView>(R.id.widget_verse)
            assertEquals(ayah.textArabic, smallVerse.contentDescription.toString())
            assertNotNull(smallVerse.drawable)
            assertTrue(small.findViewById<TextView>(R.id.widget_reference).text.contains(ayah.surahNameArabic))
            assertTrue(largeVerse.drawable.intrinsicHeight > smallVerse.drawable.intrinsicHeight)
            assertEquals(android.view.Gravity.CENTER, small.findViewById<TextView>(R.id.widget_title).gravity)
            assertTrue(small.findViewById<android.view.View>(R.id.widget_refresh).hasOnClickListeners())
            assertTrue(small.hasOnClickListeners())
            if (InstrumentationRegistry.getArguments().getString("captureWidget") == "true") {
                val density = context.resources.displayMetrics.density
                val width = (250 * density).toInt()
                val height = (350 * density).toInt()
                large.measure(android.view.View.MeasureSpec.makeMeasureSpec(width, android.view.View.MeasureSpec.EXACTLY),
                    android.view.View.MeasureSpec.makeMeasureSpec(height, android.view.View.MeasureSpec.EXACTLY))
                large.layout(0, 0, width, height)
                val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
                large.draw(android.graphics.Canvas(bitmap))
                java.io.File(context.cacheDir, "widget-preview.png").outputStream().use {
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
                }
                bitmap.recycle()
            }
        }
    }
    @Test fun verseImagesStayBoundedAtExtremeWidgetSizesAndRetainInk() {
        val text = app.rork.quran.data.repository.PassageCatalog(context).resolve("107-4").textArabic
        listOf(150, 2000).forEach { size ->
            val image = WidgetVerseRenderer.render(context, text, Bundle().apply {
                putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, size)
                putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, size)
                putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, size)
                putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, size)
            })
            assertTrue(image.byteCount <= 2 * 1024 * 1024)
            val pixels = IntArray(image.width * image.height)
            image.getPixels(pixels, 0, image.width, 0, 0, image.width, image.height)
            assertTrue(pixels.any { android.graphics.Color.alpha(it) > 0 })
            image.recycle()
        }
    }

    @Test
    @androidx.test.filters.SdkSuppress(minSdkVersion = 29)
    fun boundWidgetLoadsRefreshesAndOpensItsOwnVerse() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val manager = AppWidgetManager.getInstance(context)
        var host: android.appwidget.AppWidgetHost? = null
        var id = AppWidgetManager.INVALID_APPWIDGET_ID
        instrumentation.uiAutomation.adoptShellPermissionIdentity(android.Manifest.permission.BIND_APPWIDGET)
        try {
            instrumentation.runOnMainSync {
                host = android.appwidget.AppWidgetHost(context, 9041)
                id = host!!.allocateAppWidgetId()
                assertTrue(manager.bindAppWidgetIdIfAllowed(id,
                    android.content.ComponentName(context, AyahWidgetProvider::class.java)))
            }
            fun awaitVerse(previous: String? = null): Ayah = kotlinx.coroutines.runBlocking {
                kotlinx.coroutines.withTimeout(30_000) {
                    var verse = AyahWidgetStore(context).load(id)
                    while (verse == null || verse.id == previous) {
                        kotlinx.coroutines.delay(100)
                        verse = AyahWidgetStore(context).load(id)
                    }
                    verse
                }
            }
            val first = awaitVerse()
            instrumentation.runOnMainSync {
                AyahWidgetProvider.views(context, id, first, Bundle()).apply(context, FrameLayout(context))
                    .findViewById<android.view.View>(R.id.widget_refresh).performClick()
            }
            val refreshed = awaitVerse(first.id)
            assertNotEquals(first.id, refreshed.id)
            val monitor = instrumentation.addMonitor(app.rork.quran.MainActivity::class.java.name, null, false)
            try {
                instrumentation.runOnMainSync {
                    AyahWidgetProvider.views(context, id, refreshed, Bundle()).apply(context, FrameLayout(context)).performClick()
                }
                val opened = instrumentation.waitForMonitorWithTimeout(monitor, 30_000)
                assertNotNull(opened)
                assertEquals(refreshed.id, opened.intent.data?.lastPathSegment)
                instrumentation.runOnMainSync { opened.finish() }
            } finally {
                instrumentation.removeMonitor(monitor)
            }
        } finally {
            instrumentation.runOnMainSync { host?.deleteHost() }
            if (id != AppWidgetManager.INVALID_APPWIDGET_ID) {
                androidx.work.WorkManager.getInstance(context).cancelUniqueWork("ayah-widget-$id")
                AyahWidgetStore(context).remove(id)
            }
            instrumentation.uiAutomation.dropShellPermissionIdentity()
        }
    }

}
