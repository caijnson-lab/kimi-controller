package com.kimired.autohelper

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var serverStatusText: TextView
    private lateinit var enableButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        serverStatusText = findViewById(R.id.serverStatusText)
        enableButton = findViewById(R.id.enableButton)

        enableButton.setOnClickListener {
            openAccessibilitySettings()
        }

        updateStatus()
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun updateStatus() {
        val isEnabled = isAccessibilityServiceEnabled()
        statusText.text = if (isEnabled) {
            "无障碍服务: 已启用 ✅"
        } else {
            "无障碍服务: 未启用 ❌"
        }

        val devInfo = "\n\n开发者: Kimi_Red ❤️"
        serverStatusText.text = if (isEnabled) {
            "HTTP 服务器: 运行中 🟢\n端口: 8082$devInfo"
        } else {
            "HTTP 服务器: 已停止 🔴$devInfo"
        }

        enableButton.text = if (isEnabled) {
            "重新配置"
        } else {
            "启用无障碍服务"
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val accessibilityEnabled = Settings.Secure.getInt(
            contentResolver,
            Settings.Secure.ACCESSIBILITY_ENABLED,
            0
        )
        if (accessibilityEnabled == 1) {
            val services = Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            return services?.contains(packageName) == true
        }
        return false
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }
}
