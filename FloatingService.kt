package com.autotouchpro

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.*
import android.widget.*
import kotlin.math.max

class FloatingService : Service() {
    private lateinit var windowManager: WindowManager
    private var targetView: TextView? = null
    private var targetLayoutParams: WindowManager.LayoutParams? = null
    private var panel: LinearLayout? = null
    private var running = false
    private var cpm = 120
    private var clicks = 0L
    private var targetX = 0f
    private var targetY = 0f
    private val handler = Handler(Looper.getMainLooper())
    private val loop = object : Runnable {
        override fun run() {
            if (!running) return
            val service = TapAccessibilityService.instance
            if (service == null) {
                status?.text = "Activa Accesibilidad"
                running = false
                return
            }
            service.tap(targetX, targetY) { success ->
                if (success) {
                    clicks++
                    count?.text = "Clics: $clicks"
                }
            }
            handler.postDelayed(this, max(1L, 60000L / cpm))
        }
    }
    private var status: TextView? = null
    private var count: TextView? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotification()
        showOverlay()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        cpm = (intent?.getIntExtra("cpm", 120) ?: 120).coerceIn(1, 1000)
        return START_STICKY
    }

    private fun createNotification() {
        val channelId = "auto_touch_running"
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(channelId, "AUTO TOUCH PRO", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        val notification = Notification.Builder(this, channelId)
            .setContentTitle("AUTO TOUCH PRO activo")
            .setContentText("Controles flotantes disponibles")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .build()
        startForeground(7, notification)
    }

    private fun overlayType(): Int =
        if (Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else WindowManager.LayoutParams.TYPE_PHONE

    private fun showOverlay() {
        val density = resources.displayMetrics.density
        val target = TextView(this).apply {
            text = "◎"
            textSize = 38f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(0xAA126B58.toInt())
                setStroke((2 * density).toInt(), Color.WHITE)
            }
            elevation = 12f
        }
        val targetParams = WindowManager.LayoutParams(
            (64 * density).toInt(), (64 * density).toInt(), overlayType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.START; x = 140; y = 400 }

        target.setOnTouchListener(object : View.OnTouchListener {
            var downX = 0f; var downY = 0f; var startX = 0; var startY = 0; var moved = false
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        downX = event.rawX; downY = event.rawY
                        startX = targetParams.x; startY = targetParams.y; moved = false
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - downX).toInt()
                        val dy = (event.rawY - downY).toInt()
                        if (kotlin.math.abs(dx) + kotlin.math.abs(dy) > 5) moved = true
                        targetParams.x = startX + dx; targetParams.y = startY + dy
                        windowManager.updateViewLayout(target, targetParams)
                        targetX = targetParams.x + target.width / 2f
                        targetY = targetParams.y + target.height / 2f
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (!moved) toggle()
                        return true
                    }
                }
                return false
            }
        })
        targetX = targetParams.x + 32 * density
        targetY = targetParams.y + 32 * density
        targetView = target
        targetLayoutParams = targetParams
        windowManager.addView(target, targetParams)

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12, 10, 12, 10)
            background = GradientDrawable().apply {
                setColor(0xEE101923.toInt())
                cornerRadius = 18 * density
                setStroke((1 * density).toInt(), 0xFF3C596A.toInt())
            }
        }
        val heading = TextView(this).apply {
            text = "AUTO TOUCH PRO  •  ${cpm} CPM"
            textSize = 13f; setTextColor(Color.WHITE)
        }
        status = TextView(this).apply { text = "DETENIDO"; textSize = 12f; setTextColor(0xFF80D6B5.toInt()) }
        count = TextView(this).apply { text = "Clics: 0"; textSize = 12f; setTextColor(Color.WHITE) }
        val controls = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val start = Button(this).apply { text = "INICIAR"; setOnClickListener { startTaps() } }
        val stop = Button(this).apply { text = "DETENER"; setOnClickListener { stopTaps() } }
        val close = Button(this).apply { text = "×"; setOnClickListener { stopSelf() } }
        controls.addView(start); controls.addView(stop); controls.addView(close)
        box.addView(heading); box.addView(status); box.addView(count); box.addView(controls)
        val panelParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType(), WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.START; x = 12; y = 110 }
        panel = box
        windowManager.addView(box, panelParams)
    }

    private fun toggle() { if (running) stopTaps() else startTaps() }

    private fun startTaps() {
        if (TapAccessibilityService.instance == null) {
            status?.text = "Activa Accesibilidad"
            Toast.makeText(this, "Activa AUTO TOUCH PRO en Accesibilidad.", Toast.LENGTH_LONG).show()
            return
        }
        running = true
        // While tapping, make the crosshair window click-through so it does not
        // intercept the gesture intended for the app underneath.
        targetLayoutParams?.let {
            it.flags = it.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            try { targetView?.let { view -> windowManager.updateViewLayout(view, it) } } catch (_: Exception) {}
        }
        status?.text = "EJECUTANDO • $cpm CPM"
        handler.removeCallbacks(loop)
        handler.post(loop)
    }

    private fun stopTaps() {
        running = false
        handler.removeCallbacks(loop)
        // Re-enable dragging after stopping.
        targetLayoutParams?.let {
            it.flags = it.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
            try { targetView?.let { view -> windowManager.updateViewLayout(view, it) } } catch (_: Exception) {}
        }
        status?.text = "DETENIDO"
    }

    override fun onDestroy() {
        stopTaps()
        try { targetView?.let { windowManager.removeView(it) } } catch (_: Exception) {}
        try { panel?.let { windowManager.removeView(it) } } catch (_: Exception) {}
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
