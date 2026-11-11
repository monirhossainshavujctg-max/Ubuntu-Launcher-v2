package com.ubuntu.launcher

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PixelFormat
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextClock
import android.widget.TextView
import androidx.core.content.ContextCompat

/** সব অ্যাপের উপরে Ubuntu-স্টাইল টপ বার বসায় (সিস্টেম স্ট্যাটাস বারের জায়গায়)। */
class UbuntuShellService : AccessibilityService() {

    private var bar: View? = null
    private var netText: TextView? = null
    private var batText: TextView? = null
    private lateinit var wm: WindowManager

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, i: Intent) {
            val level = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = i.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
            val st = i.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val chg = st == BatteryManager.BATTERY_STATUS_CHARGING || st == BatteryManager.BATTERY_STATUS_FULL
            if (level >= 0) batText?.text = (if (chg) "⚡ " else "🔋 ") + (level * 100 / scale) + "%"
        }
    }

    private val netCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(n: Network, c: NetworkCapabilities) {
            val t = when {
                c.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "📶 Wi-Fi"
                c.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "📱 Mobile"
                else -> "🌐 Online"
            }
            bar?.post { netText?.text = t }
        }
        override fun onLost(n: Network) { bar?.post { netText?.text = "✖ Offline" } }
    }

    override fun onServiceConnected() {
        instance = this
        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        buildBar()
        ContextCompat.registerReceiver(
            this, batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )?.let { batteryReceiver.onReceive(this, it) }
        try {
            getSystemService(ConnectivityManager::class.java).registerDefaultNetworkCallback(netCallback)
        } catch (_: Exception) {}
    }

    private fun buildBar() {
        val d = resources.displayMetrics.density
        val id = resources.getIdentifier("status_bar_height", "dimen", "android")
        val h = if (id > 0) resources.getDimensionPixelSize(id) else (28 * d).toInt()

        fun tv(text: String, bold: Boolean = false) = TextView(this).apply {
            this.text = text
            setTextColor(Color.WHITE)
            textSize = 12f
            gravity = Gravity.CENTER
            if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

        val activities = tv("Activities", true).apply {
            setPadding((12 * d).toInt(), 0, (12 * d).toInt(), 0)
            setOnClickListener { openLauncher(MainActivity.ACTION_DRAWER) }
        }
        val clock = TextClock(this).apply {
            format12Hour = "EEE d MMM   h:mm a"
            format24Hour = "EEE d MMM   HH:mm"
            setTextColor(Color.WHITE)
            textSize = 12f
            gravity = Gravity.CENTER
            setOnClickListener { performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS) }
        }
        netText = tv("").apply { setPadding((6 * d).toInt(), 0, (6 * d).toInt(), 0) }
        batText = tv("").apply {
            setPadding((6 * d).toInt(), 0, (12 * d).toInt(), 0)
            setOnClickListener { performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS) }
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(Color.BLACK)
            addView(activities, LinearLayout.LayoutParams(-2, -1))
            addView(clock, LinearLayout.LayoutParams(0, -1, 1f))
            addView(netText, LinearLayout.LayoutParams(-2, -1))
            addView(batText, LinearLayout.LayoutParams(-2, -1))
        }

        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT, h,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP }

        bar = root
        wm.addView(root, lp)
    }

    private fun openLauncher(action: String) {
        startActivity(
            Intent(this, MainActivity::class.java).setAction(action)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        )
    }

    override fun onAccessibilityEvent(e: AccessibilityEvent) {
        if (e.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = e.packageName?.toString() ?: return
        if (pkg == "com.android.systemui") return
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (imm.enabledInputMethodList.any { it.packageName == pkg }) return
        // লঞ্চারের ভেতরে নিজস্ব টপ বার আছে, তাই সেখানে ওভারলে লুকাই
        bar?.visibility = if (pkg == packageName) View.GONE else View.VISIBLE
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        try { bar?.let { wm.removeView(it) } } catch (_: Exception) {}
        bar = null
        try { unregisterReceiver(batteryReceiver) } catch (_: Exception) {}
        try { getSystemService(ConnectivityManager::class.java).unregisterNetworkCallback(netCallback) } catch (_: Exception) {}
    }

    companion object {
        var instance: UbuntuShellService? = null
    }
}
