package app.rork.quran.tafseer

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.rork.quran.MainActivity
import app.rork.quran.domain.model.Ayah
import app.rork.quran.notification.NotificationHelper
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationTafseerTest {
    @Test fun longerSemanticGroupsAreNotReplacedOrCutInNotificationPayloads() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        if (Build.VERSION.SDK_INT >= 33) {
            instrumentation.uiAutomation.grantRuntimePermission(context.packageName, android.Manifest.permission.POST_NOTIFICATIONS)
        }
        val helper = NotificationHelper(context)
        helper.createChannel()
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val id = 9175
        try {
            assertTrue(helper.showAyahNotification(Ayah("53-40", 53, "النجم", 40, "قديم"), id))
            val deadline = android.os.SystemClock.elapsedRealtime() + 5_000
            while (manager.activeNotifications.none { it.id == id } && android.os.SystemClock.elapsedRealtime() < deadline) {
                Thread.sleep(100)
            }
            val notification = manager.activeNotifications.single { it.id == id }.notification
            val passage = app.rork.quran.data.repository.PassageCatalog(context).resolve("53-40")
            assertEquals(17, passage.verseIds.size)
            assertEquals(passage.textArabic, notification.extras.getCharSequence(android.app.Notification.EXTRA_BIG_TEXT).toString())
            assertTrue(notification.extras.getCharSequence(android.app.Notification.EXTRA_SUB_TEXT).toString().contains(passage.reference))
        } finally {
            manager.cancel(id)
        }
    }

    @Test fun tafseerActionOpensTheDisplayedVerseAndReader() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        if (Build.VERSION.SDK_INT >= 33) {
            instrumentation.uiAutomation.grantRuntimePermission(context.packageName, android.Manifest.permission.POST_NOTIFICATIONS)
        }
        val helper = NotificationHelper(context)
        helper.createChannel()
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val id = 9174
        val monitor = instrumentation.addMonitor(MainActivity::class.java.name, null, false)
        try {
            assertTrue(helper.showAyahNotification(Ayah("1-1", 1, "الفاتحة", 1, "بسم الله الرحمن الرحيم"), id))
            val postedDeadline = android.os.SystemClock.elapsedRealtime() + 5_000
            while (manager.activeNotifications.none { it.id == id } && android.os.SystemClock.elapsedRealtime() < postedDeadline) {
                Thread.sleep(100)
            }
            val notification = manager.activeNotifications.single { it.id == id }.notification
            val passage = app.rork.quran.data.repository.PassageCatalog(context).resolve("1-1")
            assertEquals(passage.textArabic, notification.extras.getCharSequence(android.app.Notification.EXTRA_BIG_TEXT).toString())
            assertTrue(notification.extras.getCharSequence(android.app.Notification.EXTRA_SUB_TEXT).toString().contains(passage.reference))
            assertTrue(notification.actions.any { it.title.toString().contains("قرأت") })
            val action = notification.actions.single { it.title.toString() == "التفسير" }
            action.actionIntent.send()
            val activity = instrumentation.waitForMonitorWithTimeout(monitor, 30_000)
            assertNotNull(activity)
            assertEquals("ayat://tafseer/1-1", activity.intent.data.toString())
            fun containsText(node: android.view.accessibility.AccessibilityNodeInfo?, text: String): Boolean {
                if (node == null) return false
                if (node.text?.contains(text) == true) return true
                return (0 until node.childCount).any { containsText(node.getChild(it), text) }
            }
            // A fresh install seeds the offline Quran before rendering the reader.
            val deadline = android.os.SystemClock.elapsedRealtime() + 90_000
            while (!containsText(instrumentation.uiAutomation.rootInActiveWindow, "الآية وتفسيرها") &&
                android.os.SystemClock.elapsedRealtime() < deadline) Thread.sleep(200)
            assertTrue(containsText(instrumentation.uiAutomation.rootInActiveWindow, "الآية وتفسيرها"))
            assertTrue(containsText(instrumentation.uiAutomation.rootInActiveWindow, "التفسير الميسر"))
            instrumentation.runOnMainSync { activity.finish() }
        } finally {
            manager.cancel(id)
            instrumentation.removeMonitor(monitor)
        }
    }
}
