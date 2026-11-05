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
        val verse = Ayah("26:32", 26, "الشعراء", 32, "فَأَلْقَىٰ عَصَاهُ")
        try {
            store.save(first, AyahWidgetStore.fallback)
            store.save(second, verse)
            assertEquals(AyahWidgetStore.fallback, AyahWidgetStore(context).load(first))
            assertEquals(verse, AyahWidgetStore(context).load(second))
            assertTrue(store.isCurrent(second))
            store.remove(first)
            assertNull(store.load(first))
            assertEquals(verse, store.load(second))
        } finally {
            store.remove(first)
            store.remove(second)
        }
    }

    @Test fun remoteViewsInflateWithTheVerseAndAdaptToWidgetHeight() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val ayah = AyahWidgetStore.fallback
            fun inflate(height: Int) = AyahWidgetProvider.views(context, -9003, ayah, Bundle().apply {
                putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, height)
            }).apply(context, FrameLayout(context))
            val small = inflate(150)
            val large = inflate(350)
            val smallVerse = small.findViewById<TextView>(R.id.widget_verse)
            val largeVerse = large.findViewById<TextView>(R.id.widget_verse)
            assertEquals(ayah.textArabic, smallVerse.text.toString())
            assertTrue(small.findViewById<TextView>(R.id.widget_reference).text.contains(ayah.surahNameArabic))
            assertTrue(largeVerse.maxLines > smallVerse.maxLines)
            assertTrue(small.findViewById<android.view.View>(R.id.widget_refresh).hasOnClickListeners())
            assertTrue(small.hasOnClickListeners())
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
