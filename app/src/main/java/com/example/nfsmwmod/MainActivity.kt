package com.example.nfsmwmod

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.provider.OpenableColumns

class MainActivity : ComponentActivity() {
    private lateinit var isoStatus: TextView
    private lateinit var details: TextView
    private lateinit var launchButton: Button
    private var selectedUri: Uri? = null

    private val picker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) acceptIso(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildScreen()
        restoreSelection()
    }

    private fun buildScreen() {
        val bg = Color.rgb(16, 17, 20)
        val panel = Color.rgb(29, 31, 37)
        val white = Color.WHITE
        val muted = Color.rgb(180, 184, 194)
        val accent = Color.rgb(255, 176, 0)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(28), dp(22), dp(22))
            setBackgroundColor(bg)
        }
        val scroll = ScrollView(this).apply { addView(root) }

        val brand = TextView(this).apply {
            text = "NEED FOR SPEED"
            textSize = 14f
            setTextColor(accent)
            letterSpacing = .18f
            typeface = Typeface.DEFAULT_BOLD
        }
        root.addView(brand)

        val title = TextView(this).apply {
            text = "MOST WANTED\nMOD LAUNCHER"
            textSize = 30f
            setTextColor(white)
            typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
            setPadding(0, dp(8), 0, dp(8))
        }
        root.addView(title)

        val subtitle = TextView(this).apply {
            text = "Pilih file game milikmu untuk menyiapkan permainan. ISO tetap berada di penyimpanan; aplikasi hanya menyimpan izin aksesnya."
            textSize = 14f
            setTextColor(muted)
            setLineSpacing(dp(3).toFloat(), 1f)
        }
        root.addView(subtitle, margin(bottom = 22))

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
            background = rounded(panel, 18)
        }
        val cardTitle = TextView(this).apply {
            text = "FILE GAME"
            textSize = 12f
            setTextColor(accent)
            typeface = Typeface.DEFAULT_BOLD
        }
        card.addView(cardTitle)
        isoStatus = TextView(this).apply {
            text = "Belum ada ISO dipilih"
            textSize = 18f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, dp(12), 0, dp(6))
        }
        card.addView(isoStatus)
        details = TextView(this).apply {
            text = "Format yang didukung untuk dipilih: .iso atau .xiso"
            textSize = 13f
            setTextColor(muted)
        }
        card.addView(details, margin(bottom = 16))
        val choose = Button(this).apply {
            text = "PILIH ISO GAME"
            setTextColor(Color.BLACK)
            setBackgroundTintList(android.content.res.ColorStateList.valueOf(accent))
            setOnClickListener { picker.launch(arrayOf("*/*")) }
        }
        card.addView(choose, ViewGroup.LayoutParams(-1, dp(50)))
        root.addView(card, margin(bottom = 18))

        val features = TextView(this).apply {
            text = "TARGET MODIFIKASI\n\n• 6 Bonus Markers dapat dipilih\n• Pink Slip tetap berfungsi\n• Save game lama tidak ditimpa\n• ISO dipilih dari dalam aplikasi"
            textSize = 14f
            setTextColor(white)
            setLineSpacing(dp(4).toFloat(), 1f)
            setPadding(dp(18), dp(16), dp(18), dp(16))
            background = rounded(panel, 18)
        }
        root.addView(features, margin(bottom = 18))

        launchButton = Button(this).apply {
            text = "ENGINE GAME BELUM TERPASANG"
            isEnabled = false
            setOnClickListener { showEngineNotice() }
        }
        root.addView(launchButton, ViewGroup.LayoutParams(-1, dp(52)))

        val note = TextView(this).apply {
            text = "STATUS: launcher awal 0.1.0 — engine game dan patch gameplay belum terintegrasi. Memilih ISO belum menjalankan game."
            textSize = 12f
            setTextColor(muted)
            gravity = Gravity.CENTER
            setPadding(0, dp(18), 0, 0)
        }
        root.addView(note)
        setContentView(scroll)
    }

    private fun acceptIso(uri: Uri) {
        try {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            contentResolver.takePersistableUriPermission(uri, flags)
            val name = queryName(uri) ?: "File game dipilih"
            val lower = name.lowercase()
            if (!lower.endsWith(".iso") && !lower.endsWith(".xiso")) {
                Toast.makeText(this, "Pilih file dengan ekstensi .iso atau .xiso", Toast.LENGTH_LONG).show()
                return
            }
            selectedUri = uri
            getSharedPreferences("launcher", MODE_PRIVATE).edit().putString("iso_uri", uri.toString()).apply()
            isoStatus.text = name
            details.text = "File terpilih. Izin akses tersimpan; ukuran: ${querySize(uri)}"
            Toast.makeText(this, "ISO berhasil dipilih", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Tidak dapat membuka file: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun restoreSelection() {
        val saved = getSharedPreferences("launcher", MODE_PRIVATE).getString("iso_uri", null) ?: return
        val uri = Uri.parse(saved)
        try {
            contentResolver.openFileDescriptor(uri, "r")?.use { }
            selectedUri = uri
            isoStatus.text = queryName(uri) ?: "ISO tersimpan"
            details.text = "Akses file tersimpan. Ukuran: ${querySize(uri)}"
        } catch (_: Exception) {
            getSharedPreferences("launcher", MODE_PRIVATE).edit().remove("iso_uri").apply()
            details.text = "File sebelumnya tidak dapat diakses. Silakan pilih ulang."
        }
    }

    private fun queryName(uri: Uri): String? = contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
        if (it.moveToFirst()) it.getString(0) else null
    }

    private fun querySize(uri: Uri): String {
        val bytes = contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use {
            if (it.moveToFirst() && !it.isNull(0)) it.getLong(0) else -1L
        } ?: -1L
        if (bytes < 0) return "tidak diketahui"
        val gib = bytes / 1073741824.0
        return if (gib >= 1) String.format("%.2f GB", gib) else String.format("%.1f MB", bytes / 1048576.0)
    }

    private fun showEngineNotice() {
        Toast.makeText(this, "Engine game belum terintegrasi. Pemilihan ISO saja belum cukup untuk menjalankan game.", Toast.LENGTH_LONG).show()
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun margin(bottom: Int = 0): LinearLayout.LayoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(bottom) }
    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply { setColor(color); cornerRadius = dp(radius).toFloat() }
}
