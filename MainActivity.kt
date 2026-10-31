package com.autotouchpro

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.*

class MainActivity : Activity() {
    private var selectedCpm = 120

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 36, 28, 24)
            setBackgroundColor(0xFF101923.toInt())
        }

        val title = TextView(this).apply {
            text = "AUTO TOUCH PRO"
            textSize = 26f
            setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER
        }
        layout.addView(title)

        val subtitle = TextView(this).apply {
            text = "Mira flotante • Funcionamiento sin Internet"
            textSize = 14f
            setTextColor(0xFFB8C7D8.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 24)
        }
        layout.addView(subtitle)

        val speedLabel = TextView(this).apply {
            text = "VELOCIDAD INICIAL"
            textSize = 14f
            setTextColor(0xFFFFFFFF.toInt())
        }
        layout.addView(speedLabel)

        val speeds = listOf(60, 120, 200, 300, 500, 1000)
        val spinner = Spinner(this)
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, speeds.map { "$it clics/minuto" })
        spinner.setSelection(1)
        spinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedCpm = speeds[position]
            }
        }
        layout.addView(spinner)

        fun addButton(label: String, action: () -> Unit) {
            val button = Button(this).apply {
                text = label
                isAllCaps = false
                setOnClickListener { action() }
            }
            val params = LinearLayout.LayoutParams(-1, -2)
            params.topMargin = 14
            layout.addView(button, params)
        }

        addButton("1. Permitir superposición") {
            if (!Settings.canDrawOverlays(this)) {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            } else toast("Permiso de superposición concedido.")
        }
        addButton("2. Activar servicio de accesibilidad") {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        addButton("3. MOSTRAR MIRA FLOTANTE") {
            if (!Settings.canDrawOverlays(this)) {
                toast("Primero concede el permiso de superposición.")
            } else {
                val intent = Intent(this, FloatingService::class.java).putExtra("cpm", selectedCpm)
                if (Build.VERSION.SDK_INT >= 26) startForegroundService(intent) else startService(intent)
                toast("Mira flotante iniciada.")
            }
        }
        addButton("OCULTAR MIRA FLOTANTE") {
            stopService(Intent(this, FloatingService::class.java))
        }

        val note = TextView(this).apply {
            text = "Para tocar otras aplicaciones, Android requiere que habilites manualmente el servicio de accesibilidad de AUTO TOUCH PRO. La aplicación no lee el contenido de la pantalla."
            textSize = 13f
            setTextColor(0xFFB8C7D8.toInt())
            setPadding(0, 24, 0, 0)
        }
        layout.addView(note)
        setContentView(ScrollView(this).apply { addView(layout) })
    }

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}
