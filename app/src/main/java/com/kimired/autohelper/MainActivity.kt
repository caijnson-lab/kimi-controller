package com.kimired.autohelper

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.view.View

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
        
        if (isEnabled) {
            statusText.text = "无障碍服务：✅ 已启用"
            enableButton.text = "服务已启用"
            enableButton.isEnabled = false
        } else {
            statusText.text = "无障碍服务：❌ 未启用"
            enableButton.text = "启用无障碍服务"
            enableButton.isEnabled = true
        }

        serverStatusText.text = "HTTP 服务器：🟢 运行中 (端口 8082)"
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        
        for (service in enabledServices) {
            if (service.resolveInfo.serviceInfo.packageName == packageName) {
                return true
            }
        }
        return false
    }

    fun openAccessibilitySettings(v: View? = null) {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            Toast.makeText(this, "请找到 'Kimi Controller' 并启用", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "无法打开设置，请手动进入", Toast.LENGTH_SHORT).show()
        }
    }
}
