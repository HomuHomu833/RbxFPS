package com.homuhomu833.rbxfps

import com.topjohnwu.superuser.Shell
import rikka.shizuku.Shizuku

enum class Mode { ROOT, SHIZUKU }

data class ShResult(val ok: Boolean, val out: String)

/** Runs a shell command as root (libsu) or as the adb shell user (Shizuku). Call off the main thread. */
object Sh {

    fun run(mode: Mode, cmd: String): ShResult = try {
        when (mode) {
            Mode.ROOT -> runRoot(cmd)
            Mode.SHIZUKU -> runShizuku(cmd)
        }
    } catch (t: Throwable) {
        ShResult(false, "Error: ${t.javaClass.simpleName}: ${t.message}")
    }

    private fun runRoot(cmd: String): ShResult {
        if (Shell.getShell().isRoot.not()) return ShResult(false, "Root access not granted")
        val r = Shell.cmd(cmd).exec()
        val text = (r.out + r.err).joinToString("\n")
        return ShResult(r.isSuccess, text)
    }

    private fun runShizuku(cmd: String): ShResult {
        // Shizuku.newProcess is private in API 13; reflection is the usual workaround.
        val m = Shizuku::class.java.getDeclaredMethod(
            "newProcess",
            Array<String>::class.java,
            Array<String>::class.java,
            String::class.java
        )
        m.isAccessible = true
        val p = m.invoke(null, arrayOf("sh", "-c", cmd), null, null) as Process
        val out = p.inputStream.bufferedReader().readText()
        val err = p.errorStream.bufferedReader().readText()
        val code = p.waitFor()
        return ShResult(code == 0, (out + err).trim())
    }
}
