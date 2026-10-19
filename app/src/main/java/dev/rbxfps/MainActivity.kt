package dev.rbxfps

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import rikka.shizuku.Shizuku

class MainActivity : AppCompatActivity() {

    private companion object {
        const val FILE = "/data/local/tmp/ClientAppSettings.json"
        const val ROBLOX = "com.roblox.client"
        const val GOS = "com.samsung.android.game.gos"
        const val FLAG = "DFIntTaskSchedulerTargetFps"
        val FPS_VALUES = listOf(60, 90, 120, 144, 165, 200, 240)
        const val DEFAULT_FPS = 60
    }

    private lateinit var modeToggle: MaterialButtonToggleGroup
    private lateinit var fpsDropdown: MaterialAutoCompleteTextView
    private lateinit var logView: TextView

    private val permListener = Shizuku.OnRequestPermissionResultListener { _, result ->
        log(
            if (result == PackageManager.PERMISSION_GRANTED) "Shizuku permission granted. Tap the button again."
            else "Shizuku permission denied."
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Keep content clear of the status/navigation bars.
        val root = findViewById<View>(R.id.root)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        modeToggle = findViewById(R.id.modeToggle)
        fpsDropdown = findViewById(R.id.fpsDropdown)
        logView = findViewById(R.id.logView)

        fpsDropdown.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                FPS_VALUES.map { "$it FPS" }
            )
        )
        fpsDropdown.setText("$DEFAULT_FPS FPS", false)

        Shizuku.addRequestPermissionResultListener(permListener)

        findViewById<View>(R.id.btnApply).setOnClickListener {
            val fps = selectedFps()
            val json = "{\"$FLAG\":$fps}"
            exec(
                "Apply $fps FPS",
                "rm -f $FILE; printf '%s' '$json' > $FILE && chmod 644 $FILE && am force-stop $ROBLOX && cat $FILE"
            )
        }
        findViewById<View>(R.id.btnRemove).setOnClickListener {
            exec("Remove override", "rm -f $FILE && am force-stop $ROBLOX && echo removed")
        }
        findViewById<View>(R.id.btnStatus).setOnClickListener {
            exec("File status", "ls -l $FILE 2>&1; cat $FILE 2>&1")
        }
        findViewById<View>(R.id.btnGosOff).setOnClickListener {
            exec(
                "Suspend GOS",
                "cmd package suspend --user 0 $GOS; pm disable-user --user 0 $GOS; " +
                    "dumpsys package $GOS | grep -E 'suspended=|enabled=' | head -n 2"
            )
        }
        findViewById<View>(R.id.btnGosOn).setOnClickListener {
            exec(
                "Restore GOS",
                "cmd package unsuspend --user 0 $GOS; pm unhide $GOS 2>&1; pm enable $GOS; " +
                    "dumpsys package $GOS | grep -E 'suspended=|enabled=' | head -n 2"
            )
        }
        findViewById<View>(R.id.btnGosStatus).setOnClickListener {
            exec("GOS status", "dumpsys package $GOS | grep -E 'hidden=|suspended=|enabled=' | head -n 3")
        }
    }

    override fun onDestroy() {
        Shizuku.removeRequestPermissionResultListener(permListener)
        super.onDestroy()
    }

    private fun selectedFps(): Int =
        fpsDropdown.text.toString().filter { it.isDigit() }.toIntOrNull() ?: DEFAULT_FPS

    private fun currentMode(): Mode =
        if (modeToggle.checkedButtonId == R.id.btnModeShizuku) Mode.SHIZUKU else Mode.ROOT

    /** Returns true if the chosen backend is usable; otherwise shows why. */
    private fun ready(mode: Mode): Boolean {
        if (mode == Mode.ROOT) return true // libsu reports root failure with a clear message
        if (!Shizuku.pingBinder()) {
            log("Shizuku is not running. Start it first in the Shizuku app.")
            return false
        }
        if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            Shizuku.requestPermission(1)
            log("Requesting Shizuku permission...")
            return false
        }
        return true
    }

    private fun exec(label: String, cmd: String) {
        val mode = currentMode()
        log("$label...")
        Thread {
            if (!ready(mode)) return@Thread
            val r = Sh.run(mode, cmd)
            log("$label: ${if (r.ok) "OK" else "FAILED"}\n${r.out}")
        }.start()
    }

    /** Output shows only the latest result; each new message replaces the previous one. */
    private fun log(msg: String) {
        runOnUiThread { logView.text = msg }
    }
}
